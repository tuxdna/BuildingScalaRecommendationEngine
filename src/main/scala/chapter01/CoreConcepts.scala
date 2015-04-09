package chapter01

import scala.annotation.tailrec

object CoreConcepts {
  def main(args: Array[String]) {
    val x: Double = 10
    val y = 20.5
    val s = sum(x, y)
    println(f"$x%05.2f + $y%05.2f = $s%05.2f")
    println(s"Factorial(5) = ${factorial(5)}")
  }

  def sum(a: Double, b: Double) = a + b

  @tailrec
  def factorial(n: Int, partialProduct: Int = 1): Int = {
    if (n <= 1) partialProduct
    else factorial(n - 1, partialProduct * n)
  }
}