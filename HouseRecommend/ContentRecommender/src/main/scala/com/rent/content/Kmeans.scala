package com.rent.content

import java.util

import scala.collection.mutable
import scala.io.Source
import scala.util.Random
import scala.util.control.Breaks._


/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/10/9

*/
object Kmeans {

  val k = 3
  val dim = 3 //数据集的维度
  val shold = 0.0000000001//阈值。用于判断聚类中心偏移量
  val centers = new Array[Vector[Double]](k) //聚类中心点（迭代更新）

  def main(args: Array[String]): Unit = {
    //处理输入数据
    val fileName = "D:\\IntelliJ IDEA-workspace\\HouseRent\\HouseRecommend\\ContentRecommender\\src\\main\\resources\\data.csv"
    val lines = Source.fromFile(fileName).getLines()
    val points = lines.map(line => {
      //数据预处理
      val parts = line.split(" ").map(_.toDouble)
      var vector = Vector[Double]()
      for (i <- 0 to dim - 1)
        vector ++= Vector(parts(i))
      vector
    }).toArray

    initialCenters(points)
    kmeansppInitial(points)
    kmeans(points,centers)
    printResult(points,centers)

  }

  //---------------------------随机初始化聚类中心---------------------------------
  def initialCenters(points:Array[Vector[Double]]) ={
    val pointsNum = points.length //数据集的个数
    var index = 0
    var flag = true
    var temp = 0
    var array = new util.LinkedList[Int]() //保存随机下标号
    while(index < k){
      temp = new Random().nextInt(pointsNum)
      flag = true
      if (array.contains(temp)){
        flag = false
      }else{
        if (flag){
          array.add(temp)
          index += 1
        }
      }
    }
    for (i <- 0 to centers.length - 1){
      centers(i) = points(array.get(i))
      println("初始化的中心点: ")
      println(array.get(i))
      println(centers(i))
    }
  }

  //-------------------------------------随机初始化聚类中心---------------------------------------------------
  def initialCenters1(points:Array[Vector[Double]]) = {
    val pointsNum = points.length//数据集个数
    //寻找k个随机数(作为数据集的下标)
    val random  = new Random()
    var index = 0
    var flag = true
    var temp = 0
    var array = new mutable.LinkedList[Int]()//保存随机下标号
    while(index < k ){
      temp = new Random().nextInt(pointsNum)
      flag = true
      if (array.contains(temp)){//在数组中存在
        flag = false
      }
      else {
        if (flag){
          array = array :+ temp
          index+=1
        }
      }//else-end
    }//while-end
    for(i <- 0 to centers.length - 1){
      centers(i) = points(array(i))
      println("初始化中心点如下：")
      println(array(i))
      println(centers(i))
    }
  }

  //---------------------------k-means++初始化聚类中心-------------------------------------
  def kmeansppInitial(points:Array[Vector[Double]]) = {
    val pointsNum = points.length//数据集个数
    val random  = new Random()
    var kSum = 1
    var flag = true
    var temp = random.nextInt(pointsNum)//选择第一个随机数（下标）
    var array = new mutable.LinkedList[Int]()//保存随机下标号
    var updatedCenters = new mutable.LinkedList[Vector[Double]]()//迭代添加元素的聚类中心数组
    var sum = 0.0
    var randomSeed = 0.0
    var pointsAndDist = Array[Double]()//保存每个样本点对应到各自聚类中心的距离
    var j = 0
    array = array :+ temp
    updatedCenters = updatedCenters :+ points(temp)//将随机选择的点作为第一个聚类中心
    while(kSum < k ){
      pointsAndDist = points.map(v => //计算每个样本点与它所属的聚类中心的距离
        vectorDis(v,closestCenter(updatedCenters.toArray,v))
      )
      sum = pointsAndDist.reduceLeft((a,b) => a + b)
      println("sum=="+ sum)
      flag = true
      while(flag){
        randomSeed = sum * (random.nextInt(100) + 1) / 100
        breakable{
          for(i <- 0 to pointsAndDist.length - 1){
            randomSeed -= pointsAndDist(i)
            if(randomSeed < 0){
              j = i
              break
            }
          }
        }
        if(array.contains(j)){//求得的新中心点的下标在数组中存在
          flag= true
        }else{
          array = array :+ j
          updatedCenters = updatedCenters :+ points(j)
          flag = false
          kSum += 1
        }
      }

    }//while-end
    println("kmean++初始化中心点如下：")
    for(i <- 0 to updatedCenters.length - 1){
      centers(i) = updatedCenters(i)
      println(array(i))
      println(centers(i))
    }
  }


