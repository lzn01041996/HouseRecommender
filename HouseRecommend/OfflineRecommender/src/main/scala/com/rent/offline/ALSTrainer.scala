package com.rent.offline

import breeze.numerics.sqrt
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import com.rent.offline.OfflineRecommender.MONGO_BROWSE_COLLECTION
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/17

*/
object ALSTrainer {
  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://localhost:27017/houserec",
      "mongo.db" -> "houserec"
    )

    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("OfflineRecommender")

    val spark = SparkSession.builder().config(sparkConf).getOrCreate()
    import spark.implicits._

    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    val browseRDD = spark.read
      .option("uri",mongoConfig.uri)
      .option("collection",MONGO_BROWSE_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[Browse]
      .rdd
      .map( rating => Rating(rating.uid,rating.hid,rating.times))
      .cache()

    //随机切分数据集
    val splits = browseRDD.randomSplit(Array(0.8,0.2))
    val trainingRDD = splits(0)
    val testRDD = splits(1)
    // 模型参数选择，输出最优参数
    adjustALSParam(trainingRDD, testRDD)

    spark.close()

  }

  def adjustALSParam(trainData: RDD[Rating], testData: RDD[Rating]): Unit ={
    val result = for (rank <- Array(50,100,200,300); lambda <- Array(0.01,0.1,1))
      yield {
        val model = ALS.train(trainData,rank,5,lambda)
        val rmse = getRMSE(model,testData)
        (rank,lambda,rmse)
      }
    println(result.minBy(_._3))
  }

  def getRMSE(model: MatrixFactorizationModel,data:RDD[Rating]):Double = {
    val userProducts = data.map(item => (item.user,item.product))
    val predictBrowse = model.predict(userProducts)

    val observed = data.map(item => ((item.user,item.product),item.rating))
    val predict = predictBrowse.map(item =>((item.user,item.product),item.rating))

    sqrt{
      observed.join(predict).map{
        case ((uid,hid),(actual,pre)) =>
          val err = actual - pre
          err * err
      }.mean()
    }
  }

}
