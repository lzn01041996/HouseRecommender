package com.rent.content

import java.util

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import com.rent.content.Kmeans.centers
import org.apache.spark.SparkConf
import org.apache.spark.ml.feature.{HashingTF, IDF, Tokenizer}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.Random
import scala.util.control.Breaks.{break, breakable}

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/10/9

*/

//房源信息
//case class House(hid: Int, houseSingleUrl: String, title: String, singleType: String, size: String, directType: String, singleLayer: String, rentType: String, place: String, underPlace: String, schoolPlace: String , price: Int)
//"_id" : ObjectId("5f3de5603016c40728e9d46e"),
//        "hid" : 882,
//        "singleType" : "3室1厅",
//        "size" : 60,
//        "directType" : 1,
//        "layer" : 2,
//        "rentType" : 1,
//        "underPlace" : "距离3号线浮桥站约261米空房",
//        "schoolPlace" : "南京师范大学附属小学（珠江路校区）",
//        "price" : 12500
case class Houses(hid:Int,singleType:String,size:Double,directType:Int,singleLayer:Double,rentType:Int,underPlace:String,schoolPlace:String,price:Double)
//case class MongoConfig(uri: String,db:String)
//一个基准的推荐对象
//case class Recommendation(hid: Int, count: Double)
//定义内容信息提取出的特征向量的房子相似度列表
//case class HouseRecs(hid: Int, recs:Seq[Recommendation])
case class HouseType(hid:Int,size:Double,layer:Double,price:Double,types:Double)
case class HouseT(size:Double,layer:Double,price:Double,types:Double)


object MyContentRecommender {

  //定义常量
  val MONGODB_HOUSES_COLLECTION = "Houses"

  val HOUSE_PRICE_RECS = "PriceBasedHouseRecs"

  val centers = new Array[Vector[Double]](9)

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://localhost1:27017/houserec",
      "mongo.db" -> "houserec"
    )

    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("ContentRecommender")
    //创建一个SparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._

    implicit val mongoConfig = MongoConfig(config("mongo.uri"),config("mongo.db"))

    //加载数据，并做预处理
    val housesDF = spark.read
      .option("uri",mongoConfig.uri)
      .option("collection",MONGODB_HOUSES_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[Houses]
      .map(
        x => (x.hid,x.size/10,x.singleLayer,x.price/1000)
      )
      .toDF("hid","size","singleLayer","price")
      .cache()

    val point = housesDF.collect().map(line =>{
      var vector = Vector[Double]()
      vector ++= Vector(line(1).asInstanceOf[Double])
      vector ++= Vector(line(2).asInstanceOf[Double])
      vector ++= Vector(line(3).asInstanceOf[Double])
      vector
    }).toArray


    val kmeans = new KmeansPP
    kmeans.initialCenters(point,centers)
    kmeans.kmeansppInitial(point,centers)
    kmeans.kmeans(point,centers)
    val pointsNum = point.length
    val pointsLabel = new Array[Int](pointsNum)
    var closestCenter = Vector[Double]()
    for (i <- 0 to pointsNum - 1){
      closestCenter = centers.reduceLeft((c1,c2) => if (kmeans.vectorDis(c1,point(i)) < kmeans.vectorDis(c2,point(i))) c1 else c2)
      pointsLabel(i) = centers.indexOf(closestCenter)
    }
    var i = -1

    val pointTypeData = point.map( f =>{
      //HouseType(f(0),f(1),f(2),pointsLabel(i))
      i = i + 1
      var a:Int = housesDF.collect()(i).get(0).asInstanceOf[Int]
      HouseType(a,f(0),f(1),f(2),pointsLabel(i))
    }).toList

    val typeData = spark.createDataFrame(pointTypeData)

    //storeDataInMongoDB(typeData)

    //********************基于得到的不同类型的聚类中心，开始将各自的聚类中心的不同的类型分别放入同一个列表中********************************************

    val typesDF = spark.sql("select hid,types from PriceBasedHouseRecs group by hid")
    typesDF.collect().foreach(println)

    //********************************************************************************************************************************************

    spark.stop()
  }

  def storeDataInMongoDB(browseDF: DataFrame)(implicit mongoConfig: MongoConfig): Unit ={
    // 新建一个mongodb的连接
    val mongoClient = MongoClient(MongoClientURI(mongoConfig.uri))

    // 如果mongodb中已经有相应的数据库，先删除
    mongoClient(mongoConfig.db)(HOUSE_PRICE_RECS).dropCollection()
    // 将DF数据写入对应的mongodb表中
    browseDF.write
      .option("uri", mongoConfig.uri)
      .option("collection", HOUSE_PRICE_RECS)
      .mode("append")
      .format("com.mongodb.spark.sql")
      .save()

    //对数据表建索引
    mongoClient(mongoConfig.db)(HOUSE_PRICE_RECS).createIndex(MongoDBObject("hid" -> 1))


    mongoClient.close()

  }
}
