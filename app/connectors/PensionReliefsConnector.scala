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

import cats.implicits._
import config.AppConfig
import connectors.PensionReliefsConnector.PensionReliefsBaseApi
import connectors.httpParsers.CreateOrAmendPensionReliefsHttpParser.{CreateOrAmendPensionReliefsHttpReads, CreateOrAmendPensionReliefsResponse}
import connectors.httpParsers.DeletePensionReliefsHttpParser.{DeletePensionReliefsHttpReads, DeletePensionReliefsResponse}
import connectors.httpParsers.GetPensionReliefsHttpParser.{GetPensionReliefsHttpReads, GetPensionReliefsResponse}
import models.CreateOrUpdatePensionReliefsModel
import models.logging.ConnectorRequestInfo
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.TaxYearHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PensionReliefsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends DesIFConnector
    with Logging {
  private def pensionReliefsIfIncomeSourceUri(nino: String, taxYear: Int, apiNum: String): String =
    appConfig.ifBaseUrl + s"/income-tax/reliefs/pensions/${TaxYearHelper.apiPath(nino, taxYear, apiNum)}"

  private def pensionReliefsDesIncomeSourceUri(nino: String, taxYear: Int): String =
    appConfig.desBaseUrl + s"/income-tax/reliefs/pensions/$nino/${TaxYearHelper.desIfTaxYearConverter(taxYear)}"

  def getPensionReliefs(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionReliefsResponse] = {
    def desIfCall(incomeSourceUri: String)(implicit hc: HeaderCarrier): Future[GetPensionReliefsResponse] =
      http.GET[GetPensionReliefsResponse](incomeSourceUri)

    if (TaxYearHelper.isTysApi(taxYear, PensionReliefsBaseApi.Get)) {
      val incomeSourceUri: String = pensionReliefsIfIncomeSourceUri(nino, taxYear, PensionReliefsBaseApi.Get)
      val apiNumber               = TaxYearHelper.apiVersion(taxYear, PensionReliefsBaseApi.Get)
      ConnectorRequestInfo("GET", incomeSourceUri, apiNumber).logRequest(logger)
      desIfCall(incomeSourceUri)(integrationFrameworkHeaderCarrier(incomeSourceUri, apiNumber))
    } else {
      val incomeSourceUri: String = pensionReliefsDesIncomeSourceUri(nino, taxYear)
      ConnectorRequestInfo("GET", incomeSourceUri, "des").logRequest(logger)
      desIfCall(incomeSourceUri)(desHeaderCarrier(incomeSourceUri))
    }
  }

  def createOrAmendPensionReliefs(nino: String, taxYear: Int, pensionReliefs: CreateOrUpdatePensionReliefsModel)(implicit
      hc: HeaderCarrier): Future[CreateOrAmendPensionReliefsResponse] = {

    def desIfCall(incomeSourceUri: String, apiNumber: String)(implicit hc: HeaderCarrier): Future[CreateOrAmendPensionReliefsResponse] = {
      ConnectorRequestInfo("PUT", incomeSourceUri, apiNumber).logRequestWithBody(logger, pensionReliefs)
      http.PUT[CreateOrUpdatePensionReliefsModel, CreateOrAmendPensionReliefsResponse](incomeSourceUri, pensionReliefs)(
        CreateOrUpdatePensionReliefsModel.format.writes,
        CreateOrAmendPensionReliefsHttpReads,
        hc,
        ec)
    }

    if (TaxYearHelper.isTysApi(taxYear, PensionReliefsBaseApi.Update)) {
      val incomeSourceUri: String = pensionReliefsIfIncomeSourceUri(nino, taxYear, PensionReliefsBaseApi.Update)
      val apiNumber               = TaxYearHelper.apiVersion(taxYear, PensionReliefsBaseApi.Update)
      desIfCall(incomeSourceUri, apiNumber)(integrationFrameworkHeaderCarrier(incomeSourceUri, apiNumber))
    } else {
      val incomeSourceUri: String = pensionReliefsDesIncomeSourceUri(nino, taxYear)
      desIfCall(incomeSourceUri, "des")(desHeaderCarrier(incomeSourceUri))
    }
  }

  def deletePensionReliefs(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[DeletePensionReliefsResponse] = {
    def desIfCall(incomeSourceUri: String, apiNumber: String)(implicit hc: HeaderCarrier): Future[DeletePensionReliefsResponse] = {
      ConnectorRequestInfo("DELETE", incomeSourceUri, apiNumber).logRequest(logger)
      http.DELETE[DeletePensionReliefsResponse](incomeSourceUri)(DeletePensionReliefsHttpReads, hc, ec)
    }

    if (TaxYearHelper.isTysApi(taxYear, PensionReliefsBaseApi.Delete)) {
      val incomeSourceUri: String = pensionReliefsIfIncomeSourceUri(nino, taxYear, PensionReliefsBaseApi.Delete)
      val apiNumber               = TaxYearHelper.apiVersion(taxYear, PensionReliefsBaseApi.Delete)
      desIfCall(incomeSourceUri, apiNumber)(integrationFrameworkHeaderCarrier(incomeSourceUri, apiNumber))
    } else {
      val incomeSourceUri: String = pensionReliefsDesIncomeSourceUri(nino, taxYear)
      desIfCall(incomeSourceUri, "des")(desHeaderCarrier(incomeSourceUri))
    }
  }
}

object PensionReliefsConnector {
  object PensionReliefsBaseApi {
    val Get    = "1656"
    val Update = "1655"
    val Delete = "1657"
  }
}
