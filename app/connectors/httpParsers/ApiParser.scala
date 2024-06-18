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

import cats.implicits._
import connectors.DownstreamErrorOr
import connectors.httpParsers.RefreshIncomeSourceHttpParser.{handleAPIError, logMessage}
import models.{APIErrorBodyModel, APIErrorModel, APIErrorsBodyModel}
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.{JsPath, JsonValidationError}
import uk.gov.hmrc.http.{HttpResponse, _}
import utils.PagerDutyHelper.PagerDutyKeys.{
  BAD_SUCCESS_JSON_FROM_API,
  FOURXX_RESPONSE_FROM_API,
  INTERNAL_SERVER_ERROR_FROM_API,
  SERVICE_UNAVAILABLE_FROM_API,
  UNEXPECTED_RESPONSE_FROM_API
}
import utils.PagerDutyHelper.pagerDutyLog

import scala.util.Try

trait APIParserTrait extends Logging {

  val parserName: String
  val service: String

  def logMessage(method: String, url: String, response: HttpResponse): String =
    s"[$parserName][read] Received ${response.status} from $service API: $method $url. Body:${response.body}"

  def badSuccessJsonFromAPI[Response]: Either[APIErrorModel, Response] = {
    pagerDutyLog(BAD_SUCCESS_JSON_FROM_API, s"[$parserName][read] Invalid Json from $service API.")
    Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel.parsingError))
  }

  def handleAPIError[Response](response: HttpResponse, statusOverride: Option[Int] = None): Either[APIErrorModel, Response] = {

    val status = statusOverride.getOrElse(response.status)

    try {
      val maybeResponseJson = Try(response.json).toOption

      val errorResult = maybeResponseJson.flatMap { json =>
        json
          .asOpt[APIErrorBodyModel]
          .map { apiError =>
            APIErrorModel(status, apiError)
          }
          .orElse {
            json
              .asOpt[APIErrorsBodyModel]
              .map { apiErrors =>
                APIErrorModel(status, apiErrors)

              }
          }
      }

      errorResult.map(Left(_)).getOrElse {
        pagerDutyLog(UNEXPECTED_RESPONSE_FROM_API, s"[$parserName][read] Unexpected Json from $service API.")
        Left(APIErrorModel(status, APIErrorBodyModel.parsingError))
      }
    } catch {
      case _: Exception => Left(APIErrorModel(status, APIErrorBodyModel.parsingError))
    }
  }
}

object ApiParser {
  final case class CommonHttpReads(service: String) extends HttpReads[DownstreamErrorOr[Unit]] {
    override def read(method: String, url: String, response: HttpResponse): DownstreamErrorOr[Unit] =
      CommonDownstreamParser(method, url, response, service).logPagerDutyAlertOnError
  }

  case class CommonDownstreamParser(method: String, url: String, response: HttpResponse, service: String = "") extends APIParserTrait {
    val parserName: String    = "CommonDownstreamParser"
    val targetUrl: String     = url
    val requestMethod: String = method
    val responseStatus: Int   = response.status
    val responseBody: String  = response.body

    private def formatJsonErrors(errors: List[(JsPath, scala.collection.Seq[JsonValidationError])]): String = {
      val errorMessages = errors.flatMap { case (path, validationErrors) =>
        validationErrors.map { error =>
          val pathString   = path.toJsonString
          val errorMessage = error.message
          val args         = error.args.mkString(", ")
          s"Error at path $pathString: $errorMessage [args: $args]"
        }
      }

      errorMessages.mkString("\n")
    }

    def reportInvalidJsonError(errors: List[(JsPath, scala.collection.Seq[JsonValidationError])]) = {
      pagerDutyLog(
        BAD_SUCCESS_JSON_FROM_API,
        s"[$parserName][read] Invalid Json when calling $requestMethod $targetUrl: ${formatJsonErrors(errors)}, " +
          s"responseStatus: $responseStatus, responseBody: $responseBody"
      )

      APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel.parsingError)
    }

    def logPagerDutyAlertOnError: DownstreamErrorOr[Unit] =
      unsafeLogPagerDutyAlertOnError(method, url, response)
  }

  def unsafeLogPagerDutyAlertOnError(method: String, url: String, response: HttpResponse): DownstreamErrorOr[Unit] = {
    def parser = CommonDownstreamParser(method, url, response)

    response.status match {
      case OK | CREATED | ACCEPTED | NO_CONTENT => ().asRight

      case BAD_REQUEST =>
        pagerDutyLog(FOURXX_RESPONSE_FROM_API, parser.logMessage(method, url, response))
        parser.handleAPIError(response)

      case INTERNAL_SERVER_ERROR =>
        pagerDutyLog(INTERNAL_SERVER_ERROR_FROM_API, parser.logMessage(method, url, response))
        parser.handleAPIError(response)

      case SERVICE_UNAVAILABLE =>
        pagerDutyLog(SERVICE_UNAVAILABLE_FROM_API, parser.logMessage(method, url, response))
        parser.handleAPIError(response)

      case _ =>
        pagerDutyLog(UNEXPECTED_RESPONSE_FROM_API, parser.logMessage(method, url, response))
        parser.handleAPIError(response, Some(INTERNAL_SERVER_ERROR))
    }
  }

}
