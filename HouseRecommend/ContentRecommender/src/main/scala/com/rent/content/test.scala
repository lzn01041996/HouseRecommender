package com.rent.content

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/10/12

*/
object test {
  def main(args: Array[String]): Unit = {
    var array = new Array[Double](5)
    array(0) = 3.0
    array(1) = 8.0
    array(2) = 13.0
    array(3) = 18.0
    array(4) = 25.0
    val a:Double = 2.0
    println(closestCenter(array,a))

    var sum = array.reduceLeft((a,b) => a + b)
    println(sum)

  }

  def closestCenter(array:Array[Double],v: Double):Double = {
    array.reduceLeft((c1,c2) =>
      if (vectorDis(c1,v) < vectorDis(c2,v)) c1 else c2)
  }


  def vectorDis(v1:Double,v2:Double):Double ={
    var distance = 0.0

    distance = (v1- v2) * (v1 - v2)

    distance = math.sqrt(distance)
    distance
  }

}
