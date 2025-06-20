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

import config.AppConfig
import connectors.httpParsers.ApiParser
import models.common.{Nino, TaxYear}
import models.employment.{AllEmploymentData, CreateUpdateEmploymentRequest}
import models.logging.ConnectorRequestInfo
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class EmploymentConnector @Inject()(val http: HttpClientV2, val appConfig: AppConfig)(implicit ec: ExecutionContext) extends Connector {
  private val downstreamServiceName = "income-tax-employment"
  private val parser = ApiParser.CommonHttpReads(downstreamServiceName)

  def getEmployments(nino: Nino, taxYear: TaxYear)(implicit hc: HeaderCarrier): DownstreamOutcome[Option[AllEmploymentData]] = {
    val url = url"${appConfig.getEmploymentSourceUrl(nino, taxYear)}"

    implicit val updatedHc: HeaderCarrier = headerCarrier(url.toString)(hc)
    implicit val optRds: OptionalContentHttpReads[AllEmploymentData] = new OptionalContentHttpReads[AllEmploymentData]

    ConnectorRequestInfo("GET", url.toString, downstreamServiceName)(updatedHc).logRequest(logger)

    http.get(url)(updatedHc).execute
  }

  def saveEmployment(nino: Nino, taxYear: TaxYear, model: CreateUpdateEmploymentRequest)(implicit
                                                                                         hc: HeaderCarrier,
                                                                                         ec: ExecutionContext): DownstreamOutcome[Unit] = {
    val url = url"${appConfig.getEmploymentSourceUrl(nino, taxYear)}"

    http.post(url).withBody(Json.toJson(model)).execute(parser, ec)
  }

  def deleteEmployment(nino: Nino, taxYear: TaxYear, employmentId: String)(implicit
                                                                           hc: HeaderCarrier,
                                                                           ec: ExecutionContext): DownstreamOutcome[Unit] = {
    val source = "CUSTOMER"
    val url = url"${appConfig.getEmploymentUrl(nino, employmentId, source, taxYear)}"
    ConnectorRequestInfo("DELETE", url.toString, downstreamServiceName).logRequest(logger)

    http.delete(url).execute(parser, ec)
  }

}
