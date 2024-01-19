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
import connectors.PensionIncomeConnector.PensionIncomeBaseApi
import connectors.httpParsers.CreateOrAmendPensionIncomeHttpParser.{CreateOrAmendPensionIncomeHttpReads, CreateOrAmendPensionIncomeResponse}
import connectors.httpParsers.DeletePensionIncomeHttpParser.{DeletePensionIncomeHttpReads, DeletePensionIncomeResponse}
import connectors.httpParsers.GetPensionIncomeHttpParser.{GetPensionIncomeHttpReads, GetPensionIncomeResponse}
import models.CreateUpdatePensionIncomeModel
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.TaxYearHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PensionIncomeConnector @Inject()(val http: HttpClient,
                                       val appConfig: AppConfig)(implicit ec: ExecutionContext) extends DesIFConnector {
  private def pensionIncomeSourceUri(nino: String, taxYear: Int, apiNum: String): String =
    appConfig.ifBaseUrl + s"/income-tax/income/pensions/${TaxYearHelper.apiPath(nino, taxYear, apiNum)}"

  def getPensionIncome(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionIncomeResponse] = {
    val incomeSourceUri: String = pensionIncomeSourceUri(nino, taxYear, PensionIncomeBaseApi.Get)
    def integrationFrameworkCall(implicit hc: HeaderCarrier): Future[GetPensionIncomeResponse] = {
      http.GET[GetPensionIncomeResponse](incomeSourceUri)
    }
    integrationFrameworkCall(integrationFrameworkHeaderCarrier(incomeSourceUri, TaxYearHelper.apiVersion(taxYear,PensionIncomeBaseApi.Get)))
  }

  def createOrAmendPensionIncome(nino: String, taxYear: Int, pensionIncome: CreateUpdatePensionIncomeModel)
                                 (implicit hc: HeaderCarrier): Future[CreateOrAmendPensionIncomeResponse] = {

    val incomeSourceUri: String = pensionIncomeSourceUri(nino, taxYear, PensionIncomeBaseApi.Update)
    def integrationFrameworkCall(implicit hc: HeaderCarrier): Future[CreateOrAmendPensionIncomeResponse] = {
      http.PUT[CreateUpdatePensionIncomeModel,
        CreateOrAmendPensionIncomeResponse](incomeSourceUri,
        pensionIncome)(CreateUpdatePensionIncomeModel.format.writes, CreateOrAmendPensionIncomeHttpReads, hc, ec)
    }
    integrationFrameworkCall(integrationFrameworkHeaderCarrier(incomeSourceUri, TaxYearHelper.apiVersion(taxYear,PensionIncomeBaseApi.Update)))
  }

  def deletePensionIncome(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[DeletePensionIncomeResponse] = {
    val incomeSourceUri: String = pensionIncomeSourceUri(nino, taxYear, PensionIncomeBaseApi.Delete)
    def integrationFrameworkCall(implicit hc: HeaderCarrier): Future[DeletePensionIncomeResponse] = {
      http.DELETE[DeletePensionIncomeResponse](incomeSourceUri)(DeletePensionIncomeHttpReads, hc, ec)
    }
    integrationFrameworkCall(integrationFrameworkHeaderCarrier(incomeSourceUri, TaxYearHelper.apiVersion(taxYear,PensionIncomeBaseApi.Delete)))
  }
}

object PensionIncomeConnector {
  object PensionIncomeBaseApi {
    val Get = "1612"
    val Update = "1611"
    val Delete = "1613"
  }
}
