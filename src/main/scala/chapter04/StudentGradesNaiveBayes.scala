package chapter04

import scala.io.Source
import java.io.File
import org.apache.spark.mllib.util.MLUtils
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.linalg
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.spark.mllib.classification.NaiveBayes
import org.apache.spark.mllib.classification.NaiveBayesModel

object StudentGradesNaiveBayes {
  def main(args: Array[String]) {
    val dataPath = "datasets/student"
    val mathFileName = "student-mat.csv"
    val fileName2 = "student-por.csv"
    val mathInstances = Utils.readAndEncodeData(dataPath, mathFileName)
    val portugeseInstances = Utils.readAndEncodeData(dataPath, mathFileName)
    val allInstances = mathInstances ++ portugeseInstances
    // now that we have encoded features into numeric values,
    // lets create RDD of labeled points for classification    
    val conf = new SparkConf(false).setMaster("local[2]").setAppName("StudentGrades")
    val sc = new SparkContext(conf)
    val data = sc.parallelize(allInstances)
    // test / train split
    val splits = data.randomSplit(Array(0.6, 0.4), seed = 11L)
    val trainData = splits(0).cache()
    val testData = splits(1)
    val NLambdas = 100
    val output = (1 to NLambdas) map { i =>
      val lambda = 1.0 * i / NLambdas
      val model = NaiveBayes.train(trainData, lambda = lambda)
      val trainResult = Utils.evaluate(model, trainData)
      val testResult = Utils.evaluate(model, testData)
      (lambda, trainResult, testResult)
    }
    Utils.writeToFile("output/StudentOutput-NaiveBayes.csv", output)
  }
}

