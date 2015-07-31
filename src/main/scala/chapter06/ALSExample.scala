package chapter06

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import java.io.File
import org.apache.commons.io.FileUtils

object ALSExample {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf(false).setMaster("local[2]").setAppName("UserCFWithALSExample")
    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")

    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    val fileName = "movies-data.csv"

    val df = sqlContext.load("com.databricks.spark.csv",
      Map("path" -> fileName,
        "header" -> "true", "delimiter" -> ",",
        "quote" -> "\""))

    df.printSchema()
    df.show()

    val ratingsRDD = df.rdd.zipWithIndex.flatMap { entry =>
      val (row, userId) = entry
      val rowLen = row.length
      (1 until rowLen).flatMap { i =>
        val uid = userId.toInt
        val r = row.getString(i).toDouble
        if (r == 0) None else Some(Rating(uid, i, r))
      }
    }.cache()

    // train with implicit ratings
    // because ratings are either 0 or 1
    val rank = 2
    val numIterations = 3
    val model = ALS.trainImplicit(ratingsRDD, rank, numIterations)

    // Evaluate the model on rating data
    val usersProducts = ratingsRDD.map {
      case Rating(user, product, rate) =>
        (user, product)
    }

    val predictions = model.predict(usersProducts).map {
      case Rating(user, product, rate) =>
        ((user, product), rate)
    }

    val ratesAndPreds = ratingsRDD.map {
      case Rating(user, product, rate) =>
        ((user, product), rate)
    }.join(predictions)

    val MSE = ratesAndPreds.map {
      case ((user, product), (r1, r2)) =>
        val err = (r1 - r2)
        err * err
    }.mean()

    println("Mean Squared Error = " + MSE)

    // Save and load model
    val modelPath = "models/ALSExampleModel"
    if (new File(modelPath).exists()) {
      FileUtils.deleteDirectory(new File(modelPath));
    }
    model.save(sc, modelPath)
    val sameModel = MatrixFactorizationModel.load(sc, modelPath)

  }

}