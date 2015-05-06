package chapter02

import java.io.{ InputStreamReader, BufferedReader, InputStream }
import java.net.Socket
import org.apache.spark.{ SparkConf, Logging }
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{ Seconds, StreamingContext }
import org.apache.spark.streaming.receiver.Receiver
import kafka.serializer.StringDecoder
import scala.collection.JavaConversions._
import kafka.consumer.Consumer
import java.util.HashMap

import org.apache.log4j.Logger
import org.apache.log4j.Level
import kafka.consumer.ConsumerConfig

class SessionDataReceiver()
  extends Receiver[SessionData](StorageLevel.MEMORY_AND_DISK_2)
  with Logging {
  private def receive() = {
    // Start the thread that receives data over a connection
    val t = new Thread("Kafka Receiver") {
      override def run() {
        val consumerConfig = new ConsumerConfig(QueueConfig.consumerProps)
        val topic = QueueConfig.topic
        val topicCountMap = Map[String, Integer](topic -> 1)
        val consumer = Consumer.createJavaConsumerConnector(consumerConfig)
        val consumerMap = consumer.createMessageStreams[String, SessionData](
          topicCountMap,
          new StringDecoder,
          new SessionDataSerializer)
        val stream = consumerMap.get(topic).get(0)
        for (messageAndMetadata <- stream) {
          val key = messageAndMetadata.key()
          val message = messageAndMetadata.message()
          store(message)
        }
      }
    }
    t.start()
  }
  def onStart() { receive() }
  def onStop() {}
}









