package chapter05

import scala.Array.canBuildFrom
import scala.Option.option2Iterable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source

import reactivemongo.api.MongoDriver
import reactivemongo.api.collections.default.BSONCollection


object LoadAmazonDataset {

  def addToEntries(entries: ArrayBuffer[AmazonMeta], currentItem: AmazonMeta) = {
    // entries += currentItem
    println(currentItem)
  }

  def addToEntries(collection: BSONCollection, currentItem: AmazonMeta) = {
    // entries += currentItem
    println(currentItem)
    collection.save(currentItem)
  }

  def main(args: Array[String]) {
    val defaultFile = "datasets/amazon-dataset/sample2.txt"
    val file = if (args.length > 0) args(0) else defaultFile

    val driver = new MongoDriver
    val connection = driver.connection(List("localhost"))

    val db = connection("amazon_dataset")
    val ratingCollection = db[BSONCollection]("products")

    val src = Source.fromFile(file)
    val idR = "\\s*Id:\\s+(.+)".r
    val asinR = "\\s*ASIN: (.+)".r
    val titleR = "\\s*title: (.+)".r
    val groupR = "\\s*group: (.+)".r
    val salesrankR = "\\s*salesrank: (.+)".r
    val similarR = "\\s*similar: (.+)".r
    val categoriesR = "\\s*categories: (.+)".r
    val reviewsR = "\\s*reviews: (.+)".r
    val reviewCountR = "\\s*total:\\s*(\\d+)\\s*downloaded:\\s*(\\d+)\\s*avg rating:\\s*(.*)".r
    val reviewLineR = "\\s*(.*?)\\s+cutomer:\\s+(.*?)\\s+rating:\\s+(.*?)\\s+votes:\\s+(.*?)\\s+helpful:\\s+(.*?)".r
    val lIter = src.getLines

    var currentItem: AmazonMeta = null

    while (lIter.hasNext) {
      val l = lIter.next
      l match {
        case idR(id) =>
          if (currentItem != null) {
            addToEntries(ratingCollection, currentItem)
            currentItem = null
          }
          currentItem = AmazonMeta(id.toInt, null, null, null, 0, Nil, Nil, Nil, OverallReview(0, 0, 0))
        case asinR(asin) =>
          currentItem.asin = asin
        case titleR(title) =>
          currentItem.title = title
        case groupR(group) =>
          currentItem.group = group
        case salesrankR(salesrank) =>
          currentItem.salesrank = salesrank.toInt
        case similarR(similar) =>
          val parts = similar.split("\\s+")
          val n = if (parts.length > 0) parts(0).toInt else 0
          currentItem.similar = parts.takeRight(n).toList
        case categoriesR(categories) =>
          val catCount = categories.toInt
          val pat = "(.*?)\\[(\\d+)\\]".r

          val catList = Array.tabulate(catCount) { i =>
            val ln = lIter.next().trim
            // println(ln)
            val parts = ln.split("\\|")
              .filter(_.length() > 0)
            val catcodes = parts
              .map { catname =>
                val pat(name, code) = catname
                Category(name, code.toInt)
              }.toList

            catcodes
          }.toList
          currentItem.categories = catList

        case reviewsR(reviews) =>
          reviews match {
            case reviewCountR(total, downloaded, averageRating) =>
              val reviewsList = Array.tabulate(total.toInt) { i =>
                val reviewLine = lIter.next()
                reviewLine match {
                  case reviewLineR(date, customer, rating, votes, helpful) =>
                    Some(Review(date, customer, rating.toInt, votes.toInt, helpful.toInt))
                  case _ => None
                }
              }.flatten.toList
              currentItem.reviews = reviewsList
              currentItem.overallReview = OverallReview(total.toInt, downloaded.toInt, averageRating.toDouble)
            case _ => // skip
          }
        case _ => // skip
      }
    }

    if (currentItem != null) {
      addToEntries(ratingCollection, currentItem)
      currentItem = null
    }

    Thread.sleep(1000) // wait sometime for last item to persist
    System.exit(0)
  }
}