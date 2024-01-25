package common.configuration

import common.Monad
import scala.util.Try
import java.io.FileReader
import java.io.FileInputStream
import java.io.File
import org.yaml.snakeyaml.constructor.Constructor
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.LoaderOptions
import common.ResultMonad
import common.ErrorMonad

trait ConfigurationProvider[TSettings] {
    def getConfig : Monad[Exception, TSettings]
}

class YamlFileConfigurationProvider(fileConfigPath: String)
    extends ConfigurationProvider[Settings] {

    override def getConfig: Monad[Exception, Settings] = {
        try {
            val input = FileInputStream(File(fileConfigPath))
            val loaderOptions = LoaderOptions()
            val yaml = Yaml(Constructor(classOf[Settings], loaderOptions))
            val config = yaml.load(input).asInstanceOf[Settings]
            ResultMonad(config)
        } catch {
            case e: Exception => ErrorMonad(e)
        }
    }
}
