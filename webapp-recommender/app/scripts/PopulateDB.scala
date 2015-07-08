package scripts

import scala.collection.mutable
import scala.util.Random

import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import com.mongodb.casbah.commons.MongoDBObject

import util.Database

object PopulateDB {

  def main(args: Array[String]): Unit = {
    println("Populate DB scripts")
    val rs = Database.productsCollection.find(MongoDBObject.empty)
    val set = mutable.Set[String]()
    for (c <- rs) {
      val reviews = c.get("reviews").asInstanceOf[BasicDBList]
      for (review <- reviews.toArray()) {
        val cust = review.asInstanceOf[BasicDBObject].get("customer").toString
        set += cust
      }
    }
    println(s"Total Customers = ${set.size}")

    val (firstNames, lastNames) = GenerateNames.loadNames

    for (customer <- set) {
      val fi = Random.nextInt(firstNames.length)
      val li = Random.nextInt(lastNames.length)
      val firstName = firstNames(fi)
      val lastName = lastNames(li)
      println(s"customer: $customer  -> $firstName $lastName")
    }
  }
}
