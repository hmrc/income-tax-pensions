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
import connectors.httpParsers.RefreshIncomeSourceHttpParser.RefreshIncomeSourcesHttpReads
import models.RefreshIncomeSourceRequest
import models.logging.ConnectorRequestInfo
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SubmissionConnector @Inject() (val http: HttpClient, val config: AppConfig)(implicit ec: ExecutionContext) extends Logging {

  def refreshPensionsResponse(nino: String, mtditid: String, taxYear: Int)(implicit hc: HeaderCarrier): DownstreamOutcome[Unit] =
    refreshPensionsResponse(taxYear, nino)(
      Connector.headerCarrier(isInternalHost = true, ("mtditid", mtditid))
    )

  private def refreshPensionsResponse(taxYear: Int, nino: String)(implicit hc: HeaderCarrier): DownstreamOutcome[Unit] = {
    val url   = config.submissionBaseUrl + s"/income-tax/nino/$nino/sources/session?taxYear=$taxYear"
    val model = RefreshIncomeSourceRequest("pensions")
    ConnectorRequestInfo("PUT", url, "income-tax-submission").logRequestWithBody(logger, model, "Submission")
    http.PUT[RefreshIncomeSourceRequest, DownstreamErrorOr[Unit]](url, model)
  }
}
