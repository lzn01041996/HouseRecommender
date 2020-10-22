package com.rent.content

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/10/12

*/

import java.util

import scala.collection.mutable
import scala.util.Random
import scala.util.control.Breaks._


class KmeansPP {
//随机初始化中心点
  def initialCenters(points:Array[Vector[Double]],centers:Array[Vector[Double]]) = {
    val pointsNum = points.length
    var index = 0
    var flag = true
    var temp = 0
    var array = new util.LinkedList[Int]()
    //这里的k代表的是9个聚类的中心
    while (index < 9){
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
    for (i <- 0 to centers.length - 1 ){
      centers(i) = points(array.get(i))
    }
  }

  //通过kmeans++来对初始化的中心点进行优化
  def kmeansppInitial(points:Array[Vector[Double]],centers:Array[Vector[Double]]) = {
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
    while(kSum < 9 ){
      pointsAndDist = points.map(v => //计算每个样本点与它所属的聚类中心的距离
        vectorDis(v,closestCenter(updatedCenters.toArray,v,centers))
      )
      sum = pointsAndDist.reduceLeft((a,b) => a + b)
      println("sum=="+ sum)
      flag = true

      /*1.输入的数据点集合中随机选择一个点作为第一个聚类中心
      * 2.重复选取
      * 3.对于数据集中的每个样本点x，计算它与最近聚类中心的距离
      * 4.选择一个新的数据点作为下一个聚类中心，选择的选择是D（x)较大的点，作为聚类中心的概率较大，
      * 5.直到k个聚类中心被选出来
      * 6.利用这k个初始的聚类中心来运行标准的k-means算法
      * 如何将D（x)反映到点被选择的概率的一种算法：
      * 随机从点集中选择一个点作为初始中心点
      * 计算每一个点到最近中心点的距离，对所有的距离求和
      * 再取一个随机值，用权重的方式计算下一个种子数，取随机值，对点集循环做减法，直到那个随机数小于0
      * 直到k个聚类中心被选出来*/
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
    for(i <- 0 to updatedCenters.length - 1){
      centers(i) = updatedCenters(i)
    }
  }

  //聚类算法
  def kmeans(points:Array[Vector[Double]],centers:Array[Vector[Double]]) = {
    var bool = true
    var newCenters = Array[Vector[Double]]()
    var move = 0.0
    var currentCost = 0.0 //当前代价函数值
    var newCost = 0.0
    //根据每个样本最近的聚类中心进行groupBy分组，最后得到的cluster是Map
    //map中的keyJ就是聚类的中心，value就是依赖于该聚类中心的点集
    while (bool){0
      move = 0.0
      currentCost = computeCost(points,centers)
      val cluster = points.groupBy(v => closestCenter(centers,v,centers))
      newCenters =
        centers.map(oldCenter => {
          cluster.get(oldCenter) match {
            //找到该聚类中心所拥有的点集
            case Some(pointsInThisCluster) =>
              //均作为新的聚类中心
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
      if (math.sqrt(vectorDis(Vector(currentCost),Vector(newCost))) < 0.0000000001)
        bool = false
    }
  }

  def computeCost(points:Array[Vector[Double]],centers:Array[Vector[Double]]):Double = {
    val cluster = points.groupBy( v => closestCenter(centers,v,centers))
    var costSum = 0.0
    for (i <- 0 to centers.length -1 ){
      cluster.get(centers(i)) match{
        case Some(subSets) =>
          for (j <- 0 to subSets.length - 1){
            costSum += (vectorDis(centers(i),subSets(j)) * vectorDis(centers(i),subSets(j)))
          }
        case None => costSum = costSum
      }
    }
    costSum
    }

  def vectorDivide(v:Vector[Double],num:Int):Vector[Double] = {
    var r = v
    for (i <- 0 to v.length - 1){
      r = r.updated(i,r(i) / num)
    }
    r
  }

  def vectorAdd(v1:Vector[Double],v2:Vector[Double]) = {
    var v3 = v1
    for (i <- 0 to v1.length - 1){
      v3 = v3.updated(i,v1(i) + v2(i))
    }
    v3
  }

  def vectorDis(v1:Vector[Double],v2:Vector[Double]):Double ={
    var distance = 0.0
    for (i <- 0 to v1.length - 1){
      distance += (v1(i) - v2(i)) * (v1(i) - v2(i))
    }
    distance = math.sqrt(distance)
    distance
  }

  def closestCenter(array:Array[Vector[Double]],v: Vector[Double],centers:Array[Vector[Double]]):Vector[Double] = {
    centers.reduceLeft((c1,c2) =>
    if (vectorDis(c1,v) < vectorDis(c2,v)) c1 else c2)
  }
}
