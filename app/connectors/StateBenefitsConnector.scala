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

import cats.data.EitherT
import config.AppConfig
import connectors.httpParsers.ApiParser
import models.AllStateBenefitsData
import models.common.{Nino, TaxYear}
import models.domain.ApiResultT
import models.logging.ConnectorRequestInfo
import models.statebenefit.StateBenefitsUserData
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait StateBenefitsConnector {
  def getStateBenefits(nino: Nino, taxYear: TaxYear)(implicit hc: HeaderCarrier): ApiResultT[Option[AllStateBenefitsData]]

  def saveClaim(nino: Nino, model: StateBenefitsUserData)(implicit hc: HeaderCarrier): ApiResultT[Unit]

  def deleteClaim(nino: Nino, taxYear: TaxYear, benefitId: UUID)(implicit hc: HeaderCarrier): ApiResultT[Unit]
}

@Singleton
class StateBenefitsConnectorImpl @Inject()(val http: HttpClientV2, val appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends StateBenefitsConnector
    with Connector {
  private val downstreamServiceName = "income-tax-state-benefits"
  private val parser                = ApiParser.CommonHttpReads(downstreamServiceName)

  def getStateBenefits(nino: Nino, taxYear: TaxYear)(implicit hc: HeaderCarrier): ApiResultT[Option[AllStateBenefitsData]] = {
    val url = url"${appConfig.stateBenefitsBaseUrl + s"/income-tax-state-benefits/benefits/nino/$nino/tax-year/$taxYear"}"

    implicit val updatedHc: HeaderCarrier                               = headerCarrier(url.toString)(hc)
    implicit val optRds: OptionalContentHttpReads[AllStateBenefitsData] = new OptionalContentHttpReads[AllStateBenefitsData]

    ConnectorRequestInfo("GET", url.toString, downstreamServiceName)(updatedHc).logRequest(logger)
    EitherT(http.get(url)(updatedHc).execute)
      .leftMap(err => err.toServiceError)
  }

  def saveClaim(nino: Nino, model: StateBenefitsUserData)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val url = url"${appConfig.stateBenefitsBaseUrl + s"/income-tax-state-benefits/claim-data/nino/$nino"}"

    implicit val updatedHc: HeaderCarrier = headerCarrier(url.toString)(hc)
    val updatedModel                      = model.copy(claim = model.claim.map(_.copy(submittedOn = Some(Instant.now()))))

    EitherT(http.put(url)(updatedHc).withBody(Json.toJson(updatedModel)).execute(parser, ec))
      .leftMap(err => err.toServiceError)
  }

  def deleteClaim(nino: Nino, taxYear: TaxYear, benefitId: UUID)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val url = url"${appConfig.stateBenefitsBaseUrl}/income-tax-state-benefits/claim-data/nino/$nino/$taxYear/$benefitId/remove"

    implicit val updatedHc: HeaderCarrier = headerCarrier(url.toString)(hc)

    ConnectorRequestInfo("DELETE", url.toString, downstreamServiceName)(updatedHc).logRequest(logger)
    EitherT(http.delete(url)(updatedHc).execute(parser, ec))
      .leftMap(err => err.toServiceError)
  }
}
