package com.rent.statistics

import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/14

*/
case class Houses(hid: Int, singleType: String, size: Double, directType: Int, layer: Int, rentType: Int, underPlace: String, schoolPlace: String , price: Int)

case class Browse(uid: Double, hid: Int, times: Double)

//基准的推荐对象
case class Recommendation(hid: Int, count: Double )
case class MongoConfig(uri: String, db:String)

case class GenresRecommendation( genres: String, recs: Seq[Recommendation] )

object StatisticsRecommender {
  val MONGO_HOUSES_COLLECTION = "Houses"
  val MONGO_BROWSE_COLLECTION = "Browse"

  val BROWSE_MORE_HOUSES = "BrowseMoreHouses"
  val AVERAGE_HOUSES = "AverageHouses"
  val AVERAGE_BROWSE = "AverageBrowse"
  val SCHOOLS_TOP_HOUSES = "SchoolsTopHouses"
  val SINGLETYPE_TOP_HOUSES = "SingleTypeTopHouses"
  val DIRECTTYPE_TOP_HOUSES = "DirectTopHouses"

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://localhost:27017/houserec",
      "mongo.db" -> "houserec"
    )

    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("statisticsRecommender")

    //创建sparkSsession
    val spark =SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._

    implicit val mongoConfig =MongoConfig(config("mongo.uri"),config("mongo.db"))

    //从MongoDB加载数据
    val browseDF = spark.read
      .option("uri",mongoConfig.uri)
      .option("collection",MONGO_BROWSE_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[Browse]
      .toDF()
    val housesDF = spark.read
        .option("uri",mongoConfig.uri)
        .option("collection",MONGO_HOUSES_COLLECTION)
        .format("com.mongodb.spark.sql")
        .load()
        .as[Houses]
        .toDF()

    //创建临时表
    browseDF.createOrReplaceTempView("browses")
    //1.存储的是浏览次数最多的房源
    val browseMoreHousesDF = spark.sql("select hid,count(hid) as count from browses group by hid")
    storeDFInMongoDB(browseMoreHousesDF,BROWSE_MORE_HOUSES)
//hid: Int, singleType: String, size: Double, directType: Int, layer: Int, rentType: Int, underPlace: String, schoolPlace: String , price: Int
    //val averageHousesDF = spark.sql("select hid,size/avg(size),directType,layer,rentType,price/avg(price) from Houses group by hid ")
    //storeDFInMongoDB(averageHousesDF,AVERAGE_HOUSES)

    //2.浏览次数最多的房子
    val averageBrowseDF = spark.sql("select hid,sum(times) as sum from browses group by hid")
 //   storeDFInMongoDB(averageBrowseDF,AVERAGE_BROWSE)

    //3.根据学校位置选择浏览次数最多的top10
    //定义所有学校

    //3.根据房子的房型来求出相似的房子
      val houseType = List("13室2厅","1室0厅","1室1厅","1室2厅","2室0厅","2室1厅","2室2厅","3室0厅","3室1厅",
        "3室2厅","3室3厅","4室0厅","4室1厅","4室2厅","4室3厅","4室4厅"
        ,"5室0厅","5室1厅","5室2厅","6室0厅","6室1厅","6室2厅","6室3厅","6室4厅","7室1厅","8室2厅","8室3厅",
        "9室3厅")

      val houseWithBrowse = housesDF.join(averageBrowseDF,"hid")
      val schoolGenresRDD =spark.sparkContext.makeRDD(houseType)

      val schoolTopHousesDF = schoolGenresRDD.cartesian(houseWithBrowse.rdd)
        .filter{
//条件过滤，
          case (school,houseRow) => houseRow.getAs[String]("singleType").equals(school)
        }
        .map{
            case(school,houseRow) => (school,(houseRow.getAs[Int]("hid"),houseRow.getAs[Long]("sum")))
        }
        .groupByKey()
        .map{
          case (genre, items) => GenresRecommendation( genre, items.toList.sortWith(_._2>_._2).take(50).map( item=> Recommendation(item._1, item._2)) )
        }
        .toDF()

    storeDFInMongoDB(schoolTopHousesDF,SINGLETYPE_TOP_HOUSES)

  }




  //将数据存储到MongoDB中
  def storeDFInMongoDB(df: DataFrame, collection_name: String)(implicit mongoConfig: MongoConfig): Unit = {
    df.write
      .option("uri",mongoConfig.uri)
      .option("collection",collection_name)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()
  }
}
