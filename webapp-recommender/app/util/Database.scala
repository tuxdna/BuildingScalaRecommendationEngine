package util

import com.mongodb.casbah.MongoClient

object Database {
  def getCollection(collectionName: String) = {
    val mongoClient = MongoClient(DBConfig.dbHost, DBConfig.dbPort)
    val db = mongoClient(DBConfig.dbName)
    db(collectionName)
  }

  lazy val productsCollection = getCollection(DBConfig.products)

  private def deleteAll() = {
    productsCollection.drop()
  }

  def getAllProductGroups(): List[String] = {
    val rs = productsCollection.distinct("group")
    rs.map(_.toString).toList
  }

  def findBasic(): List[String] = {
    val rs = productsCollection.find()
    rs.map(_.toString).toList
  }

}
