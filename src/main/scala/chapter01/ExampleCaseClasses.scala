package chapter01

import scala.util.Random

object ExampleCaseClasses {

  case class Employee(name: String, age: Int)

  def main(args: Array[String]) {
    val NUM_EMPLOYEES = 5
    val firstNames = List("Bruce", "Great", "The", "Jackie")
    val lastNames = List("Lee", "Khali", "Rock", "Chan")
    val employees = (0 until NUM_EMPLOYEES) map { i =>
      val first = Random.shuffle(firstNames).head
      val last = Random.shuffle(lastNames).head
      val fullName = s"$last, $first"
      val age = 20 + Random.nextInt(40)
      Employee(fullName, age)
    }

    employees foreach println

    val hasLee = """(Lee).*""".r
    for (employee <- employees) {
      employee match {
        case Employee(hasLee(x), age) => println("Found a Lee!")
        case _ => // Do nothing
      }
    }
  }
}




