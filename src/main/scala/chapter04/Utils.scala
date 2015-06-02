package chapter04

import java.io.File
import java.io.PrintWriter
import org.apache.spark.mllib.regression.LabeledPoint
import scala.io.Source
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.classification.NaiveBayesModel
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.tree.model.DecisionTreeModel

object Utils {
  def writeToFile(filePath: String, output: Seq[(Double, Map[String, Double], Map[String, Double])]) = {
    val outFile = new File(filePath)
    val writer = new PrintWriter(outFile)
    writer.println("Lambda,TrainAccuracy,TrainMeanError,TrainRMSE,TestAccuracy,TestMeanError,TestRMSE")

    output foreach { e =>
      val (lambda, trainResult, testResult) = e
      writer.print(s"$lambda,${trainResult("Accuracy")},${trainResult("MeanError")},${trainResult("RMSE")},")
      writer.println(s"${testResult("Accuracy")},${testResult("MeanError")},${testResult("RMSE")}")
    }

    writer.close()

  }

  def readAndEncodeData(dataPath: String, fileName: String): Array[LabeledPoint] = {
    val f = new File(dataPath)
    val f2 = new File(f, fileName)
    val src = Source.fromFile(f2)
    val lines = src.getLines()
    val headerLine = lines.next()
    val headers = headerLine.split(";")
    val encoders = headers.map(hname => new DictionaryEncoder(hname))
    val data = lines.map(_.split(";")).toArray
    val encodedInstances = data.map { instance =>
      instance.zipWithIndex map { x =>
        x match {
          case (v, i) =>
            if (v.startsWith("\"")) {
              val encoder = encoders(i)
              val c = encoder.encode(v)
              c
            } else {
              v.toInt
            }
        }
      }
    }

    encodedInstances.map { inst =>
      val classLabel = inst.last
      val features = inst.dropRight(1)
      val vec = Vectors.dense(features.map(_.toDouble))
      LabeledPoint(classLabel, vec)
    }
  }

  def evaluate(model: NaiveBayesModel, test: RDD[LabeledPoint]) = {
    // Evaluate model on test data
    val actualVsPredicted = test.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }

    val accuracy = 1.0 * actualVsPredicted.filter(x => x._1 == x._2).count() / test.count()

    val meanError = actualVsPredicted.map { case (v, p) => v - p }.mean()
    println(s"Mean Error = $meanError")

    val RMSE = math.sqrt(actualVsPredicted.map {
      case (v, p) => math.pow((v - p), 2)
    }.mean())

    Map("RMSE" -> RMSE, "Accuracy" -> accuracy, "MeanError" -> meanError)
  }

  def evaluate(model: DecisionTreeModel, test: RDD[LabeledPoint]) = {
    // Evaluate model on test data
    val actualVsPredicted = test.map { point =>
      val prediction = model.predict(point.features)
      (point.label, prediction)
    }

    val accuracy = 1.0 * actualVsPredicted.filter(x => x._1 == x._2).count() / test.count()

    val meanError = actualVsPredicted.map { case (v, p) => v - p }.mean()
    println(s"Mean Error = $meanError")

    val RMSE = math.sqrt(actualVsPredicted.map {
      case (v, p) => math.pow((v - p), 2)
    }.mean())

    Map("RMSE" -> RMSE, "Accuracy" -> accuracy, "MeanError" -> meanError)
  }
}