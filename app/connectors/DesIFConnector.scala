/*
 * Copyright 2023 HM Revenue & Customs
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

package connectors

import com.typesafe.config.ConfigFactory
import config.AppConfig
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier.Config
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import java.net.URI

import java.net.URL

trait DesIFConnector extends Logging {

  val appConfig: AppConfig

  val headerCarrierConfig: Config = HeaderCarrier.Config.fromConfig(ConfigFactory.load())

  private[connectors] def desHeaderCarrier(url: String)(implicit hc: HeaderCarrier): HeaderCarrier =
    headerCarriers(url, authToken = appConfig.authorisationToken, appConfig.environment)
  private[connectors] def integrationFrameworkHeaderCarrier(url: String, apiNumber: String)(implicit hc: HeaderCarrier): HeaderCarrier =
    headerCarriers(url, authToken = appConfig.integrationFrameworkAuthorisationToken(apiNumber), appConfig.integrationFrameworkEnvironment)

  private[connectors] def hipHeaderCarrier(url: String, apiNumber: String)(implicit hc: HeaderCarrier): HeaderCarrier =
    headerCarriers(url, authToken = appConfig.hipAuthorisationToken(apiNumber), appConfig.hipEnvironment)

  def headerCarriers(url: String, authToken: String, env: String)(implicit hc: HeaderCarrier): HeaderCarrier = {
    val hcWithAuth     = hc.copy(authorization = Some(Authorization(s"Bearer $authToken")))
    val isInternalHost = headerCarrierConfig.internalHostPatterns.exists(_.pattern.matcher(URI.create(url).toURL.getHost).matches())
    Connector.headerCarrier(isInternalHost, "Environment" -> env)(hcWithAuth)
  }
}
