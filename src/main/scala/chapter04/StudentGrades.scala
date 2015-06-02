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
import org.apache.spark.mllib.tree.DecisionTree
import org.apache.spark.mllib.regression.IsotonicRegression

object StudentGrades {
  def main(args: Array[String]) {
    val dataPath = if (args.length > 0) args(0) else "datasets/student"
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

    val nbClassifier = {
      val lambda = 0.5
      val model = NaiveBayes.train(trainData, lambda = lambda)
      val trainResult = Utils.evaluate(model, trainData)
      val testResult = Utils.evaluate(model, testData)
      (0.0, trainResult, testResult)
    }

    val dtreeClassifier = {
      val numClasses = 21
      val categoricalFeaturesInfo = Map[Int, Int]()
      val impurity = "gini"
      val maxDepth = 5
      val maxBins = 32
      val model = DecisionTree.trainClassifier(trainData, numClasses, categoricalFeaturesInfo, impurity, maxDepth, maxBins)
      val trainResult = Utils.evaluate(model, trainData)
      val testResult = Utils.evaluate(model, testData)
      (1.0, trainResult, testResult)
    }

    val dtreeRegressor = {
      val categoricalFeaturesInfo = Map[Int, Int]()
      val impurity = "variance"
      val maxDepth = 5
      val maxBins = 32
      val model = DecisionTree.trainRegressor(trainData, categoricalFeaturesInfo, impurity, maxDepth, maxBins)
      val trainResult = Utils.evaluate(model, trainData)
      val testResult = Utils.evaluate(model, testData)
      (2.0, trainResult, testResult)
    }

    val output = List(nbClassifier, dtreeClassifier, dtreeRegressor)
    Utils.writeToFile("output/StudentOutput-Comparison.csv", output)
  }

}
