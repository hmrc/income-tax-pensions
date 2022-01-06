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
import connectors.httpParsers.GetPensionReliefsHttpParser.{GetPensionReliefsHttpReads, GetPensionReliefsResponse}

import javax.inject.Inject
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import utils.DESTaxYearHelper.desTaxYearConverter

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
}
