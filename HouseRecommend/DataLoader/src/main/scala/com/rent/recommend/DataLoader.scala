package com.rent.recommend

import java.net.InetAddress

import com.mongodb.casbah.{MongoClient, MongoClientURI}
import com.mongodb.casbah.commons.MongoDBObject
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.spark.sql.EsSparkSQL
import org.elasticsearch.transport.client.PreBuiltTransportClient

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/13

在houses表中
diretType: 0毛坯 ， 1 简装， 2 精装
layer: 0高层， 1低层
rentType： 1整租   0合租

*///1室1厅^73㎡^精装^低层(共28层)^整租^建邺-奥体-嘉业国际城^有家私电梯^南京师大附中新城小学(南校区)^6500
//https://saas-qw1.qfangimg.com/pro/f3d6e3d9-d45f-455b-a889-189758c1bfa7.JPG-240x180	/rent/100239581?insource=rent_list&top=1	亚东名座 亚东名座电梯房新街口精装	1室1厅	47.68㎡	精装	中层(共27层)	整租	秦淮-升州路-亚东名座	距离1号线张府园站约382米有家私随时看房	南京市府西街小学
case class MongoConfig(uri:String, db:String)
case class ESConfig(httpHosts:String, index:String, clustername:String)
case class Hplace(pid: Int , name: String, lon: Double , lai: Double)
object DataLoader {
  val HOUSE_PLACE_PATH = "D:\\IntelliJ IDEA-workspace\\HouseRent\\HouseRecommend\\DataLoader\\src\\main\\resources\\newhid.csv";

  val ES_HOUSE_INDEX = "_doc"

  def main(args: Array[String]): Unit = {

    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://localhost:27017/houserec",
      "mongo.db" -> "houserec",
      "es.httpHosts" -> "localhost:9200",
      "es.transportHosts" -> "localhost:9300",
      "es.index" -> "houserec",
      "es.cluster.name" -> "elasticsearch"
    )

    // 创建一个sparkConf
    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("DataLoader")

    // 创建一个SparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._
    // 声明了一个 ES 配置的隐式参数
   // implicit val esConfig = ESConfig(config("es.httpHosts"), config("es.index"), config("es.cluster.name"))
    //    val houseDF = spark.sql("select hid,")
    val houseRDD = spark.sparkContext.textFile(HOUSE_PLACE_PATH)
    val houseDF = houseRDD.map(
      f = item => {
        val attr = item.split(",")
       // val lon = attr(2).toDouble
        Hplace(attr(0).toInt,attr(1).trim,attr(2).toDouble,attr(3).toDouble)
      }
    ).toDF()

   // EsSparkSQL.saveToEs(houseDF,"house")
    storeDataInES(houseDF)
    //storeDataInES(underDF)


    // 关闭 Spark
    spark.stop()

  }
  def storeDataInES(movieDF:DataFrame): Unit = {
    movieDF
      .write
      .option("es.nodes","localhost:9200")
      .option("es.http.timeout","100m")
      .option("es.mapping.id","pid")
      .option("es.batch.write.retry.count", "10")
      .mode("append")
      .format("org.elasticsearch.spark.sql")
      .save("houserec"+"/"+ES_HOUSE_INDEX)
  }
}
