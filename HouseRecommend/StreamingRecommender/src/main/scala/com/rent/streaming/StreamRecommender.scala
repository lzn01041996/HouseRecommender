package com.rent.streaming

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import redis.clients.jedis.Jedis

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/19

*/
// 定义连接助手对象，序列化
object ConnHelper extends Serializable{
  lazy val jedis = new Jedis("localhost")
  lazy val mongoClient = MongoClient( MongoClientURI("mongodb://localhost:27017/houserec") )
}

case class MongoConfig(uri:String, db:String)
//基准的推荐对象
case class Recommendation(hid: Int, count: Double)
//基于预测用户可能会喜欢的房子的列表
case class UserRecs(uid: Int, recs:Seq[Recommendation])
//基于LFM的房子之间的相似推荐
case class HouseRecs(hid: Int, recs:Seq[Recommendation])

case class HouseType(hid:Int,size:Double,layer:Double,price:Double,types:Double)

object StreamRecommender {

  val MAX_USER_BROWSE_NUM = 20
  val MAX_SIM_HOUSE_NUM = 20
  val MONGODB_STREAM_REC_COLLECTION = "StreamRecs"
  val MONGODB_BROWSE_COLLECTION = "Browse"
  val MONGODB_HOUSE_RECS_COLLECTION = "HouseRecs"
  val HOUSE_PRICE_RECS = "PriceBasedHouseRecs"


  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://localhost:27017/houserec",
      "mongo.db" -> "houserec",
      "kafka.topic" -> "houserec"
    )

    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("StreamRecommender")

    // 创建一个SparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    // 拿到streaming context
    val sc = spark.sparkContext
    val ssc = new StreamingContext(sc, Seconds(2))

    import spark.implicits._

    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    val priceDF = spark.read
      .option("uri",mongoConfig.uri)
      .option("collection",HOUSE_PRICE_RECS)
      .format("com.mongodb.spark.sql")
      .load()
      .as[HouseType]
      .toDF()

    priceDF.createOrReplaceTempView("PriceBasedHouseRecs")

    val simHouseMatrix = spark.read
      .option("uri",mongoConfig.uri)
      .option("collection",MONGODB_HOUSE_RECS_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[HouseRecs]
      .rdd
      .map{
        houseRecs => (houseRecs.hid,houseRecs.recs.map(x=>(x.hid,x.count)).toMap)
      }.collectAsMap()

    val simHouseMatrixBroadCast = sc.broadcast(simHouseMatrix)

    val kafkaParam = Map(
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "houserec",
      "auto.offset.reset" -> "latest"
    )
    //通过kafka创建一个DStream
    val kafkaStream = KafkaUtils.createDirectStream[String,String](ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String,String](Array(config("kafka.topic")),kafkaParam))
    //把原始数据uid/hid/browse转换成数据流
    val browseStream = kafkaStream.map{
      msg =>
        val attr = msg.value().split("\\|")
        (attr(0).toInt,attr(1).toInt,attr(2).toDouble)
    }

    //流式数据处理，核心实时算法
    browseStream.foreachRDD{
      rdds => rdds.foreach{
        case(uid,hid,browse) =>{
          println("浏览数据出现!>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
          //1.从redis里获取当前用户最近的浏览的房子的浏览量
          val userRecentlyBrowse = getUserRecentlyBrowse(MAX_USER_BROWSE_NUM,uid,ConnHelper.jedis)
          //3.根据房子的价格做聚类分析
          println(hid)
          val sparks = SparkSession.builder().config(sparkConf).getOrCreate()
          val priceDataDF = getPriceSimis(hid,sparks)
          //2. 从相似度矩阵中取出当前房源中最相似的N个房子，作为备选列表
          val candidateHouses = getTopSimHouses(MAX_SIM_HOUSE_NUM,hid,uid,simHouseMatrixBroadCast.value,priceDataDF.collect().toArray)
          //4.对每个备选的房源，计算推荐的优先级，得到当前用户的实时推荐列表
          val streamRecs = computeHouseBrowses(candidateHouses,userRecentlyBrowse,simHouseMatrixBroadCast.value)
          //5.把推荐的数据保存到MongoDB中
          saveDataToMongoDB(uid,streamRecs)
        }
      }
    }
    //开始接受数据
    ssc.start()

    println(">>>>>>>>>>>>>>>>>>>>>>>>>>>流式处理新进来的数据!")

    ssc.awaitTermination()
  }

  //redis操作返回的是java类，为了用map需要引入转换类
  import scala.collection.JavaConversions._


  def getPriceSimis(hid:Int,sparkSession: SparkSession): DataFrame = {
    val pricesDataDF = sparkSession.sql("select types from PriceBasedHouseRecs where hid = " + hid )
    val list : Array[Any] = pricesDataDF.collect().toArray
    val s : String = list(0).toString.replace("[","").replace("]","")
    println("s" + s)
    val priceDataDF = sparkSession.sql("select hid from PriceBasedHouseRecs where types = " + s)
    priceDataDF
  }

  def getUserRecentlyBrowse(num: Int, uid: Int, jedis: Jedis): Array[(Int, Double)] = {
    //从redis读取数据，用户的浏览数据保存在uid:UID 为key的队列里，value是HID:BROWSE
    jedis.lrange("uid:" + uid, 0, num-1)
      .map{
        item => //具体的每个浏览量可以用冒号分隔的两个值
          val attr = item.split("\\:")
          (attr(0).trim.toInt,attr(1).trim.toDouble)
      }.toArray
  }


  //获取跟当前的房源做相似的num个房子，作为备选房源
  def getTopSimHouses(num: Int, hid: Int, uid: Int, simHouses: scala.collection.Map[Int, scala.collection.immutable.Map[Int, Double]],priceSimi:Array[Any]
                     )
    (implicit mongoConfig: MongoConfig): Array[Int] ={
    //1.从相似矩阵中拿到所有相似的房源
    var allSimHouses = simHouses(hid).toArray
    //2.从mongodb中查询用户已经浏览过的房源
    val browseExist = ConnHelper.mongoClient(mongoConfig.db)(MONGODB_BROWSE_COLLECTION)
      .find(MongoDBObject("uid" -> uid))
      .toArray
      .map{
        item => item.get("hid")
      }
    //3.把看过的过滤，得到输出列表
    val res = allSimHouses.filter( x=> ! browseExist.contains(x._1)).filter( x => !priceSimi.contains(x._1))
      .sortWith(_._2>_._2)
      .take(num)
      .map( x=>x._1)
    res ++ priceSimi
    res
  }

  def computeHouseBrowses(candidateHouses: Array[Int],
                          userRecentlyBrowses: Array[(Int,Double)],
                          simHouses: scala.collection.Map[Int, scala.collection.immutable.Map[Int,Double]]): Array[(Int,Double)] ={
    //定义一个ArrayBuffer，用于保存每一个备选房源的基础浏览量
    val browses = scala.collection.mutable.ArrayBuffer[(Int,Double)]()
    //定义一个HashMap，保存每一个备选房源的增强减弱因子
    val increMap = scala.collection.mutable.HashMap[Int,Int]()
    val decreMap = scala.collection.mutable.HashMap[Int,Int]()

    for ( candidateHouses <- candidateHouses; userRecentlyBrowses <- userRecentlyBrowses){
      //拿到备选房源和最近浏览的房源的相似度
      val simScore = gethousesSimScore(candidateHouses,userRecentlyBrowses._1,simHouses)

      if (simScore > 0.5){
        //计算备选房源的基础推荐得分
        browses += ((candidateHouses, simScore*userRecentlyBrowses._2))
        if (userRecentlyBrowses._2 > 3){
          increMap(candidateHouses) = increMap.getOrDefault(candidateHouses,0) + 1
        }else{
          decreMap(candidateHouses) = decreMap.getOrDefault(candidateHouses,0) + 1
        }
      }
    }
    //根据备选房源的hid做groupby，根据公式去求最后的推荐评分
    browses.groupBy(_._1).map{
      //groupby得到的数据Map
      case (hid,scoreList) =>
        (hid,scoreList.map(_._2).sum / scoreList.length + log(increMap.getOrDefault(hid,1)) - log(decreMap.getOrDefault(hid,1)))
    }.toArray.sortWith(_._2>_._2)
  }

  //获取两个房源之间的相似度
  def gethousesSimScore(hid1:Int,hid2: Int, simHouses: scala.collection.Map[Int, scala.collection.immutable.Map[Int,Double]]): Double ={
     simHouses.get(hid1) match {
       case Some(sims) => sims.get(hid2) match {
         case  Some(score) => score
         case None =>0.0
       }
       case None => 0.0
     }
  }

  //求一个数的对数，利用换底公式
  def log(m: Int):Double ={
    val N = 10
    math.log(m)/math.log(N)
  }

  //将数据存储到MongoDB
  def saveDataToMongoDB(uid: Int, streamRecs: Array[(Int,Double)])(implicit mongoConfig: MongoConfig): Unit ={
    //定义到streamRecs表的连接
    val streamingCollection = ConnHelper.mongoClient(mongoConfig.db)(MONGODB_STREAM_REC_COLLECTION)
    //如果表中已有uid对应的数据，则删除
    streamingCollection.findAndRemove(MongoDBObject("uid" -> uid))
    //将streamRecs数据存入到表中
    streamingCollection.insert(MongoDBObject("uid" -> uid,
    "recs" -> streamRecs.map(x=>MongoDBObject("hid"->x._1,"count"-> x._2))))
  }

}
