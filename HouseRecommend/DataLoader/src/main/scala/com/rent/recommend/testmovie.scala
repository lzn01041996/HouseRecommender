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
//case class Houses(hid: Int, singleType: String, size: Double, directType: Int, layer: Int, rentType: Int, underPlace: String, schoolPlace: String , price: Int)
//https://saas-qw1.qfangimg.com/pro/f3d6e3d9-d45f-455b-a889-189758c1bfa7.JPG-240x180	/rent/100239581?insource=rent_list&top=1	亚东名座 亚东名座电梯房新街口精装	1室1厅	47.68㎡	精装	中层(共27层)	整租	秦淮-升州路-亚东名座	距离1号线张府园站约382米有家私随时看房	南京市府西街小学
//case class House(hid: Int, houseSingleUrl: String, title: String, singleType: String, size: Double, directType: Int, singleLayer: Int, rentType: Int, place: String, underPlace: String, schoolPlace: String , price: Int)
//case class MongoConfig(uri:String, db:String)
//case class ESConfig(httpHosts:String, transportHosts:String, index:String, clustername:String)
//case class Browse(uid: Int, hid: Int, times: Int)
object testmovie {
  val MOVIE_DATA_PATH = "D:\\IntelliJ IDEA-workspace\\HouseRent\\HouseRecommend\\DataLoader\\src\\main\\resources\\logs.csv"
  val BROSWE_DATA_PATH = "D:\\IntelliJ IDEA-workspace\\HouseRent\\HouseRecommend\\DataLoader\\src\\main\\resources\\count.csv"
  val MONGODB_MOVIE_COLLECTION = "House"
  val MONGODB_HOUSES_COLLECTION = "Houses"
  val MONGODB_BROWSE_COLLECTION = "Browse"
  val ES_MOVIE_INDEX = "House"

  def main(args: Array[String]): Unit = {

    /*val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://localhost:27017/houserec",
      "mongo.db" -> "houserec",
      "es.httpHosts" -> "localhost:9200",
      "es.transportHosts" -> "localhost:9300",
      "es.index" -> "houserec",
      "es.cluster.name" -> "elasticsearch"
    )

    // 创建一个sparkConf
    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("DataLoader")

    // 创建一个SparkSession
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._

    // 加载数据
    val movieRDD = spark.sparkContext.textFile(MOVIE_DATA_PATH)
    val movieDF = movieRDD.map(
      f = item => {
        println("++++++++++++++++++++++++++++++++")
        val attr = item.split("\\^")
        val id = attr(1).split("top=")(1)
        val size = attr(4).replace("㎡","")
        var directType = -1
        if (attr(5).contains("精")){
          directType = 2
        }else if (attr(5).contains("简")){
          directType = 1
        }else{
          directType = 0
        }
        var layer = -1
        if (attr(6).contains("低")){
          layer = 2
        }else if (attr(6).contains("中")){
          layer = 1
        }else{
          layer = 0
        }
        var rent = -1
        if(attr(7).contains("整")){
          rent = 1
        }else{
          rent = 0
        }
        printf("id : %s",id)
        println()
        //hid: Int, singleType: String, size: Double, directType: Int, layer: Int, isUnder: Int, isSchool: Int, price: Double
        //hid: Int, houseSingleUrl: String, title: String, singleType: String, size: Double, directType: Int, singleLayer: Int, rentType: Int, place: String, underPlace: String, schoolPlace: String , price: Int
        Houses(id.toInt, attr(3).trim, size.toDouble, directType, layer, rent,  attr(9).trim, attr(10).trim, attr(11).toInt)
      }
    ).toDF()
    val browseRDD = spark.sparkContext.textFile(BROSWE_DATA_PATH)
    val browseDF = browseRDD.map(
      f = item => {
        val attr = item.split(",")
        Browse(attr(0).toInt,attr(1).toInt,attr(2).toInt)
      }
    ).toDF()

    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    // 将数据保存到MongoDB
    storeDataInMongoDB(movieDF)
    // 声明了一个 ES 配置的隐式参数
    implicit val esConfig = ESConfig(config("es.httpHosts"), config("es.transportHosts"), config("es.index"), config("es.cluster.name"))
//    val houseDF = spark.sql("select hid,")


    // 关闭 Spark
    spark.stop()

  }
  def storeDataInMongoDB(browseDF: DataFrame)(implicit mongoConfig: MongoConfig): Unit ={
    // 新建一个mongodb的连接
    val mongoClient = MongoClient(MongoClientURI(mongoConfig.uri))

    // 如果mongodb中已经有相应的数据库，先删除
    mongoClient(mongoConfig.db)(MONGODB_HOUSES_COLLECTION).dropCollection()


    // 将DF数据写入对应的mongodb表中
    browseDF.write
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_HOUSES_COLLECTION)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

    //对数据表建索引
    mongoClient(mongoConfig.db)(MONGODB_HOUSES_COLLECTION).createIndex(MongoDBObject("uid" -> 1))


    mongoClient.close()

  }
  def storeDataInES(movieDF:DataFrame)(implicit eSConfig: ESConfig): Unit = {
    //新建一个配置
    val settings:Settings = Settings.builder()
      .put("cluster.name",eSConfig.clustername).build()
    //新建一个 ES 的客户端
    val esClient = new PreBuiltTransportClient(settings)
    //需要将 TransportHosts 添加到 esClient 中
    val REGEX_HOST_PORT = "(.+):(\\d+)".r
    eSConfig.transportHosts.split(",").foreach{
      case REGEX_HOST_PORT(host:String,port:String) => {
        esClient.addTransportAddress(new
            InetSocketTransportAddress(InetAddress.getByName(host),port.toInt))
      }
    }
    //需要清除掉 ES 中遗留的数据
    if(esClient.admin().indices().exists(new
        IndicesExistsRequest(eSConfig.index)).actionGet().isExists){
      esClient.admin().indices().delete(new DeleteIndexRequest(eSConfig.index))
    }
    esClient.admin().indices().create(new CreateIndexRequest(eSConfig.index))
    //将数据写入到 ES 中
    movieDF
      .write
      .option("es.nodes",eSConfig.httpHosts)
      .option("es.http.timeout","100m")
      .option("es.mapping.id","mid")
      .mode("overwrite")
      .format("org.elasticsearch.spark.sql")
      .save(eSConfig.index+"/"+ES_MOVIE_INDEX)
  }*/
  }
}