  //-------------------------------------------迭代聚类----------------------
  def kmeans(points:Array[Vector[Double]],centers:Array[Vector[Double]]) = {
    var bool = true
    var newCenters = Array[Vector[Double]]()
    var move = 0.0
    var currentCost = 0.0 //当前代价函数值
    var newCost = 0.0
    //根据每个样本最近的聚类中心进行groupBy分组，最后得到的cluster是Map
    //map中的keyJ就是聚类的中心，value就是依赖于该聚类中心的点集
    while (bool){
      move = 0.0
      currentCost = computeCost(points,centers)
      val cluster = points.groupBy(v => closestCenter(centers,v))
      println("cluster: ")
      cluster.foreach(println)
      println("****************************************")
      println("改变聚类中心的点开始：")

      newCenters =
        centers.map(oldCenter => {
          println("oldCenter: " + oldCenter)
          println(cluster.get(oldCenter).foreach(println))
          cluster.get(oldCenter) match {
            //找到该聚类中心所拥有的点集
            case Some(pointsInThisCluster) =>
              //均作为新的聚类中心
              println("pointsInThisCluster: ")
              println("++++++++++++++++++++++++++")
              pointsInThisCluster.foreach(println)
              println("++++++++++++++++++++++++++")
              vectorDivide(pointsInThisCluster.reduceLeft((v1,v2) => vectorAdd(v1,v2)),pointsInThisCluster.length)
            case None => oldCenter
          }
        })
      for (i <-  0 to centers.length - 1){
        centers(i) = newCenters(i)
      }

      newCost = computeCost(points,centers)//新的代价函数值
      println("当前的代价函数值：" + currentCost)
      println("新的代价函数值：" + newCost)
      if (math.sqrt(vectorDis(Vector(currentCost),Vector(newCost))) < shold)
        bool = false
    }


    println("寻找的最优中心点如下：")
    for (i <- 0 to centers.length - 1){
      println(centers(i))
    }

  }

  //-------------------------------------------找到某样本点所属的聚类中心---------------
  def closestCenter(array: Array[Vector[Double]], v: Vector[Double]):Vector[Double] = {
    centers.reduceLeft((c1,c2) =>
      if (vectorDis(c1,v) < vectorDis(c2,v)) c1 else c2
    )
  }

  //--------------------------输出聚类结果-----------------------------
  def printResult(points:Array[Vector[Double]],centers:Array[Vector[Double]]) = {
    //将每个点的聚类中心用centers中的下标表示，属于同一类的点拥有相同的下标
    val pointsNum = points.length
    val pointsLabel = new Array[Int](pointsNum)
    var closetCenter = Vector[Double]()
    println("聚类结果如下：")
    for(i <- 0 to pointsNum - 1){
      closetCenter = centers.reduceLeft((c1,c2) => if (vectorDis(c1,points(i)) < vectorDis(c2,points(i))) c1 else c2)
      pointsLabel(i) = centers.indexOf(closetCenter)
      println(points(i) + "-----------" + pointsLabel(i))
    }

  }

  //--------------------------------------------计算代价函数---------------------------
  def computeCost(points:Array[Vector[Double]],centers:Array[Vector[Double]]):Double = {
    val cluster = points.groupBy(v => closestCenter(centers,v))
    var costSum = 0.0
    for (i <- 0 to centers.length - 1){
      cluster.get(centers(i)) match{
        case Some(subSets) =>
          for (j <- 0 to subSets.length -1){
            costSum += (vectorDis(centers(i),subSets(j)) * vectorDis(centers(i),subSets(j)))
          }
        case None => costSum = costSum
      }
    }
    costSum
  }

  //--------------------------------------------向量间的欧式距离-----------------------------
  def vectorDis(v1: Vector[Double], v2: Vector[Double]):Double = {
    var distance = 0.0
    for (i <- 0 to v1.length - 1){
      distance += (v1(i) - v2(i)) * (v1(i) - v2(i))
    }
    distance = math.sqrt(distance)
    distance
  }

  //--------------------------向量加法-----------------------------
  def vectorAdd(v1:Vector[Double],v2:Vector[Double]):Vector[Double] = {
    var v3 = v1
    for(i <- 0 to v1.length - 1){
      v3 = v3.updated(i,v1(i) + v2(i))
    }
    v3
  }
  //--------------------------向量除法-----------------------------
  def vectorDivide(v:Vector[Double],num:Int):Vector[Double] = {
    var r = v
    for(i <- 0 to v.length - 1){
      r = r.updated(i,r(i) / num)
    }
    println("r: " + r)
    r
  }

}
