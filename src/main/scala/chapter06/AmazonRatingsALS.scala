package chapter06

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import java.io.File
import org.apache.commons.io.FileUtils

object AmazonRatingsALS {
  def removePathIfExists(path: String): Unit = {
    if (new File(path).exists()) {
      FileUtils.deleteDirectory(new File(path))
    }
  }

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf(false)
      .setMaster("local[4]")
      .setAppName("AmazonRatingsALS")

    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.set("spark.executor.memory", "2g")

    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    val fileName = "datasets/amazon-ratings.csv"

    val rawData = sc.textFile(fileName, 100).
      map(_.split("\t")).
      flatMap { arr =>
      if (arr.length == 3) {
        val Array(c, a, r) = arr
        if (r.equals("rating")) None
        else Some((c, a, r.toDouble))
      } else None
    }

    val customerToIdRDD = rawData.map(_._1).distinct().zipWithUniqueId()
    val asinToIdRDD = rawData.map(_._2).distinct().zipWithUniqueId()
    val custFile = "mappings/customerToId"
    val asinFile = "mappings/asinToId"
    removePathIfExists(custFile)
    removePathIfExists(asinFile)
    customerToIdRDD.map { case (x, y) => s"$x,$y" }.saveAsTextFile(custFile)
    asinToIdRDD.map { case (x, y) => s"$x,$y" }.saveAsTextFile(asinFile)

    val customerToId = customerToIdRDD.collectAsMap
    val asinToId = asinToIdRDD.collectAsMap

    val ratingsRDD = rawData.map { t =>
      val (c, a, r) = t
      val cid = customerToId(c).toInt
      val aid = asinToId(a).toInt
      Rating(cid, aid, r)
    }.cache()

    val rank = 2
    val numIterations = 3
    val model = ALS.train(ratingsRDD, rank, numIterations, 0.01)

    // Evaluate the model on rating data
    val usersProducts = ratingsRDD.map {
      case Rating(user, product, rate) => (user, product)
    }
    val predictions = model.predict(usersProducts).map {
      case Rating(user, product, rate) => ((user, product), rate)
    }
    val ratesAndPreds = ratingsRDD.map {
      case Rating(user, product, rate) => ((user, product), rate)
    }.join(predictions)
    val MSE = ratesAndPreds.map {
      case ((user, product), (r1, r2)) =>
        val err = (r1 - r2)
        err * err
    }.mean()
    println("Mean Squared Error = " + MSE)

    // Save and load model
    val modelPath = "models/AmazonRatingsALSModel"
    removePathIfExists(modelPath)
    model.save(sc, modelPath)
    println(s"Model saved to: $modelPath")
    val sameModel = MatrixFactorizationModel.load(sc, modelPath)

  }

}