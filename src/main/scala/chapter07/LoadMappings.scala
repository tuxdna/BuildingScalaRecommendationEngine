package chapter07

import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.{SparkConf, SparkContext}

object LoadMappings {
  def main(args: Array[String]) {
    val conf = new SparkConf(false)
      .setMaster("local[4]")
      .setAppName("LoadMappings")

    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.set("spark.executor.memory", "2g")

    val sc = new SparkContext(conf)


    val codePath = "/home/tuxdna/work/packt/BuildingScalaRecommendationEngine/code"
    val asinPath = codePath + "/mappings/asinToId"
    val custPath = codePath + "/mappings/customerToId"

    val asinRDD = sc.textFile(asinPath)
    val rdd1 = asinRDD.flatMap { line =>
      line.split(",") match {
        case Array(x, y, _*) => Some(x -> y.toInt)
        case _ => None
      }
    }

    val asin2Id = rdd1.collectAsMap
    val id2asin = rdd1.map(x => x._2 -> x._1).collectAsMap

    val custRDD = sc.textFile(custPath)
    val rdd2 = custRDD.flatMap { line =>
      line.split(",") match {
        case Array(x, y, _*) => Some(x -> y.toInt)
        case _ => None
      }
    }

    val customer2Id = rdd2.collectAsMap
    val id2Customer = rdd2.map(x => x._2 -> x._1).collectAsMap

    val modelPath = codePath + "/models/AmazonRatingsALSModel"
    val sameModel = MatrixFactorizationModel.load(sc, modelPath)

    val customer = "AM8ODIXYACHBA"
    val userId = customer2Id(customer)
    val item = "1576750248"
    val itemId = asin2Id(item)
    val rating = sameModel.predict(1, 1)
    println(s"Rating for (customer: $customer, item: $item) = $rating")
    val recommendations = sameModel.recommendProducts(1, 10)
    recommendations foreach println

  }

}
