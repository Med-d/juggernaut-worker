package di

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import com.google.inject.Guice
import common.configuration.Settings
import common.configuration.ConfigurationProvider
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import consumer.RabbitMqWrapper
import redis.clients.jedis.Jedis
import consumer.RedisWrapper
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import com.rabbitmq.client.ConnectionFactory

class ClientsModule extends AbstractModule with ScalaModule {
    override def configure(): Unit = {
        val injector = Guice.createInjector(SettingsModule())
        val configurationProvider = injector.instance[ConfigurationProvider[Settings]]
        val (err, config) = configurationProvider
            .getConfig
            .tryGetValue
        if (err != null)
            throw new IllegalStateException(s"Configuration provider error: ${err.toString()}")

        val rabbitConnectionFactory = ConnectionFactory()
        rabbitConnectionFactory.setHost(config.rabbitMqHost)
        rabbitConnectionFactory.setPort(config.rabbitMqPort)
        rabbitConnectionFactory.setUsername(config.rabbitMqUsername)
        rabbitConnectionFactory.setPassword(config.rabbitMqPassword)
        rabbitConnectionFactory.setAutomaticRecoveryEnabled(true)
        rabbitConnectionFactory.setConnectionTimeout(60 * 1000)
        val rabbitMqWrapper = RabbitMqWrapper(rabbitConnectionFactory)

        bind[RabbitMqWrapper].toInstance(rabbitMqWrapper)

        val jedisPool = JedisPool(JedisPoolConfig(), config.redisHost, config.redisPort)
        bind[RedisWrapper].toInstance(RedisWrapper(jedisPool))
    }
}
