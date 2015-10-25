package chapter07

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import spray.can.Http
import spray.http._
import spray.http.MediaTypes._
import spray.routing._

import spray.json._
import DefaultJsonProtocol._

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}

case class RecResults(message: String, results: Map[String, Double] = Map())

// this trait defines our service behavior independently from the service actor
trait RecommendationService extends HttpService {

  implicit val impRecResults = jsonFormat2(RecResults)

  val myRoute = {
    path("") {
      get {
        respondWithMediaType(`text/html`) {
          // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                Welcome to RecommendationService.
              </body>
            </html>
          }
        }
      }
    }

    path("recommendations" / "foruser" / Segment) { user =>
      get {
        respondWithMediaType(`application/json`) {
          complete {
            val m = SharedData.alsModel
            val userId = SharedData.customerToIdMap.get(user)
            val rr = userId.map { id =>
              val recommendations: Array[Rating] = try {
                m.recommendProducts(id, 10)
              } catch {
                case t: Throwable => Array()
              }
              val recs: Array[(String, Double)] = recommendations.flatMap { r =>
                val asinOpt = SharedData.idToAsinMap.get(r.product)
                asinOpt.map(asin => asin -> r.rating)
              }

              RecResults(s"Found results for user: ${user}", recs.toMap)
            }.getOrElse(RecResults(s"No such user found: ${user}"))

            rr.toJson.prettyPrint

          }
        }
      }
    }
  }
}

class RecommendationServiceActor extends Actor with RecommendationService {

  def actorRefFactory = context

  def receive = runRoute(myRoute)
}

object SharedData {
  val modelPath = "models/AmazonRatingsALSModel/"
  val asinToIdPath = "mappings/asinToId"
  val customerToIdPath = "mappings/customerToId"

  lazy val sc = getSparkContext()
  lazy val asinToIdMap: Map[String, Int] = loadAsinMap
  lazy val customerToIdMap: Map[String, Int] = loadCustomerMap

  lazy val idToAsinMap: Map[Int, String] = asinToIdMap.map(x => x._2 -> x._1)
  lazy val idToCustomerMap: Map[Int, String] = customerToIdMap.map(x => x._2 -> x._1)

  lazy val alsModel = loadALSModel

  private[this] def loadALSModel() = MatrixFactorizationModel.load(sc, modelPath)

  private[this] def loadAsinMap(): Map[String, Int] = {
    sc.textFile(asinToIdPath).flatMap { line =>
      line.split(",") match {
        case Array(x, y, _*) => Some(x -> y.toInt)
        case _ => None
      }
    }.collectAsMap.toMap
  }

  private[this] def loadCustomerMap(): Map[String, Int] = {
    sc.textFile(customerToIdPath).flatMap { line =>
      line.split(",") match {
        case Array(x, y, _*) => Some(x -> y.toInt)
        case _ => None
      }
    }.collectAsMap.toMap
  }

  private[this] def getSparkContext(): SparkContext = {
    val conf = new SparkConf(false)
      .setMaster("local[4]")
      .setAppName(RecommendationServer.getClass.getName)

    conf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    conf.set("spark.executor.memory", "2g")
    val sc = new SparkContext(conf)
    sc
  }
}

object RecommendationServer {
  def main(args: Array[String]) {
    // we need an ActorSystem to host our application in
    implicit val system = ActorSystem("on-spray-can")

    // create and start our service actor
    val service = system.actorOf(Props[RecommendationServiceActor], "recommendation-service")

    implicit val timeout = Timeout(5.seconds)
    // start a new HTTP server on port 8080 with our service actor as the handler
    IO(Http) ? Http.Bind(service, interface = "localhost", port = 8082)
  }
}
