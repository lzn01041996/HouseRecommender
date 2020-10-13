package com.rent.offline

import org.apache.spark.SparkConf
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.sql.SparkSession
import org.jblas.DoubleMatrix

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/17

*/
case class MongoConfig(uri: String, db:String)

case class Recommendation(hid: Int, count: Double )
case class Browse(uid: Int, hid: Int, times: Int)
case class Houses(hid: Int, singleType: String, size: Double, directType: Int, layer: Int, rentType: Int, underPlace: String, schoolPlace: String , price: Int)
//case class HouseBrowse(uid: Int,hid: Int, count: Long)

case class UserRecs(uid: Int, recs: Seq[Recommendation])

case class HouseRecs(hid: Int, recs: Seq[Recommendation])
object OfflineRecommender {

  val MONGO_BROWSE_COLLECTION = "Browse"

  val USER_RECS = "UserRecs";
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
      .map(rating =>(rating.uid,rating.hid,rating.times))
      .cache()

    val userRDD = browseRDD.map(_._1).distinct()
    val houseRDD = browseRDD.map(_._2).distinct()
//训练隐语义模型
    val trainData = browseRDD.map( x=> Rating(x._1,x._2,x._3))

    val(rank,iterations,lambda) = (200,5,0.1)
    val model = ALS.train(trainData,rank,iterations,lambda)

    val userHouses = userRDD.cartesian(houseRDD)

    val preBrowses = model.predict(userHouses)

    val userRecs = preBrowses
      .filter(_.rating > 0)
      .map(rating => (rating.user,(rating.product,rating.rating)))
      .groupByKey()
      .map{
        case(uid,recs) => UserRecs(uid,recs.toList.sortWith(_._2>_._2).take(USER_MAX_RECOMMENDATION).map(x=>Recommendation(x._1,x._2)))
      }
      .toDF()

    userRecs.write
      .option("uri",mongoConfig.uri)
      .option("collection", USER_RECS)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

   //基于房子的隐特征，计算相似度矩阵，得到房子的相似度列表
    val houseFeatures = model.productFeatures.map{
      case (hid,features) => (hid, new DoubleMatrix(features))
    }

    //对所有房子计算它们的相似度，先做笛卡尔积
    val houseRecs = houseFeatures.cartesian(houseFeatures)
        .filter{
            //过滤掉自己和自己
        case (a,b) => a._1 != b._1
      }
      .map{
        case (a,b) => {
          val simScore = this.consinSim(a._2,b._2)
          (a._1,(b._1,simScore))
        }
      }
      .filter(_._2._2 >= 0.5)
      .groupByKey()
      .map{
        case (hid,items) => HouseRecs(hid,items.toList.sortWith(_._2>_._2).map(x => Recommendation(x._1,x._2)))
      }
      .toDF()

    houseRecs.write
      .option("uri",mongoConfig.uri)
      .option("collection",HOUSE_RECS)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

    spark.stop()




  }

  // 求向量余弦相似度
  def consinSim(movie1: DoubleMatrix, movie2: DoubleMatrix):Double ={
    movie1.dot(movie2) / ( movie1.norm2() * movie2.norm2() )
  }

}
