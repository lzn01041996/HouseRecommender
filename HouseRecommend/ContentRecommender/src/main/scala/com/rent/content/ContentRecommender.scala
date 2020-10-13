package com.rent.content


import org.apache.spark.SparkConf
import org.apache.spark.ml.feature.{HashingTF, IDF, Tokenizer}
import org.apache.spark.ml.linalg.SparseVector
import org.apache.spark.sql.SparkSession
import org.jblas.DoubleMatrix

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/9/1

*/
//房源信息
case class House(hid: Int, houseSingleUrl: String, title: String, singleType: String, size: String, directType: String, singleLayer: String, rentType: String, place: String, underPlace: String, schoolPlace: String , price: Int)

case class MongoConfig(uri: String,db:String)
//一个基准的推荐对象
case class Recommendation(hid: Int, count: Double)
//定义内容信息提取出的特征向量的房子相似度列表
case class HouseRecs(hid: Int, recs:Seq[Recommendation])

object ContentRecommender {

  //定义常量
  val MONGODB_HOUSE_COLLECTION = "House"

  val CONTENT_HOUSE_RECS = "ContentHouseRecs"

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

    //加载数据，并做预处理
    val houseBrowseDF = spark.read
      .option("uri",mongoConfig.uri)
      .option("collection",MONGODB_HOUSE_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[House]
      .map(
        // "hid" : 880, "houseSingleUrl" : "/rent/100251832?insource=rent_list&top=880", "title" : "元隆府邸 龙江 元隆 精装单室套 采光好 交通便利",
        // "singleType" : "1室1厅", "size" : "45㎡", "directType" : "精装", "singleLayer" : "中层(共7层)", "rentType" : "整租",
        // "place" : "鼓楼-草场门大街-元隆府邸", "underPlace" : "空房", "schoolPlace" : "南京市银城小学", "price" : 3000
        //提取hid，singleType,size,singleLayer,place,price三项作为原始的内容特征，分词器默认按照空格做分词
        x => (x.hid,x.singleType,x.size,x.singleLayer,x.place.map(c=> if(c=='-') ' ' else c),x.price)
      )
      .toDF("hid","singleType","size","singleLayer","place","price")
      .cache()

    //核心部分，用TF-IDF从内容信息中提取房子特征向量
    //创建一个分词器，默认按照空格分词，对原始数据做转换，生成新的一列words
    val tokenizer = new Tokenizer().setInputCol("place").setOutputCol("words")

    val wordsData = tokenizer.transform(houseBrowseDF)

    //引入hashingTF工具，可以把一个词语序列转化为对应的词频
    val hashingTF = new HashingTF().setInputCol("words").setOutputCol("rawFeatures").setNumFeatures(50)
    val featurizedData = hashingTF.transform(wordsData)
    //featurizedData.collect().foreach(println)

    //引入IDF工具，可以得到idf模型
    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    //训练idf模型，得到每个词的逆文档频率
    val idfModel = idf.fit(featurizedData)
    //用模型对算数据进行处理，得到文档中每个词的tf-idf,作为新的特征向量
    val rescaledData = idfModel.transform(featurizedData)
    rescaledData.collect().foreach(println)

    val houseFeatures = rescaledData.map(
      row => (row.getAs[Int]("hid"),row.getAs[SparseVector]("features").toArray)
    )
      .rdd
      .map(
        x => (x._1,new DoubleMatrix(x._2))
      )
 //   houseFeatures.collect().foreach(println)
    //对所有的房子两两计算他们的相似度，先做笛卡尔积
    val houseRecs = houseFeatures.cartesian(houseFeatures)
      .filter{
        //把自己的过滤掉
        case(a,b) => a._1 != b._1
      }
      .map{
        case (a,b) => {
          val simScore = this.consinSim(a._2,b._2)
          (a._1,(b._1,simScore))
        }
      }
      .filter(_._2._2 > 0.5)
      .groupByKey()
      .map{
        case(hid,items) => HouseRecs(hid,items.toList.sortWith(_._2>_._2).map(x => Recommendation(x._1,x._2)))
      }
      .toDF()

    houseRecs.write
      .option("uri",mongoConfig.uri)
      .option("collection",CONTENT_HOUSE_RECS)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

    spark.stop()
  }

  //求向量余弦相似度
  def consinSim(house1: DoubleMatrix,house2: DoubleMatrix): Double = {
    house1.dot(house2) / (house1.norm2() * house2.norm2())
  }
}
