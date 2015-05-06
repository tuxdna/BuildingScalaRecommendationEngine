package chapter02
import java.io.File
import scala.collection.immutable.SortedMap

object Stats {
  def main(args: Array[String]) {
    val entreeDataPath = args(0)
    val config = DataConfig(entreeDataPath)
    val featuresMap = Utilities.loadFeaturesMap(config.featuresFile)
    val restaurants = config.locations.flatMap { location =>
      Utilities.loadLocationData(new File(s"${config.dataPath}/" + location))
    }
    println("Cities and their restaurant count")
    println("-" * 50)
    val grouped = restaurants.groupBy(_.city).map { case (k, v) => k -> v.size }
    val citiesSorted = grouped.keys.toArray.sorted
    citiesSorted foreach { city =>
      val count = grouped(city)
      println(f"$city%20s has $count%6d restaurants")
    }
    println()

    // top 5 popular features in each city
    val cityAndTop5 = restaurants.groupBy(_.city).map { r =>
      val (city, restaurantList) = r
      val partialSum = restaurantList.flatMap(_.features).map(_ -> 1)
      val allsum = partialSum.groupBy(_._1).map {
        case (k, v) => k -> v.map(_._2).sum
      }.toSeq.sortBy(_._2).reverse
      val top5 = allsum.take(5)
      city -> top5
    }

    citiesSorted.foreach { city =>
      val top5 = cityAndTop5(city)
      println(s"City: $city")
      println("-" * 50)
      top5 foreach { entry =>
        val (f, count) = entry
        println(f"${featuresMap(f)}%25s at $count%6d restaurants")
      }
      println()
    }
  }
}





