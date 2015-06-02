package chapter04

class DictionaryEncoder(val name: String) {
  private var c = -1
  private var dict = Map[String, Int]()
  private var reverseDict = Map[Int, String]()
  def encode(s: String): Int = {
    this.synchronized {
      if (dict.contains(s)) { dict(s) }
      else {
        c += 1
        dict = dict ++ Map(s -> c)
        reverseDict = reverseDict ++ Map(c -> s)
        c
      }
    }
  }
  def decode(c: Int): String = {
    this.synchronized { reverseDict.getOrElse(c, "") }
  }
}

