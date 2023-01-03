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
import connectors.httpParsers.CreateOrAmendPensionReliefsHttpParser.CreateOrAmendPensionReliefsResponse
import connectors.httpParsers.DeletePensionReliefsHttpParser.{DeletePensionReliefsHttpReads, DeletePensionReliefsResponse}
import connectors.httpParsers.GetPensionReliefsHttpParser.{GetPensionReliefsHttpReads, GetPensionReliefsResponse}
import models.CreateOrUpdatePensionReliefsModel
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.DESTaxYearHelper.desTaxYearConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PensionReliefsConnector @Inject()(val http: HttpClient,
                                        val appConfig: AppConfig)(implicit ec: ExecutionContext) extends DesConnector {

  def pensionReliefsIncomeSourceUri(nino: String, taxYear: Int): String =
    appConfig.desBaseUrl + s"/income-tax/reliefs/pensions/$nino/${desTaxYearConverter(taxYear)}"

  def getPensionReliefs(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionReliefsResponse] = {
    val incomeSourcesUri: String = pensionReliefsIncomeSourceUri(nino, taxYear)

    def desCall(implicit hc: HeaderCarrier): Future[GetPensionReliefsResponse] = {
      http.GET[GetPensionReliefsResponse](incomeSourcesUri)
    }

    desCall(desHeaderCarrier(incomeSourcesUri))
  }

  def createOrAmendPensionReliefs(nino: String, taxYear: Int, pensionReliefs: CreateOrUpdatePensionReliefsModel)
                                 (implicit hc: HeaderCarrier): Future[CreateOrAmendPensionReliefsResponse] = {

    val incomeSourcesUri: String = pensionReliefsIncomeSourceUri(nino, taxYear)

    import connectors.httpParsers.CreateOrAmendPensionReliefsHttpParser.{CreateOrAmendPensionReliefsHttpReads, CreateOrAmendPensionReliefsResponse}

    def desCall(implicit hc: HeaderCarrier): Future[CreateOrAmendPensionReliefsResponse] = {
      http.PUT[CreateOrUpdatePensionReliefsModel,
        CreateOrAmendPensionReliefsResponse](incomeSourcesUri,
        pensionReliefs)(CreateOrUpdatePensionReliefsModel.format.writes, CreateOrAmendPensionReliefsHttpReads, hc, ec)
    }

    desCall(desHeaderCarrier(incomeSourcesUri))
  }

  def deletePensionReliefs(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[DeletePensionReliefsResponse] = {
    val incomeSourceUri: String = pensionReliefsIncomeSourceUri(nino, taxYear)

    def desCall(implicit hc: HeaderCarrier): Future[DeletePensionReliefsResponse] = {
      http.DELETE[DeletePensionReliefsResponse](incomeSourceUri)(DeletePensionReliefsHttpReads, hc, ec)
    }

    desCall(desHeaderCarrier(incomeSourceUri))

  }
}
