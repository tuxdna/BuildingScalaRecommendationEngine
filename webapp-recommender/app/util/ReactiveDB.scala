package util

import scala.concurrent.ExecutionContext.Implicits.global

import com.mongodb.casbah.MongoClient

import model.AmazonMeta
import model.Customer
import reactivemongo.api.MongoDriver
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.Producer.nameValue2Producer

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

}