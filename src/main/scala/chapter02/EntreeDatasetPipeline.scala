package chapter02

import scala.io.Source
import java.io.File
import java.io.FilenameFilter
import akka.actor.Actor
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import akka.dispatch.ExecutionContexts._
import scala.io.Source
import java.util.Properties
import com.mongodb.DBObject
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.MongoClient
import scala.collection.JavaConversions._
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import akka.actor.{ Actor, Props }
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.receiver._
import org.apache.log4j.Logger
import org.apache.log4j.Level
import kafka.consumer.ConsumerConfig
import com.mongodb.BasicDBList

object QueueConfig {
  val producerProps = Map(
    "metadata.broker.list" -> "localhost:9092",
    "serializer.class" -> "kafka.serializer.DefaultEncoder",
    "key.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
    "value.serializer" -> "chapter02.SessionDataSerializer",
    "bootstrap.servers" -> "localhost:9092")

  val zkConnect = "127.0.0.1:2181"
  val groupId = "group"
  val topic = "topic"
  val kafkaServerURL = "localhost"
  val kafkaServerPort = 9092
  val kafkaProducerBufferSize = 64 * 1024
  val connectionTimeOut = 100000
  val reconnectInterval = 10000
  val clientId = "EntreeClient"

  val consumerProps = {
    val props = new Properties()
    props.put("zookeeper.connect", zkConnect)
    props.put("group.id", groupId)
    props.put("zookeeper.session.timeout.ms", "400")
    props.put("zookeeper.sync.time.ms", "200")
    props.put("auto.commit.interval.ms", "1000")
    props
  }
}

object DBConfig {
  val dbName = "entree"
  val restaurants = "restaurants"
  val sessions = "sessions"
  val dbHost = "localhost"
  val dbPort = 27017
}

object Database {
  def getCollection(collectionName: String) = {
    val mongoClient = MongoClient(DBConfig.dbHost, DBConfig.dbPort)
    val db = mongoClient(DBConfig.dbName)
    db(collectionName)
  }

  lazy val restaurantCollection = getCollection(DBConfig.restaurants)
  lazy val sessionCollection = getCollection(DBConfig.sessions)

  def deleteAll() = {
    restaurantCollection.drop()
    sessionCollection.drop()
  }

  def insertSession(sessionData: SessionData) = {
    val attributes = Map(
      "datatime" -> sessionData.datetime,
      "ip" -> sessionData.ip,
      "entryPoint" -> sessionData.entryPoint,
      "navigations" -> sessionData.navigations,
      "endPoint" -> sessionData.endPoint)
    val uo = MongoDBObject.newBuilder
    uo ++= attributes
    val doc = uo.result
    // println(s"${sessionData.ip}: ${sessionData.datetime}")
    sessionCollection.insert(doc)
  }

  def insertRestaurant(restaurant: Restaurant) = {
    val attributes = Map(
      "id" -> restaurant.id,
      "name" -> restaurant.name,
      "features" -> restaurant.features,
      "city" -> restaurant.city)
    val uo = MongoDBObject.newBuilder
    uo ++= attributes
    val doc = uo.result
    // println(s"${restaurant.id}: ${restaurant.name}")
    restaurantCollection.insert(doc)
  }

  def getRestaurantByIdInChicago(id: String): Option[Restaurant] = {
    val coll = getCollection(DBConfig.restaurants)
    val idCode = id.toInt
    val q = MongoDBObject("id" -> f"$idCode%07d", "city" -> "chicago")
    // val fields = MongoDBObject("name" -> 1)
    val r = coll.findOne(q)
    r.map { x =>
      val id = x.get("id").asInstanceOf[String]
      val name = x.get("name").asInstanceOf[String]
      val features = x.get("features").asInstanceOf[BasicDBList].toArray().map(_.asInstanceOf[String])
      val city = x.get("city").asInstanceOf[String]
      Restaurant(id, name, features, city)
    }
  }
}

class DBPersistenceActor extends Actor {
  def receive = {
    case restaurant: Restaurant => {
      Database.insertRestaurant(restaurant)
    }
    case sessionData: SessionData => {
      Database.insertSession(sessionData)
    }
  }
}

