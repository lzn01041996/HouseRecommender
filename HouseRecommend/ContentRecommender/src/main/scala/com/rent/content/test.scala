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

    var arr  =new Array[Vector[Double]](5)
    var vector = Vector[Double]()
    vector ++= Vector(3.5)
    vector ++= Vector(3.5)
    vector ++= Vector(3.5)
    arr(0) = vector

    var vector1= Vector[Double]()
    vector1 ++= Vector(2.5)
    vector1 ++= Vector(2.5)
    vector1 ++= Vector(2.5)
    arr(1) = vector1

    var vector2 = Vector[Double]()
    vector2 ++= Vector(3.8)
    vector2 ++= Vector(3.8)
    vector2 ++= Vector(3.8)
    arr(2) = vector2

    var vector3 = Vector[Double]()
    vector3 ++= Vector(6.8)
    vector3 ++= Vector(6.8)
    vector3 ++= Vector(6.8)
    arr(3) = vector3

    var vector4 = Vector[Double]()
    vector4 ++= Vector(3.5)
    vector4 ++= Vector(3.5)
    vector4 ++= Vector(3.5)
    arr(4) = vector4

    val newArray = array ++ arr
    for (elem <- newArray) {
      println("elem : " + elem)
    }

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
