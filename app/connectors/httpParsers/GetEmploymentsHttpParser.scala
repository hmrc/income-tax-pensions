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

package connectors.httpParsers

import models.{DesErrorBodyModel, DesErrorModel, GetEmploymentPensionsModel}
import play.api.Logging
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object GetEmploymentsHttpParser extends Logging {
  type GetEmploymentsResponse = Either[DesErrorModel, Option[GetEmploymentPensionsModel]]

  val parserName: String = "GetEmploymentsHttpParser"

  implicit object GetEmploymentsHttpReads extends HttpReads[GetEmploymentsResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetEmploymentsResponse = {
      response.status match {
        case OK => response.json.validate[GetEmploymentPensionsModel].fold[GetEmploymentsResponse](
          invalid => {
            logger.warn(s"[$parserName][read] Invalid Json - $invalid")
            Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("PARSING_ERROR", "Error parsing response")))
          },
          valid =>
            Right(Some(GetEmploymentPensionsModel(
              valid.hmrcEmploymentData.filter(_.occPen.contains(true)),
              valid.customerEmploymentData.filter(_.occPen.contains(true))
            )))
        )
        case NO_CONTENT => Right(None)
        case status =>
          logger.warn(s"[$parserName][read] Received status: $status from income-tax-employment")
          Left(DesErrorModel(status, DesErrorBodyModel(s"$status", "Error returned when attempting to retrieve employment details")))
      }
    }
  }
}
