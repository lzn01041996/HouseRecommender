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

*/
//1室1厅^73㎡^精装^低层(共28层)^整租^建邺-奥体-嘉业国际城^有家私电梯^南京师大附中新城小学(南校区)^6500
case class NewHouse(hid: Int, title: String, singleType: String, size: String, directType: String, singleLayer: String, rentType: String, place: String, underPlace: String, schoolPlace: String, price: Int)

case class Houses(hid: Int, singleType: String, size: Double, directType: Int, layer: Double, rentType: Int, underPlace: String, schoolPlace: String, price: Double)

//case class MongoConfig(uri:String, db:String)
//case class ESConfig(httpHosts:String, transportHosts:String, index:String, clustername:String)
case class Browse(uid: Int, hid: Int, times: Int)

object AddHouse {
  val HOUSE_DATA_PATH = "D:\\IntelliJ IDEA-workspace\\HouseRent\\Rent-spiderData\\src\\main\\log\\logs.log"

  val MONGODB_HOUSES_COLLECTION = "Houses"
  val MONGODB_HOUSE_COLLECTION = "House"


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
        //https://saas-qw4.qfangimg.com/pro/1fee026c-d05c-45ac-a447-eab5c734cd2b.jpg-240x180^/rent/100281910?insource=rent_list&top=1681^海宇公寓 2室1厅1厨1卫 50.0m² 整租^2室1厅3333^50㎡^精装^低层(共9层)^整租^秦淮-五老村-海宇公寓^有家私家电齐全^南京市五老村小学^3300
        //case class Houses(hid:Int,singleType:String,size:Double,directType:Int,layer:Double,rentType:Int,underPlace:String,schoolPlace:String,price:Double)
        var s5 = if (attr(5).contains("毛坯")) {
          0
        } else if (attr(5).contains("简装")) {
          1
        } else {
          2
        }

        var s6 = if (attr(6).contains("高层")) {
          0
        } else {
          1
        }
        var s7 = attr(7).trim match {
          case "整租" => 1
          case "合租" => 0

        }

        Houses(hid.toInt, attr(3).trim, attr(4).replace("㎡", "").toDouble, s5.toInt, s6.toDouble, s7.toInt, attr(9).trim, attr(10).trim, attr(11).toDouble)

      }
    ).toDF()

    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    val housesDF = spark.read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_HOUSE_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[House]
      .map(
        //case class Houses(hid:Int,singleType:String,size:Double,directType:Int,layer:Double,rentType:Int,underPlace:String,schoolPlace:String,price:Double)
        x => {
          val s5 = if (x.directType.contains("毛坯")) {
            0
          } else if (x.directType.contains("简装")) {
            1
          } else {
            2
          }

          val s6 = if (x.singleLayer.contains("高层")) {
            0
          } else {
            1
          }
          val s7 = x.rentType match {
            case "整租" => 1
            case "合租" => 0

          }
          Houses(x.hid, x.singleType, x.size.replace("㎡", "").toDouble, s5, s6, s7, x.underPlace, x.schoolPlace, x.price)
        }
      ).toDF("hid", "singleType", "size", "directType", "singleLayer", "rentType", "underPlace", "schoolPlace", "price")

    // 将数据保存到MongoDB
    storeDataInMongoDB(housesDF)
    // 声明了一个 ES 配置的隐式参数

    // 关闭 Spark
    spark.stop()

  }

  def storeDataInMongoDB(browseDF: DataFrame)(implicit mongoConfig: MongoConfig): Unit = {
    // 新建一个mongodb的连接
    val mongoClient = MongoClient(MongoClientURI(mongoConfig.uri))

    // 如果mongodb中已经有相应的数据库，先删除
    mongoClient(mongoConfig.db)(MONGODB_HOUSES_COLLECTION).dropCollection()
    // 将DF数据写入对应的mongodb表中
    browseDF.write
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_HOUSES_COLLECTION)
      .mode("append")
      .format("com.mongodb.spark.sql")
      .save()

    //对数据表建索引
    mongoClient(mongoConfig.db)(MONGODB_HOUSES_COLLECTION).createIndex(MongoDBObject("hid" -> 1))


    mongoClient.close()

  }

  def storeDataInES(movieDF: DataFrame)(implicit eSConfig: ESConfig): Unit = {
    movieDF
      .write
      .option("es.nodes", eSConfig.httpHosts)
      .option("es.http.timeout", "100m")
      .option("es.mapping.id", "mid")
      .mode("overwrite")
      .format("org.elasticsearch.spark.sql")
      .save(eSConfig.index + "/")
  }
}
