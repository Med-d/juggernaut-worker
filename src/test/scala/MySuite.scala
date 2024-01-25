import consumer.Consumer
import com.google.inject.Guice
import di.SettingsModule
import di.InfraModule
import di.ClientsModule
import worker.downloadfile.DownloadFileWorkerContext
import worker.downloadfile.DownloadFileWorker
import net.codingwell.scalaguice.InjectorExtensions._
import consumer.RabbitMqWrapper
import consumer.RedisWrapper
import common.configuration.Settings
import parser.TaskParser
import consumer.QueueInfo
import worker.downloadfile.DownloadFileWorkerFactory
import org.slf4j.LoggerFactory
import common.configuration.ConfigurationProvider
// For more information on writing tests, see
// https://scalameta.org/munit/docs/getting-started.html
class MySuite extends munit.FunSuite {
    val injector = Guice.createInjector(
        SettingsModule(),
        InfraModule(),
        ClientsModule(),
    )

    test("[playground] check for consumer") {
        val logger = LoggerFactory.getLogger(classOf[MySuite])
        logger.debug("[playground] check for consumer")
        val settings = injector.instance[ConfigurationProvider[Settings]]
            .getConfig
        val consumer = Consumer[DownloadFileWorkerContext, DownloadFileWorker](
            rabbitClient = injector.instance[RabbitMqWrapper],
            redis = injector.instance[RedisWrapper],
            taskParser = injector.instance[TaskParser],
            settings = settings.tryGetValue._2
        )
        consumer.configureFor(QueueInfo("download-file"), injector.instance[DownloadFileWorkerFactory])
        consumer.startScanTask()

        while(true) {
            Thread.sleep(1000)
        }
    }
}
