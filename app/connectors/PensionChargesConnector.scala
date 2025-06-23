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
import models.charges.{CreateUpdatePensionChargesRequestModel, GetPensionChargesRequestModel}
import models.common.{JourneyContextWithNino, Nino, TaxYear}
import models.domain.ApiResultT
import models.logging.ConnectorRequestInfo
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import utils.TaxYearHelper

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PensionChargesConnector @Inject()(val http: HttpClientV2, val appConfig: AppConfig)(implicit ec: ExecutionContext) extends DesIFConnector {

  private def pensionChargesIncomeSourceUri(nino: String, taxYear: Int, baseApiNum: String): String =
    appConfig.ifBaseUrl + s"/income-tax/charges/pensions/${TaxYearHelper.apiPath(nino, taxYear, baseApiNum)}"
  // TODO refactor method above to remove repeat if needed, else delete once all journeys are upgrade
  private def pensionChargesIfsIncomeSourceUri(nino: String, taxYear: Int, apiNum: String): String =
    appConfig.ifBaseUrl + s"/income-tax/charges/pensions/${TaxYearHelper.apiPath(nino, taxYear, apiNum)}"

  private def pensionChargesDesIncomeSourceUri(nino: String, taxYear: Int): String =
    appConfig.desBaseUrl + s"/income-tax/charges/pensions/$nino/${TaxYearHelper.taxYearConverter(taxYear)}"

  def getPensionChargesT(nino: Nino, taxYear: TaxYear)(implicit hc: HeaderCarrier): ApiResultT[Option[GetPensionChargesRequestModel]] = {
    val ans = getPensionCharges(nino.value, taxYear.endYear)
    EitherT(ans).leftMap(err => err.toServiceError)
  }

  def getPensionCharges(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionChargesResponse] = {
    val incomeSourceUri = url"${pensionChargesIncomeSourceUri(nino, taxYear, PensionChargesBaseApi.Get)}"
    val apiNumber               = TaxYearHelper.apiVersion(taxYear, PensionChargesBaseApi.Get)

    def desIfCall(implicit hc: HeaderCarrier): Future[GetPensionChargesResponse] = {
      ConnectorRequestInfo("GET", incomeSourceUri.toString, apiNumber).logRequest(logger)

      http.get(incomeSourceUri).execute[GetPensionChargesResponse]
    }

    desIfCall(integrationFrameworkHeaderCarrier(incomeSourceUri.toString, apiNumber))
  }

  def deletePensionChargesT(nino: Nino, taxYear: TaxYear)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val ans = deletePensionCharges(nino.value, taxYear.endYear)
    EitherT(ans).leftMap(err => err.toServiceError)
  }

  def deletePensionCharges(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[DeletePensionChargesResponse] = {
    val incomeSourceUri = url"${pensionChargesIncomeSourceUri(nino, taxYear, PensionChargesBaseApi.Delete)}"
    val apiNumber               = TaxYearHelper.apiVersion(taxYear, PensionChargesBaseApi.Delete)

    def desIfCall(implicit hc: HeaderCarrier): Future[DeletePensionChargesResponse] = {
      ConnectorRequestInfo("DELETE", incomeSourceUri.toString, apiNumber).logRequest(logger)

      http.delete(incomeSourceUri).execute[DeletePensionChargesResponse](DeletePensionChargesHttpReads, ec)
    }

    desIfCall(integrationFrameworkHeaderCarrier(incomeSourceUri.toString, apiNumber))
  }

  def createUpdatePensionChargesT(ctx: JourneyContextWithNino, pensionCharges: CreateUpdatePensionChargesRequestModel)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val ans = createUpdatePensionCharges(ctx.nino.value, ctx.taxYear.endYear, pensionCharges)
    EitherT(ans).leftMap(err => err.toServiceError)
  }

  def createUpdatePensionCharges(nino: String, taxYear: Int, model: CreateUpdatePensionChargesRequestModel)(implicit
      hc: HeaderCarrier): Future[CreateUpdatePensionChargesResponse] = {

    def call(incomeSourceUri: URL, apiNumber: String)(implicit hc: HeaderCarrier): Future[CreateUpdatePensionChargesResponse] = {
      ConnectorRequestInfo("PUT", incomeSourceUri.toString, apiNumber).logRequest(logger)

      http.put(incomeSourceUri)
        .withBody(Json.toJson(model))
        .execute(CreateUpdatePensionChargesHttpReads, ec)
    }

    if (TaxYearHelper.isTysApi(taxYear, PensionChargesBaseApi.Update)) {
      val incomeSourceUri = url"${pensionChargesIfsIncomeSourceUri(nino, taxYear, PensionChargesBaseApi.Update)}"
      val apiNumber       = TaxYearHelper.apiVersion(taxYear, PensionChargesBaseApi.Update)
      call(incomeSourceUri, apiNumber)(integrationFrameworkHeaderCarrier(incomeSourceUri.toString, apiNumber))
    } else {
      val incomeSourceUri = url"${pensionChargesDesIncomeSourceUri(nino, taxYear)}"
      call(incomeSourceUri, "des")(desHeaderCarrier(incomeSourceUri.toString))
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
