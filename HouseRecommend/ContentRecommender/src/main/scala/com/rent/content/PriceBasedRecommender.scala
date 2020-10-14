package com.rent.content

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/10/14

*/

case class Recommender(hid:Int,price:Double)

case class PriceBasedRecommend(types:Int, recs: Seq[Recommender])


object PriceBasedRecommender {

  val HOUSE_PRICE_RECS = "PriceBasedHouseRecs"

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://localhost:27017/houserec",
      "mongo.db" -> "houserec"
    )

    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("ContentRecommender")
    //创建一个SparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._

    implicit val mongoConfig = MongoConfig(config("mongo.uri"),config("mongo.db"))

    val priceDF = spark.read
      .option("uri",mongoConfig.uri)
      .option("collection",HOUSE_PRICE_RECS)
      .format("com.mongodb.spark.sql")
      .load()
      .as[HouseType]
      .toDF()
    priceDF.collect().foreach(print)

    priceDF.createOrReplaceTempView("PriceBasedHouseRecs")

    val pricesDataDF = spark.sql("select types from PriceBasedHouseRecs where hid = " + 1312 )
    val list : Array[Any] = pricesDataDF.collect().toArray
    val s : String = list(0).toString.replace("[","").replace("]","")
    println("s" + s)
    val priceDataDF = spark.sql("select hid from PriceBasedHouseRecs where types = " + s)

  }

}
