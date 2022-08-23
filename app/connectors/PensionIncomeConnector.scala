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
import connectors.httpParsers.CreateOrAmendPensionIncomeHttpParser.CreateOrAmendPensionIncomeResponse
import connectors.httpParsers.DeletePensionIncomeHttpParser.{DeletePensionIncomeHttpReads, DeletePensionIncomeResponse}
import connectors.httpParsers.GetPensionIncomeHttpParser.{GetPensionIncomeHttpReads, GetPensionIncomeResponse}
import models.CreateUpdatePensionIncomeModel
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.DESTaxYearHelper.desTaxYearConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PensionIncomeConnector @Inject()(val http: HttpClient,
                                       val appConfig: AppConfig)(implicit ec: ExecutionContext) extends IFConnector {

  object PensionIncomeConnectorApiNumbers {
    val Get = "1612"
    val Update = "1611"
    val Delete = "1613"
  }

  def pensionIncomeSourceUri(nino: String, taxYear: Int): String =
    appConfig.integrationFrameworkBaseUrl + s"/income-tax/income/pensions/$nino/${desTaxYearConverter(taxYear)}"

  def getPensionIncome(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionIncomeResponse] = {
    val incomeSourcesUri: String = pensionIncomeSourceUri(nino, taxYear)

    def integrationFrameworkCall(implicit hc: HeaderCarrier): Future[GetPensionIncomeResponse] = {
      http.GET[GetPensionIncomeResponse](incomeSourcesUri)
    }

    integrationFrameworkCall(integrationFrameworkHeaderCarrier(incomeSourcesUri, PensionIncomeConnectorApiNumbers.Get))
  }

  def createOrAmendPensionIncome(nino: String, taxYear: Int, pensionIncome: CreateUpdatePensionIncomeModel)
                                 (implicit hc: HeaderCarrier): Future[CreateOrAmendPensionIncomeResponse] = {

    val incomeSourcesUri: String = pensionIncomeSourceUri(nino, taxYear)

    import connectors.httpParsers.CreateOrAmendPensionIncomeHttpParser.{CreateOrAmendPensionIncomeHttpReads, CreateOrAmendPensionIncomeResponse}

    def integrationFrameworkCall(implicit hc: HeaderCarrier): Future[CreateOrAmendPensionIncomeResponse] = {
      http.PUT[CreateUpdatePensionIncomeModel,
        CreateOrAmendPensionIncomeResponse](incomeSourcesUri,
        pensionIncome)(CreateUpdatePensionIncomeModel.format.writes, CreateOrAmendPensionIncomeHttpReads, hc, ec)
    }

    integrationFrameworkCall(integrationFrameworkHeaderCarrier(incomeSourcesUri, PensionIncomeConnectorApiNumbers.Update))
  }

  def deletePensionIncome(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[DeletePensionIncomeResponse] = {
    val incomeSourceUri: String = pensionIncomeSourceUri(nino, taxYear)

    def integrationFrameworkCall(implicit hc: HeaderCarrier): Future[DeletePensionIncomeResponse] = {
      http.DELETE[DeletePensionIncomeResponse](incomeSourceUri)(DeletePensionIncomeHttpReads, hc, ec)
    }
    integrationFrameworkCall(integrationFrameworkHeaderCarrier(incomeSourceUri, PensionIncomeConnectorApiNumbers.Delete))
  }
}
