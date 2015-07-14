package util

import scala.concurrent.ExecutionContext.Implicits.global
import com.mongodb.casbah.MongoClient
import model.AmazonMeta
import model.Customer
import reactivemongo.api.MongoDriver
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.Producer.nameValue2Producer
import reactivemongo.bson.BSONDateTime
import java.util.Date
import model.Review
import scala.concurrent.Await
import reactivemongo.bson.BSONDouble

object ReactiveDB {
  def getCollection(collectionName: String) = {
    val mongoClient = MongoClient(DBConfig.dbHost, DBConfig.dbPort)
    val db = mongoClient(DBConfig.dbName)
    db(collectionName)
  }

  def productCollection() = {
    val driver = new MongoDriver
    val connection = driver.connection(List(DBConfig.dbHost))
    val db = connection(DBConfig.dbName)
    val collection = db("products")
    collection
  }

  def customerCollection() = {
    val driver = new MongoDriver
    val connection = driver.connection(List(DBConfig.dbHost))
    val db = connection(DBConfig.dbName)
    val collection = db("customers")
    collection
  }

  def reviewsCollection() = {
    val driver = new MongoDriver
    val connection = driver.connection(List(DBConfig.dbHost))
    val db = connection(DBConfig.dbName)
    val collection = db("reviews")
    collection
  }

  def getByASIN(asin: String) = {
    val collection = productCollection()
    val query = BSONDocument("asin" -> asin)
    val rs = collection.find(query).cursor[AmazonMeta]
    rs
  }

  def getByGroup(group: String) = {
    val collection = productCollection()
    val query = BSONDocument("group" -> group)
    val rs = collection.find(query).cursor[AmazonMeta]
    rs
  }

  def getAll(asin: String) = {
    val collection = productCollection()
    val query = BSONDocument.empty
    val rs = collection.find(query).cursor[AmazonMeta]
    rs
  }

  def allProducts() = {
    val query = BSONDocument.empty
    val collection = productCollection()
    val cursor = collection.find(query).cursor[AmazonMeta]
    cursor
  }

  def topRatedProducts() = {
    val query = BSONDocument.empty
    val sortCriteria = BSONDocument("overallReview.averageRating" -> -1)
    val collection = productCollection()
    val cursor = collection.find(query).sort(sortCriteria).cursor[AmazonMeta]
    cursor
  }

  def mostPopularProducts() = {
    val query = BSONDocument.empty
    val sortCriteria = BSONDocument("overallReview.total" -> -1)
    val collection = productCollection()
    val cursor = collection.find(query).sort(sortCriteria).cursor[AmazonMeta]
    cursor
  }

  def allCustomers() = {
    val query = BSONDocument.empty
    val collection = customerCollection()
    val cursor = collection.find(query).cursor[Customer]
    cursor
  }

  def getCustomerByNumber(number: Int) = {
    val collection = customerCollection()
    val query = BSONDocument("Number" -> number)
    val rs = collection.find(query).cursor[Customer]
    rs
  }

  def getCustomerByID(id: String) = {
    val collection = customerCollection()
    val query = BSONDocument("id" -> id)
    val rs = collection.find(query).cursor[Customer]
    rs
  }

  def trendingCustomers(year: Int, month: Int) = {
    val query = BSONDocument.empty
    val sortCriteria = BSONDocument("overallReview.total" -> -1)
    val collection = reviewsCollection()
    val cursor = collection.find(query).sort(sortCriteria).cursor[AmazonMeta]
    cursor
  }

  def oneMonthReviewsFrom(year: Int, month: Int) = {

    val start = DateUtils.dateFor(year, month, 1)
    val end = DateUtils.dateFor(year, month + 1, 1)
    println("Start Date: %s, End Date: %s".format(
      DateUtils.sdfFull.format(start),
      DateUtils.sdfFull.format(end)))

    println(start.getTime(), end.getTime())
    val query = BSONDocument(
      "date" -> BSONDocument(
        "$gte" -> BSONDateTime(start.getTime()),
        "$lt" -> BSONDateTime(end.getTime()) //
        ) //
        )

    val sortCriteria = BSONDocument("votes" -> -1)
    val collection = reviewsCollection()
    val cursor = collection
      .find(query)
      //.sort(sortCriteria)
      .cursor[BSONDocument]

    cursor
  }

  import scala.concurrent._
  import scala.concurrent.duration._

  def main(args: Array[String]) {
    val c = oneMonthReviewsFrom(2005, 1)
    val seqF = c.collect[Seq](10, false)
    seqF.onFailure {
      case _ =>
        println("Failed")
    }

    seqF.onSuccess {
      case x @ _ =>
        println(s"Succeeded with $x")
    }

    val resF = seqF.map { seq =>
      seq foreach { x =>
        val productId = x.get("productId").map { v =>
          v.asInstanceOf[BSONDouble].value.toLong
        }.get
        println(productId)
      }
    }

    Await.result(resF, 1 minute)
  }

}