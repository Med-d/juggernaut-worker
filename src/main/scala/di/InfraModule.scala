package di

import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.google.inject.AbstractModule
import net.codingwell.scalaguice.InjectorExtensions.ScalaInjector
import parser.TaskParser
import net.codingwell.scalaguice.ScalaModule
import worker.downloadfile.DownloadFileWorkerFactory
import common.clients.ControllerClientFactory

class InfraModule extends AbstractModule with ScalaModule {
    override def configure(): Unit = {
        val jsonMapper = JsonMapper.builder()
            .addModule(DefaultScalaModule)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .build()

        bind[JsonMapper].toInstance(jsonMapper)

        bind[TaskParser]
        bind[DownloadFileWorkerFactory]
        bind[ControllerClientFactory]
    }
}
