package chapter02

import kafka.serializer.Encoder
import kafka.serializer.Decoder
import kafka.message.Message
import com.esotericsoftware.kryo.io.Output
import com.twitter.chill.ScalaKryoInstantiator
import com.esotericsoftware.kryo.io.Input
import org.apache.kafka.common.serialization.Serializer

class SessionDataSerializer extends Encoder[SessionData] with Decoder[SessionData] with Serializer[SessionData] {

  // Encoder interface
  def toBytes(data: SessionData): Array[Byte] = {
    val instantiator = new ScalaKryoInstantiator
    instantiator.setRegistrationRequired(false)
    val kryo = instantiator.newKryo()

    val buffer = new Array[Byte](4096)
    val output = new Output(buffer)
    kryo.writeObject(output, data)
    buffer
  }

  // Decoder interface
  def fromBytes(buffer: Array[Byte]): SessionData = {
    val instantiator = new ScalaKryoInstantiator
    instantiator.setRegistrationRequired(false)
    val kryo = instantiator.newKryo()
    val input = new Input(buffer)
    val sessionData = kryo.readObject(input, classOf[SessionData])
      .asInstanceOf[SessionData]
    sessionData
  }

  // Serializer interface
  def serialize(topic: String, data: SessionData) = toBytes(data)
  def configure(configs: java.util.Map[String, _], isKey: Boolean): Unit = {}
  def close(): Unit = {}
}

