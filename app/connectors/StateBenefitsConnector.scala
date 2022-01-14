/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.httpParsers.GetStateBenefitsHttpParser.{GetStateBenefitsHttpReads, GetStateBenefitsResponse}
import connectors.httpParsers.DeleteOverrideStateBenefitHttpParser.{DeleteOverrideStateBenefitHttpReads, DeleteStateBenefitOverrideResponse}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.DESTaxYearHelper.desTaxYearConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class StateBenefitsConnector @Inject()(val http: HttpClient,
                                       val appConfig: AppConfig)(implicit ec: ExecutionContext) extends DesConnector {

  private def stateBenefitsIncomeSourceUri(nino: String, taxYear: Int, benefitId: Option[String]): String = {
    val queryString = benefitId.fold("")(benefitId => s"?benefitId=$benefitId")
    appConfig.desBaseUrl + s"/income-tax/income/state-benefits/$nino/${desTaxYearConverter(taxYear)}$queryString"
  }

  def getStateBenefits(nino: String, taxYear: Int, benefitId: Option[String])(implicit hc: HeaderCarrier): Future[GetStateBenefitsResponse] = {
    val incomeSourcesUri: String = stateBenefitsIncomeSourceUri(nino, taxYear, benefitId)

    def desCall(implicit hc: HeaderCarrier): Future[GetStateBenefitsResponse] = {
      http.GET[GetStateBenefitsResponse](incomeSourcesUri)
    }

    desCall(desHeaderCarrier(incomeSourcesUri))
  }

  def deleteOverrideStateBenefit(nino: String, taxYear: Int, benefitId: String)(implicit hc: HeaderCarrier): Future[DeleteStateBenefitOverrideResponse] = {
    val incomeSourceUri: String = appConfig.desBaseUrl + s"/income-tax/income/state-benefits/$nino/${desTaxYearConverter(taxYear)}/$benefitId"

    def desCall(implicit hc: HeaderCarrier): Future[DeleteStateBenefitOverrideResponse] = {
      http.DELETE[DeleteStateBenefitOverrideResponse](incomeSourceUri)(DeleteOverrideStateBenefitHttpReads, hc, ec)
    }
    desCall(desHeaderCarrier(incomeSourceUri))
  }
}
