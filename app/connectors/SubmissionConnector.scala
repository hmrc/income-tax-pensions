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
import connectors.httpParsers.RefreshIncomeSourceHttpParser.{RefreshIncomeSourceResponse, RefreshIncomeSourcesHttpReads}
import models.RefreshIncomeSourceRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmissionConnector @Inject()(val http: HttpClient,
                                    val config: AppConfig)(implicit ec: ExecutionContext) {


  def refreshPensionsResponse(nino: String, mtditid: String, taxYear: Int)
                             (implicit hc: HeaderCarrier): Future[RefreshIncomeSourceResponse] = {
    refreshPensionsResponse(taxYear, nino)(hc.withExtraHeaders(("mtditid", mtditid)))
  }


  private def refreshPensionsResponse(taxYear: Int, nino: String)
                                          (implicit hc: HeaderCarrier): Future[RefreshIncomeSourceResponse] = {
    val url = config.submissionBaseUrl + s"/income-tax/nino/$nino/sources/session?taxYear=$taxYear"
    http.PUT[RefreshIncomeSourceRequest, RefreshIncomeSourceResponse](url, RefreshIncomeSourceRequest("pensions"))
  }
}
