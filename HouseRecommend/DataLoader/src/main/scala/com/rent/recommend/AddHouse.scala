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
case class NewHouse(hid: Int, title: String, singleType: String, size: String, directType: String, singleLayer: String, rentType: String, place: String, underPlace: String, schoolPlace: String , price: Int)
//case class MongoConfig(uri:String, db:String)
//case class ESConfig(httpHosts:String, transportHosts:String, index:String, clustername:String)
case class Browse(uid: Int, hid: Int, times: Int)
object AddHouse {
  val HOUSE_DATA_PATH = "D:\\IntelliJ IDEA-workspace\\HouseRent\\Rent-spiderData\\src\\main\\log\\logs.log"

  val MONGODB_HOUSES_COLLECTION = "House"

  def main(args: Array[String]): Unit = {

    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://localhost:27017/houserec",
      "mongo.db" -> "houserec"
    )

    // 创建一个sparkConf
    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("DataLoader")

    // 创建一个SparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._

    // 加载数据
    val movieRDD = spark.sparkContext.textFile(HOUSE_DATA_PATH)
    val movieDF = movieRDD.map(
      f = item => {
        val attr = item.split("\\^")
        val hid = attr(1).split("top=")(1)
        //NewHouse(hid: Int, houseSingleUrl: String, title: String, singleType: String, size: String,
        // directType: String,singleLayer: Int, rentType: Int, place: String, underPlace: String, schoolPlace: String , price: Int)
        NewHouse(hid.toInt, attr(2).trim, attr(3).trim, attr(4).trim, attr(5).trim, attr(6).trim, attr(7).trim, attr(8).trim, attr(9).trim,attr(10).trim,attr(11).toInt)
      }
    ).toDF()

    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    // 将数据保存到MongoDB
    storeDataInMongoDB(movieDF)
    // 声明了一个 ES 配置的隐式参数

    // 关闭 Spark
    spark.stop()

  }
  def storeDataInMongoDB(browseDF: DataFrame)(implicit mongoConfig: MongoConfig): Unit ={
    // 新建一个mongodb的连接
    val mongoClient = MongoClient(MongoClientURI(mongoConfig.uri))

    // 如果mongodb中已经有相应的数据库，先删除
   // mongoClient(mongoConfig.db)(MONGODB_HOUSES_COLLECTION).dropCollection()


    // 将DF数据写入对应的mongodb表中
    browseDF.write
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_HOUSES_COLLECTION)
      .mode("append")
      .format("com.mongodb.spark.sql")
      .save()

    //对数据表建索引
   // mongoClient(mongoConfig.db)(MONGODB_HOUSES_COLLECTION).createIndex(MongoDBObject("hid" -> 1))


    mongoClient.close()

  }
  def storeDataInES(movieDF:DataFrame)(implicit eSConfig: ESConfig): Unit = {
    movieDF
      .write
      .option("es.nodes",eSConfig.httpHosts)
      .option("es.http.timeout","100m")
      .option("es.mapping.id","mid")
      .mode("overwrite")
      .format("org.elasticsearch.spark.sql")
      .save(eSConfig.index+"/")
  }
}
