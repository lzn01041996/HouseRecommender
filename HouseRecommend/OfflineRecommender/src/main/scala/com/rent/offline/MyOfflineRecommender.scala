package com.rent.offline

import com.rent.offline.DIY.{ItemPref1, ItemSimilarity, RecommendedItem, UserRecomm1}
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/10/9

*/
case class Recommendation(hid: Int, count: Double )
case class Browse(uid: Int, hid: Int, times: Int)
case class Houses(hid: Int, singleType: String, size: Double, directType: Int, layer: Int, rentType: Int, underPlace: String, schoolPlace: String , price: Int)
//case class HouseBrowse(uid: Int,hid: Int, count: Long)
case class HouseSimi(hid1:Int,hid2:Int)

case class UserRecomm(uid:Int,hid:Int,pref:Double)extends Serializable

case class UserRecs(uid: Int, recs: Seq[Recommendation])

case class HouseRecs(hid: Int, recs: Seq[Recommendation])
object MyOfflineRecommender {

  val MONGO_BROWSE_COLLECTION = "Browse"

  val USER_RECS = "UserRecs1";
  val HOUSE_RECS = "HouseRecs"

  val USER_MAX_RECOMMENDATION = 100

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://localhost:27017/houserec",
      "mongo.db" -> "houserec"
    )
    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("OfflineRecommender")

    // 创建一个SparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._

    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    //加载
    val browseRDD = spark.read
      .option("uri",mongoConfig.uri)      
      .option("collection",MONGO_BROWSE_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[Browse]
      .rdd
      .map(rating =>(rating.uid,rating.hid,rating.times.toDouble))
      .cache()

    val userRDD = browseRDD.map(_._1).distinct()
    val houseRDD = browseRDD.map(_._2).distinct()
    val mysimi = new ItemSimilarity()
    val browse_data : RDD[ItemPref1] = browseRDD.map(f => (ItemPref1(f._1,f._2,f._3))).cache()
    val houseRecs = mysimi.Similarity1(browse_data,"cosine")
    val recommd = new RecommendedItem
    val recommdRDD : RDD[UserRecomm1] = recommd.Recommend1(houseRecs,browse_data,30)

    val userRecs = recommdRDD.filter(_.pref > 0)
      .map(rating => (rating.userId,(rating.itemId,rating.pref)))
      .groupByKey()
      .map{
        case(uid,recs) => UserRecs(uid,recs.toList.sortWith(_._2 >_._2).take(USER_MAX_RECOMMENDATION).map(x => Recommendation(x._1,x._2)))
      }
      .toDF()
    userRecs.write
      .option("uri",mongoConfig.uri)
      .option("collection", USER_RECS)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()


    spark.stop()


  }
}
