package scripts

import scala.io.Source
import scala.util.Random

object GenerateNames {

  def loadNames = {
    val datasetFolder = "/home/tuxdna/work/packt/BuildingScalaRecommendationEngine/code/datasets/"

    val firstNamesFile = datasetFolder + "census-derived-all-first.txt"
    val lastNamesFile = datasetFolder + "dist.all.last"

    val firstNames = Source.fromFile(firstNamesFile).getLines.map {
      line => line.split("\\s+").take(1).head
    }.toArray
    val lastNames = Source.fromFile(lastNamesFile).getLines.map {
      line => line.split("\\s+").take(1).head
    }.toArray

    (firstNames, lastNames)
  }

  def main(args: Array[String]) {
    val (firstNames, lastNames) = loadNames

    for (i <- 0 until 10000) {
      val fi = Random.nextInt(firstNames.length)
      val li = Random.nextInt(lastNames.length)
      val firstName = firstNames(fi)
      val lastName = lastNames(li)
      println(s"$firstName $lastName")
    }

  }
}