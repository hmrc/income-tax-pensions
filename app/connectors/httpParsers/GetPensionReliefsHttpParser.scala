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

package connectors.httpParsers

import models.logging.ConnectorResponseInfo
import models.{DesErrorBodyModel, DesErrorModel, GetPensionReliefsModel}
import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.PagerDutyHelper.PagerDutyKeys._
import utils.PagerDutyHelper.pagerDutyLog

object GetPensionReliefsHttpParser extends DESParser {
  type GetPensionReliefsResponse = Either[DesErrorModel, Option[GetPensionReliefsModel]]

  override val parserName: String = "GetPensionReliefsHttpParser"

  implicit object GetPensionReliefsHttpReads extends HttpReads[GetPensionReliefsResponse] {

    override def read(method: String, url: String, response: HttpResponse): GetPensionReliefsResponse = {
      ConnectorResponseInfo(method, url, response).logResponseWarnOn4xx(logger)

      response.status match {
        case OK =>
          response.json
            .validate[GetPensionReliefsModel]
            .fold[GetPensionReliefsResponse](
              jsonErrors => {
                pagerDutyLog(BAD_SUCCESS_JSON_FROM_DES, s"[GetPensionReliefsHttParser][read] Invalid Json from DES.")
                Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError))
              },
              parsedModel => Right(Some(parsedModel))
            )
        case NOT_FOUND => Right(None)
        case INTERNAL_SERVER_ERROR =>
          pagerDutyLog(INTERNAL_SERVER_ERROR_FROM_DES, logMessage(method, url, response))
          handleDESError(response)
        case SERVICE_UNAVAILABLE =>
          pagerDutyLog(SERVICE_UNAVAILABLE_FROM_DES, logMessage(method, url, response))
          handleDESError(response)
        case BAD_REQUEST =>
          pagerDutyLog(FOURXX_RESPONSE_FROM_DES, logMessage(method, url, response))
          handleDESError(response)
        case _ =>
          pagerDutyLog(UNEXPECTED_RESPONSE_FROM_DES, logMessage(method, url, response))
          handleDESError(response, Some(INTERNAL_SERVER_ERROR))
      }
    }
  }
}
