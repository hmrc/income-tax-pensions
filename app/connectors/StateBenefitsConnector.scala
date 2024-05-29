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

import config.AppConfig
import connectors.httpParsers.ApiParser
import connectors.httpParsers.GetStateBenefitsHttpParser.{GetStateBenefitsHttpReads, GetStateBenefitsResponse}
import models.AllStateBenefitsData
import models.common.{Nino, TaxYear}
import models.logging.ConnectorRequestInfo
import models.statebenefit.StateBenefitsUserData
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StateBenefitsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)(implicit ec: ExecutionContext) extends Connector {
  private val downstreamServiceName = "income-tax-state-benefits"
  private val parser                = ApiParser.CommonHttpReads(downstreamServiceName)

  def getStateBenefits(nino: Nino, taxYear: TaxYear)(implicit hc: HeaderCarrier): DownstreamOutcome[Option[AllStateBenefitsData]] = {
    val url = appConfig.stateBenefitsBaseUrl + s"/income-tax-state-benefits/benefits/nino/$nino/tax-year/$taxYear"

    implicit val updatedHc: HeaderCarrier                               = headerCarrier(url)(hc)
    implicit val optRds: OptionalContentHttpReads[AllStateBenefitsData] = new OptionalContentHttpReads[AllStateBenefitsData]

    ConnectorRequestInfo("GET", url, downstreamServiceName)(updatedHc).logRequest(logger)
    http.GET[DownstreamErrorOr[Option[AllStateBenefitsData]]](url)(optRds, updatedHc, ec)
  }

  def saveClaim(nino: Nino, model: StateBenefitsUserData)(implicit hc: HeaderCarrier): DownstreamOutcome[Unit] = {
    val url = appConfig.stateBenefitsBaseUrl + s"/income-tax-state-benefits/claim-data/nino/$nino"

    implicit val updatedHc: HeaderCarrier = headerCarrier(url)(hc)

    ConnectorRequestInfo("PUT", url, downstreamServiceName)(updatedHc).logRequestWithBody(logger, model, "StateBenefits")
    http.PUT[StateBenefitsUserData, DownstreamErrorOr[Unit]](url, model)(StateBenefitsUserData.format, parser, updatedHc, ec)
  }

  def deleteClaim(nino: Nino, taxYear: TaxYear, benefitId: UUID)(implicit hc: HeaderCarrier, ec: ExecutionContext): DownstreamOutcome[Unit] = {
    val url = appConfig.stateBenefitsBaseUrl + s"/income-tax-state-benefits/claim-data/nino/$nino/$taxYear/$benefitId/remove"

    implicit val updatedHc: HeaderCarrier = headerCarrier(url)(hc)

    ConnectorRequestInfo("DELETE", url, downstreamServiceName)(updatedHc).logRequest(logger)
    http.DELETE[DownstreamErrorOr[Unit]](url)(parser, updatedHc, ec)
  }
}
