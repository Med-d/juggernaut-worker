package common.configuration

import scala.beans.BeanProperty

case class Settings() {
    @BeanProperty var threadsCount: Int = 0
    @BeanProperty var rabbitMqHost: String = null
    @BeanProperty var rabbitMqPort: Int = 0
    @BeanProperty var rabbitMqUsername: String = null
    @BeanProperty var rabbitMqPassword: String = null
    @BeanProperty var redisHost: String = null
    @BeanProperty var redisPort: Int = 0
}
