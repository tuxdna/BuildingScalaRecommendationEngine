package chapter07

import scala.io.Source

object ImportElasticSearch {

  val colSep = "\t"

  def main(args: Array[String]): Unit = {
    val fileName = "datasets/content-based-dataset-new.csv"
    val src = Source.fromFile(fileName)
    val iter = src.getLines()

    // skip header
    iter.next()

    println("Delete existing products from index")
    ESUtil.deleteProducts()

    iter.zipWithIndex.foreach {
      case (line, idx) =>
        try {
          val row = line.split(colSep)
          val asin = row(0)
          val title = row(1)
          val group = row(2)
          val salesRank = row(3).toLong
          val averageRating = row(4).toDouble
          val categories = if (row.length >= 6) row(5) else ""
          val item = AmazonItem(asin, title, group, salesRank, averageRating, categories)
          println(idx)
          ESUtil.insertItem(item)
        } catch {
          case t: NumberFormatException => t.printStackTrace() // ignore this record
        }
    }

  }
}

