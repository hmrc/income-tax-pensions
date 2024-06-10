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
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

// TODO LT extract trait
class EmploymentConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)(implicit ec: ExecutionContext) extends Connector {
  private val downstreamServiceName = "income-tax-employment"
  private val parser                = ApiParser.CommonHttpReads(downstreamServiceName)

  def getEmployments(nino: Nino, taxYear: TaxYear)(implicit hc: HeaderCarrier): DownstreamOutcome[Option[AllEmploymentData]] = {
    val url = appConfig.getEmploymentSourceUrl(nino, taxYear)

    implicit val updatedHc: HeaderCarrier                            = headerCarrier(url)(hc)
    implicit val optRds: OptionalContentHttpReads[AllEmploymentData] = new OptionalContentHttpReads[AllEmploymentData]

    ConnectorRequestInfo("GET", url, downstreamServiceName)(updatedHc).logRequest(logger)
    http.GET[DownstreamErrorOr[Option[AllEmploymentData]]](url)(optRds, updatedHc, ec)
  }

  def saveEmployment(nino: Nino, taxYear: TaxYear, model: CreateUpdateEmploymentRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): DownstreamOutcome[Unit] = {
    val url = appConfig.getEmploymentSourceUrl(nino, taxYear)

    ConnectorRequestInfo("POST", url, downstreamServiceName).logRequestWithBody(logger, model, "Employment")
    http.POST[CreateUpdateEmploymentRequest, DownstreamErrorOr[Unit]](url, model)(CreateUpdateEmploymentRequest.format, parser, hc, ec)
  }

  // TODO LT employmentId - use type not string
  def deleteEmployment(nino: Nino, taxYear: TaxYear, employmentId: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): DownstreamOutcome[Unit] = {
    // TODO: Will there ever be HMRC-held employments?
    val source = "CUSTOMER"
    val url    = appConfig.getEmploymentUrl(nino, employmentId, source, taxYear)

    ConnectorRequestInfo("DELETE", url, downstreamServiceName).logRequest(logger)
    http.DELETE[DownstreamErrorOr[Unit]](url)(parser, hc, ec)
  }

}
