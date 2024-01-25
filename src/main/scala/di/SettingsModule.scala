package di

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import common.configuration.YamlFileConfigurationProvider
import common.configuration.ConfigurationProvider
import common.configuration.Settings

class SettingsModule extends AbstractModule with ScalaModule{
    override def configure(): Unit = {
        val configPath = "settings.yml"
        val configurationProvider = YamlFileConfigurationProvider(configPath)
        bind[ConfigurationProvider[Settings]].toInstance(configurationProvider)
    }
}
