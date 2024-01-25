package consumer

import com.rabbitmq.client.{ConnectionFactory}
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Channel
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.Envelope
import java.nio.charset.Charset
import com.rabbitmq.client.Connection

class RabbitMqWrapper(connectionFactory: ConnectionFactory) {
    var connection: Connection = null
    var channel: Channel = null

    def setMessagesHandler(queueInfo: QueueInfo, handler: String => Unit): Unit = {
        connection = connectionFactory.newConnection()
        channel = connection.createChannel()
        channel.basicConsume(queueInfo.name, true, RabbitMqConsumer(channel, handler))
    }
    
    def dispose = {
        if channel != null then channel.close()
        if connection != null then connection.close()
    }
}

class RabbitMqConsumer(channel: Channel, handler: String => Unit) extends DefaultConsumer(channel) {
    override def handleDelivery(consumerTag: String, envelope: Envelope, properties: BasicProperties, body: Array[Byte]): Unit = {
        // val deliveryTag = envelope.getDeliveryTag()
        // channel.basicAck(deliveryTag, false)
        handler(String(body, Charset.forName("UTF-8")))
    }
}
