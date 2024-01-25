import common.configuration.YamlFileConfigurationProvider
import common.configuration.Settings
import org.slf4j.LoggerFactory
import worker.downloadfile.DownloadFileWorker
import consumer.Consumer
import worker.downloadfile.DownloadFileWorkerContext
import net.codingwell.scalaguice.InjectorExtensions._
import com.google.inject.Guice
import di.SettingsModule
import di.InfraModule
import di.ClientsModule
import consumer.RabbitMqWrapper
import consumer.RedisWrapper
import parser.TaskParser
import consumer.QueueInfo
import worker.downloadfile.DownloadFileWorkerFactory

@main def main: Unit = {
    val configPath = "settings.yml"
    val config = YamlFileConfigurationProvider(configPath)
        .getConfig

    val injector = Guice.createInjector(
        SettingsModule(),
        InfraModule(),
        ClientsModule(),
    )
    
    val logger = LoggerFactory.getLogger(classOf[DownloadFileWorker])
    val consumer = Consumer[DownloadFileWorkerContext, DownloadFileWorker](
        rabbitClient = injector.instance[RabbitMqWrapper],
        redis = injector.instance[RedisWrapper],
        taskParser = injector.instance[TaskParser],
        settings = config.tryGetValue._2
    )
    consumer.configureFor(QueueInfo("download-file"), injector.instance[DownloadFileWorkerFactory])
    consumer.startScanTask()

    while (true) {
        Thread.sleep(1000)
    }
}
