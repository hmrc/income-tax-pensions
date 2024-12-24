/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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