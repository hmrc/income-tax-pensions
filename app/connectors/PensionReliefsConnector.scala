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
import connectors.PensionReliefsConnector.PensionReliefsBaseApi
import connectors.httpParsers.CreateOrAmendPensionReliefsHttpParser.{CreateOrAmendPensionReliefsHttpReads, CreateOrAmendPensionReliefsResponse}
import connectors.httpParsers.DeleteHttpParser.{DeleteHttpReads, DeleteResponse}
import connectors.httpParsers.GetPensionReliefsHttpParser.{GetPensionReliefsHttpReads, GetPensionReliefsResponse}
import models.common.{JourneyContextWithNino, Nino, TaxYear}
import models.domain.ApiResultT
import models.logging.ConnectorRequestInfo
import models.{CreateOrUpdatePensionReliefsModel, GetPensionReliefsModel}
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import utils.TaxYearHelper

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PensionReliefsConnector @Inject()(val http: HttpClientV2, val appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends DesIFConnector
    with Logging {
  private def pensionReliefsIfIncomeSourceUri(nino: String, taxYear: Int, apiNum: String): String =
    appConfig.ifBaseUrl + s"/income-tax/reliefs/pensions/${TaxYearHelper.apiPath(nino, taxYear, apiNum)}"

  private def pensionReliefsDesIncomeSourceUri(nino: String, taxYear: Int): String =
    appConfig.desBaseUrl + s"/income-tax/reliefs/pensions/$nino/${TaxYearHelper.taxYearConverter(taxYear)}"

  private def pensionReliefsHipIncomeSourceUri(nino: String, taxYear: Int): String =
    appConfig.hipBaseUrl + s"/income-tax/v1/reliefs/pensions/$nino/${TaxYearHelper.taxYearConverter(taxYear)}"

  def getPensionReliefsT(nino: Nino, taxYear: TaxYear)(implicit hc: HeaderCarrier): ApiResultT[Option[GetPensionReliefsModel]] = {
    val ans = getPensionReliefs(nino.value, taxYear.endYear)
    EitherT(ans).leftMap(err => err.toServiceError)
  }

  def getPensionReliefs(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionReliefsResponse] = {
    def call(incomeSourceUri: URL)(implicit hc: HeaderCarrier): Future[GetPensionReliefsResponse] =
      http.get(incomeSourceUri).execute[GetPensionReliefsResponse]

    if (TaxYearHelper.isTysApi(taxYear, PensionReliefsBaseApi.Get)) {
      val incomeSourceUri = url"${pensionReliefsIfIncomeSourceUri(nino, taxYear, PensionReliefsBaseApi.Get)}"
      val apiNumber               = TaxYearHelper.apiVersion(taxYear, PensionReliefsBaseApi.Get)
      call(incomeSourceUri)(integrationFrameworkHeaderCarrier(incomeSourceUri.toString, apiNumber))
    } else {
      val incomeSourceUri = url"${pensionReliefsHipIncomeSourceUri(nino, taxYear)}"
      call(incomeSourceUri)(hipHeaderCarrier(incomeSourceUri.toString, PensionReliefsBaseApi.Get))
    }
  }
  def createOrAmendPensionReliefsT(ctx: JourneyContextWithNino, pensionReliefs: CreateOrUpdatePensionReliefsModel)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val ans = createOrAmendPensionReliefs(ctx.nino.value, ctx.taxYear.endYear, pensionReliefs)
    EitherT(ans).leftMap(err => err.toServiceError)
  }

  def createOrAmendPensionReliefs(nino: String, taxYear: Int, pensionReliefs: CreateOrUpdatePensionReliefsModel)(implicit
      hc: HeaderCarrier): Future[CreateOrAmendPensionReliefsResponse] = {

    def call(incomeSourceUri: URL, apiNumber: String)(implicit hc: HeaderCarrier): Future[CreateOrAmendPensionReliefsResponse] = {
      ConnectorRequestInfo("PUT", incomeSourceUri.toString, apiNumber).logRequest(logger)
      http.put(incomeSourceUri).withBody(Json.toJson(pensionReliefs)).execute(CreateOrAmendPensionReliefsHttpReads, ec)
    }

    if (TaxYearHelper.isTysApi(taxYear, PensionReliefsBaseApi.Update)) {
      val incomeSourceUri: URL    = url"${pensionReliefsIfIncomeSourceUri(nino, taxYear, PensionReliefsBaseApi.Update)}"
      val apiNumber               = TaxYearHelper.apiVersion(taxYear, PensionReliefsBaseApi.Update)
      call(incomeSourceUri, apiNumber)(integrationFrameworkHeaderCarrier(incomeSourceUri.toString, apiNumber))
    } else {
      val incomeSourceUri: URL = url"${pensionReliefsDesIncomeSourceUri(nino, taxYear)}"
      call(incomeSourceUri, "des")(desHeaderCarrier(incomeSourceUri.toString))
    }
  }

  def deletePensionReliefsT(nino: Nino, taxYear: TaxYear)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val ans = deletePensionReliefs(nino.value, taxYear.endYear)
    EitherT(ans).leftMap(err => err.toServiceError)
  }

  def deletePensionReliefs(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[DeleteResponse] = {
    def call(incomeSourceUri: URL, apiNumber: String)(implicit hc: HeaderCarrier): Future[DeleteResponse] = {
      ConnectorRequestInfo("DELETE", incomeSourceUri.toString, apiNumber).logRequest(logger)
      http.delete(incomeSourceUri).execute(DeleteHttpReads, ec)
    }

    if (TaxYearHelper.isTysApi(taxYear, PensionReliefsBaseApi.Delete)) {
      val incomeSourceUri = url"${pensionReliefsIfIncomeSourceUri(nino, taxYear, PensionReliefsBaseApi.Delete)}"
      val apiNumber               = TaxYearHelper.apiVersion(taxYear, PensionReliefsBaseApi.Delete)
      call(incomeSourceUri, apiNumber)(integrationFrameworkHeaderCarrier(incomeSourceUri.toString, apiNumber))
    } else {
      val incomeSourceUri = url"${pensionReliefsDesIncomeSourceUri(nino, taxYear)}"
      call(incomeSourceUri, "des")(desHeaderCarrier(incomeSourceUri.toString))
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
