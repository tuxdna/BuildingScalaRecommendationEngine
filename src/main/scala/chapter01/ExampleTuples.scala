package chapter01

object ExampleTuples {
  def main(args: Array[String]) {
    val tuple1 = Tuple1(1)
    val tuple2 = ('a', 1) // can also be defined: ('a' -> 1)
    val tuple3 = ('a', 1, "name")

    // Access tuple members by underscore followed by 
    // member index starting with 1
    val a = tuple1._1 // res0: Int = 1
    val b = tuple2._2 // res1: Int = 1
    val c = tuple3._1 // res2: Char = a
    val d = tuple3._3 // res3: String = name
    
  }
}