class MainActor(config: DataConfig) extends Actor {
  import Utilities._
  val dbPersister = context.actorOf(Props[DBPersistenceActor], "dbPersister")
  val messageProducer = context.actorOf(Props[MessageProducerActor], "messageProducer")
  def receive = {
    case "begin" => {
      val featuresMap = loadFeaturesMap(config.featuresFile)
      val restaurants = config.locations.flatMap { location =>
        loadLocationData(new File(s"${config.dataPath}/" + location))
      }
      println(s"Number of restuarants: ${restaurants.size}")

      // store all locations to MongoDB
      for (restaurant <- restaurants) { dbPersister ! restaurant }

      // load session data
      val sessionFiles = new File(config.sessionPath).list(
        new FilenameFilter {
          def accept(f: File, name: String) = {
            name.startsWith("session.")
          }
        })

      println(sessionFiles.toList)

      // treat each file as a separate batch of data
      for (sf <- sessionFiles) {
        val sessionFile = s"${config.sessionPath}/$sf"
        println(s"Loading session from: $sessionFile")
        val sessionsData = loadSessionData(sessionFile)
        println(s"\t\tNumber of recorded sessions: ${sessionsData.size}")
        for (session <- sessionsData) {
          // persist to database
          dbPersister ! session
          // enqueue into Kafka
          messageProducer ! session
        }
        Thread.sleep(2 * 1000)
      }
    }
    case "exit" => {
      println("Exiting now...")
      System.exit(0)
    }
  }
}

class MessageProducerActor extends Actor {

  import org.apache.kafka.clients.producer.KafkaProducer

  var producer: KafkaProducer[String, SessionData] = _
  override def preStart() = {
    println(s"=== MessageProducer starting up: path=${context.self.path} ===")
    producer = new KafkaProducer[String, SessionData](QueueConfig.producerProps)
  }

  def receive = {
    case sessionData: SessionData => {
      val key = sessionData.datetime + sessionData.ip
      val value = sessionData
      val rec = new ProducerRecord[String, SessionData](QueueConfig.topic, key, value)
      // println(rec)
      val f = producer.send(rec)
      // wait for message ack
      f.get()
    }
  }

  override def postStop() = {
    producer.close()
  }
}

object EntreeDatasetPipeline {
  def main(args: Array[String]) {
    val system = ActorSystem("system")
    val entreeDataPath = args(0)
    val config = DataConfig(entreeDataPath)

    /*
     * Pipeline basically looks like this:
     * 
     * Entree Text files -> Database -> Kafka -> Spark Streaming
     * 
     */

    // drop existing data from MongoDB
    Database.deleteAll()
    // cleanup the stage and then begin

    val props = Props(new MainActor(config))
    val mainActor = system.actorOf(props)
    mainActor ! "begin"

    // set logging to error
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    val conf = new SparkConf(false).setMaster("local[2]").setAppName("Entree")
    val ssc = new StreamingContext(conf, Seconds(2))

    val receiver = new SessionDataReceiver()
    val sessionDataStream = ssc.receiverStream(receiver)
    val userVisit = sessionDataStream.map(sd => sd.endPoint)
    val userVisitCount = userVisit.countByValue()

    val pattern = """(\d\d\d)(\w)""".r
    val navigations = sessionDataStream.flatMap { sd =>
      sd.navigations.filter(_.length == 4).map { code =>
        code match {
          case pattern(restuarantCode, action) => (restuarantCode, action)
        }
      }
    }

    val navigationCount = navigations.map(_._1).countByValue()

    navigationCount.foreachRDD { rdd =>
      println("\nNext batch...\n")
      val lst = rdd.collect.toList
      val top5 = lst.sortBy(_._2).reverse.take(5)
      val top5restaurants = top5.flatMap { x =>
        val (restaurantCode, count) = x
        Database.getRestaurantByIdInChicago(restaurantCode).map { r =>
          r.name -> count
        }
      }
      top5restaurants foreach { entry =>
        println(s"${entry._1} was recently visited ${entry._2} times")
      }
    }

    // Wait for all connections to establish to different services
    Thread.sleep(5 * 1000)

    // start stream processing and wait until notified
    ssc.start()
    ssc.awaitTermination()
  }
}




