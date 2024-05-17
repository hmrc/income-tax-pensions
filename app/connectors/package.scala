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

import connectors.httpParsers.ApiParser.CommonDownstreamParser
import models.ServiceErrorModel
import models.logging.ConnectorResponseInfo
import play.api.Logger
import play.api.http.Status.NO_CONTENT
import play.api.libs.json._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

import scala.concurrent.Future

package object connectors {
  type DownstreamErrorOr[A] = Either[ServiceErrorModel, A]
  type DownstreamOutcome[A] = Future[Either[ServiceErrorModel, A]]

  implicit def httpReads[A: Reads](implicit logger: Logger): HttpReads[DownstreamErrorOr[A]] =
    (method: String, url: String, response: HttpResponse) => {
      ConnectorResponseInfo(method, url, response).logResponseWarnOn4xx(logger)

      def validateJson =
        response.json
          .validate[A]
          .fold[Either[ServiceErrorModel, A]](
            errors => Left(createCommonErrorParser(method, url, response).reportInvalidJsonError(errors.toList)),
            parsedModel => Right(parsedModel)
          )

      for {
        _ <- createCommonErrorParser(method, url, response).logPagerDutyAlertOnError
        a <- validateJson
      } yield a
    }

  private def createCommonErrorParser(method: String, url: String, response: HttpResponse): CommonDownstreamParser =
    CommonDownstreamParser(method, url, response)

  def isSuccess(status: Int): Boolean = status >= 200 && status <= 299

  def isNoContent(status: Int): Boolean = status == NO_CONTENT
}
