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
import connectors.PensionChargesConnector.PensionChargesBaseApi
import connectors.httpParsers.CreateUpdatePensionChargesHttpParser.{CreateUpdatePensionChargesHttpReads, CreateUpdatePensionChargesResponse}
import connectors.httpParsers.DeletePensionChargesHttpParser.{DeletePensionChargesHttpReads, DeletePensionChargesResponse}
import connectors.httpParsers.GetPensionChargesHttpParser.{GetPensionChargesHttpReads, GetPensionChargesResponse}
import models.common.{JourneyContextWithNino, Nino, TaxYear}
import models.domain.ApiResultT
import models.logging.ConnectorRequestInfo
import models.{CreateUpdatePensionChargesRequestModel, GetPensionChargesRequestModel}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.TaxYearHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PensionChargesConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)(implicit ec: ExecutionContext) extends DesIFConnector {

  private def pensionChargesIncomeSourceUri(nino: String, taxYear: Int, baseApiNum: String): String =
    appConfig.ifBaseUrl + s"/income-tax/charges/pensions/${TaxYearHelper.apiPath(nino, taxYear, baseApiNum)}"
  // TODO refactor method above to remove repeat if needed, else delete once all journeys are upgrade
  private def pensionChargesIfsIncomeSourceUri(nino: String, taxYear: Int, apiNum: String): String =
    appConfig.ifBaseUrl + s"/income-tax/charges/pensions/${TaxYearHelper.apiPath(nino, taxYear, apiNum)}"

  private def pensionChargesDesIncomeSourceUri(nino: String, taxYear: Int): String =
    appConfig.desBaseUrl + s"/income-tax/charges/pensions/$nino/${TaxYearHelper.desIfTaxYearConverter(taxYear)}"

  def getPensionChargesT(nino: Nino, taxYear: TaxYear)(implicit hc: HeaderCarrier): ApiResultT[Option[GetPensionChargesRequestModel]] = {
    val ans = getPensionCharges(nino.value, taxYear.endYear)
    EitherT(ans).leftMap(err => err.toServiceError)
  }

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

  def createUpdatePensionChargesT(ctx: JourneyContextWithNino, pensionCharges: CreateUpdatePensionChargesRequestModel)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val ans = createUpdatePensionCharges(ctx.nino.value, ctx.taxYear.endYear, pensionCharges)
    EitherT(ans).leftMap(err => err.toServiceError)
  }

  def createUpdatePensionCharges(nino: String, taxYear: Int, model: CreateUpdatePensionChargesRequestModel)(implicit
      hc: HeaderCarrier): Future[CreateUpdatePensionChargesResponse] = {

    def call(incomeSourceUri: String, apiNumber: String)(implicit hc: HeaderCarrier): Future[CreateUpdatePensionChargesResponse] = {
      ConnectorRequestInfo("PUT", incomeSourceUri, apiNumber).logRequestWithBody(logger, model, "Charges")
      http.PUT[CreateUpdatePensionChargesRequestModel, CreateUpdatePensionChargesResponse](incomeSourceUri, model)(
        charges => CreateUpdatePensionChargesRequestModel.format.writes(charges),
        CreateUpdatePensionChargesHttpReads,
        hc,
        ec)
    }

    if (TaxYearHelper.isTysApi(taxYear, PensionChargesBaseApi.Update)) {
      val incomeSourceUri = pensionChargesIfsIncomeSourceUri(nino, taxYear, PensionChargesBaseApi.Update)
      val apiNumber       = TaxYearHelper.apiVersion(taxYear, PensionChargesBaseApi.Update)
      call(incomeSourceUri, apiNumber)(integrationFrameworkHeaderCarrier(incomeSourceUri, apiNumber))
    } else {
      val incomeSourceUri: String = pensionChargesDesIncomeSourceUri(nino, taxYear)
      call(incomeSourceUri, "des")(desHeaderCarrier(incomeSourceUri))
    }
  }
}

object PensionChargesConnector {
  object PensionChargesBaseApi {
    val Get    = "1674"
    val Update = "1673"
    val Delete = "1675"
  }
}
