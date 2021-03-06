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
import connectors.httpParsers.CreateUpdatePensionChargesHttpParser.{CreateUpdatePensionChargesHttpReads, CreateUpdatePensionChargesResponse}
import connectors.httpParsers.DeletePensionChargesHttpParser.{DeletePensionChargesHttpReads, DeletePensionChargesResponse}
import connectors.httpParsers.GetPensionChargesHttpParser.{GetPensionChargesHttpReads, GetPensionChargesResponse}
import models.CreateUpdatePensionChargesRequestModel
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.DESTaxYearHelper.desTaxYearConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PensionChargesConnector @Inject()(val http: HttpClient,
                                        val appConfig: AppConfig)(implicit ec: ExecutionContext) extends DesConnector {

  def pensionChargesIncomeSourceUri(nino: String, taxYear: Int): String =
    appConfig.desBaseUrl + s"/income-tax/charges/pensions/$nino/${desTaxYearConverter(taxYear)}"

  def getPensionCharges(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionChargesResponse] = {

    val incomeSourceUri: String = pensionChargesIncomeSourceUri(nino, taxYear)

    def desCall(implicit hc: HeaderCarrier): Future[GetPensionChargesResponse] = {
      http.GET[GetPensionChargesResponse](incomeSourceUri)
    }

    desCall(desHeaderCarrier(incomeSourceUri))
  }

  def deletePensionCharges(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[DeletePensionChargesResponse] = {

    val incomeSourceUri: String = pensionChargesIncomeSourceUri(nino, taxYear)

    def desCall(implicit hc: HeaderCarrier): Future[DeletePensionChargesResponse] = {
      http.DELETE[DeletePensionChargesResponse](incomeSourceUri)(DeletePensionChargesHttpReads, hc, ec)
    }

    desCall(desHeaderCarrier(incomeSourceUri))
  }

  def createUpdatePensionCharges(nino: String, taxYear: Int, model: CreateUpdatePensionChargesRequestModel)
                                (implicit hc: HeaderCarrier): Future[CreateUpdatePensionChargesResponse] = {

    val incomeSourceUri: String = pensionChargesIncomeSourceUri(nino, taxYear)

    def desCall(implicit hc: HeaderCarrier): Future[CreateUpdatePensionChargesResponse] = {
      http.PUT[CreateUpdatePensionChargesRequestModel, CreateUpdatePensionChargesResponse](incomeSourceUri,
        model)(CreateUpdatePensionChargesRequestModel.format.writes, CreateUpdatePensionChargesHttpReads, hc, ec)
    }
    desCall(desHeaderCarrier(incomeSourceUri))
  }
}
