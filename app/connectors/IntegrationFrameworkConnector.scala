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

package connectors

import cats.data.EitherT
import config.AppConfig
import connectors.httpParsers.ApiParser
import models.domain.ApiResultT
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import utils.Logging

import javax.inject.Inject
import scala.concurrent.ExecutionContext

// TODO Move all calls to IFS here
trait IntegrationFrameworkConnector {}

class IntegrationFrameworkConnectorImpl @Inject()(val http: HttpClientV2, val appConfig: AppConfig)
                                                 (implicit ec: ExecutionContext) extends IntegrationFrameworkConnector with Logging {

  private def parser(apiNumber: Option[String]) = ApiParser.CommonHttpReads(s"integration-framework: api-${apiNumber.getOrElse("?")}")

  // It's a public method outside of the trait because it is only used in testOnly
  def testClearData(nino: String)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val url = url"${appConfig.ifBaseUrl}/nino/$nino"
    implicit val reads: ApiParser.CommonHttpReads = parser(Some("test-only-stub"))

    val res = http.delete(url).execute

    EitherT(res).leftMap(_.toServiceError)
  }
}
