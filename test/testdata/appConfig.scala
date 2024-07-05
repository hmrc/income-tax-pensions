package testdata

import config.AppConfig
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder

object appConfig {
  def createAppConfig(config: Configuration = Configuration()): AppConfig = {
    val app = new GuiceApplicationBuilder().configure(config).build()
    app.injector.instanceOf[AppConfig]
  }
}
