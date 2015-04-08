package chapter01

object Funky {
  def main(args: Array[String]) {
    nu kid on the { "next block" }
  }

  object on; object block

  object nu {
    def kid(y: on.type) = { print("new kid on "); this }
    def the(block: => String) = {
      println(block)
      println("says Scala is awesome!")
    }
  }
}
