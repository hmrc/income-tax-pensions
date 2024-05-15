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

import cats.implicits._
import models.{APIErrorBodyModel, APIErrorModel, ServiceErrorModel}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.Reads
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.Logging

import scala.util.{Failure, Success, Try}

class ContentHttpReads[A: Reads] extends HttpReads[DownstreamErrorOr[A]] {

  override def read(method: String, url: String, response: HttpResponse): DownstreamErrorOr[A] =
    if (isSuccess(response.status)) {
      ContentHttpReads.readOne[A](method, url, response)
    } else {
      ??? // TODO LT fix me
    }
}

object ContentHttpReads extends Logging {
  def readOne[A: Reads](method: String, url: String, response: HttpResponse): Either[ServiceErrorModel, A] = {
    val validated = Try(response.json.validate[A].asEither)

    validated match {
      case Success(validatedRes) =>
        validatedRes.fold(
          { err =>
            logger.error(s"Error on validating JSON response: ${err.toList.mkString("\n")}")
//            APIParser.badSuccessJsonFromAPI("ContentHttpReads", s"$method $url")
            // TODO LT fix me
            ???
          },
          a => a.asRight
        )

      case Failure(err) =>
        logger.error(s"Error on parsing JSON response", err)
        APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel.parsingError).asLeft
    }
  }
}
