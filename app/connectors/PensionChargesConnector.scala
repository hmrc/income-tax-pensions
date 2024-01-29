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
import connectors.PensionChargesConnector.PensionChargesBaseApi
import connectors.httpParsers.CreateUpdatePensionChargesHttpParser.{CreateUpdatePensionChargesHttpReads, CreateUpdatePensionChargesResponse}
import connectors.httpParsers.DeletePensionChargesHttpParser.{DeletePensionChargesHttpReads, DeletePensionChargesResponse}
import connectors.httpParsers.GetPensionChargesHttpParser.{GetPensionChargesHttpReads, GetPensionChargesResponse}
import models.CreateUpdatePensionChargesRequestModel
import models.logging.ConnectorRequestInfo
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.TaxYearHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import cats.implicits._

class PensionChargesConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)(implicit ec: ExecutionContext) extends DesIFConnector {

  private def pensionChargesIncomeSourceUri(nino: String, taxYear: Int, baseApiNum: String): String =
    appConfig.ifBaseUrl + s"/income-tax/charges/pensions/${TaxYearHelper.apiPath(nino, taxYear, baseApiNum)}"

  def getPensionCharges(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionChargesResponse] = {
    val incomeSourceUri: String = pensionChargesIncomeSourceUri(nino, taxYear, PensionChargesBaseApi.Get)
    val apiNumber               = TaxYearHelper.apiVersion(taxYear, PensionChargesBaseApi.Get)

    def desIfCall(implicit hc: HeaderCarrier): Future[GetPensionChargesResponse] = {
      ConnectorRequestInfo("GET", incomeSourceUri, apiNumber).logRequest(logger)
      http.GET[GetPensionChargesResponse](incomeSourceUri)
    }

    desIfCall(integrationFrameworkHeaderCarrier(incomeSourceUri, apiNumber))
  }

  def deletePensionCharges(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[DeletePensionChargesResponse] = {
    val incomeSourceUri: String = pensionChargesIncomeSourceUri(nino, taxYear, PensionChargesBaseApi.Delete)
    val apiNumber               = TaxYearHelper.apiVersion(taxYear, PensionChargesBaseApi.Delete)

    def desIfCall(implicit hc: HeaderCarrier): Future[DeletePensionChargesResponse] = {
      ConnectorRequestInfo("DELETE", incomeSourceUri, apiNumber).logRequest(logger)
      http.DELETE[DeletePensionChargesResponse](incomeSourceUri)(DeletePensionChargesHttpReads, hc, ec)
    }

    desIfCall(integrationFrameworkHeaderCarrier(incomeSourceUri, apiNumber))
  }

  def createUpdatePensionCharges(nino: String, taxYear: Int, model: CreateUpdatePensionChargesRequestModel)(implicit
      hc: HeaderCarrier): Future[CreateUpdatePensionChargesResponse] = {
    val incomeSourceUri: String = pensionChargesIncomeSourceUri(nino, taxYear, PensionChargesBaseApi.Update)
    val apiNumber               = TaxYearHelper.apiVersion(taxYear, PensionChargesBaseApi.Update)

    def desIfCall(implicit hc: HeaderCarrier): Future[CreateUpdatePensionChargesResponse] = {
      ConnectorRequestInfo("PUT", incomeSourceUri, apiNumber).logRequestWithBody(logger, model)
      http.PUT[CreateUpdatePensionChargesRequestModel, CreateUpdatePensionChargesResponse](incomeSourceUri, model)(
        CreateUpdatePensionChargesRequestModel.format.writes,
        CreateUpdatePensionChargesHttpReads,
        hc,
        ec)
    }

    desIfCall(integrationFrameworkHeaderCarrier(incomeSourceUri, apiNumber))
  }
}

object PensionChargesConnector {
  object PensionChargesBaseApi {
    val Get    = "1674"
    val Update = "1673"
    val Delete = "1675"
  }
}
