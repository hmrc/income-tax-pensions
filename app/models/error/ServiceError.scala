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

package models.error

import models.error.ServiceError._
import models.{ServiceErrorBody, ServiceErrorModel}
import play.api.libs.json._

trait ServiceError extends ServiceErrorModel {
  val errorMessage: String
  val status: Int = 500
  val body        = GenericServerError(errorMessage)

  def toJson: JsValue = Json.toJson(body)(GenericServerError.format)
}

object ServiceError {
  type JsonErrorWithPath = List[(JsPath, scala.collection.Seq[JsonValidationError])]

  final case class GenericServerError(error: String) extends ServiceErrorBody
  object GenericServerError {
    val format = Json.format[GenericServerError]
  }

  case class InvalidJsonFormatError(expectedCaseClassName: String, rawJson: String, error: JsonErrorWithPath) extends ServiceError {
    val errorMessage: String = s"Cannot convert JSON to a case class: $expectedCaseClassName. Error: ${error.toString}. JSON:\n$rawJson"
  }

  final case class CannotReadJsonError(details: JsonErrorWithPath) extends ServiceError {
    val errorMessage: String = s"Cannot read JSON: ${details.toString}"
  }

  final case class CannotParseJsonError(details: Throwable) extends ServiceError {
    val errorMessage: String = s"Cannot parse JSON: ${details.getMessage}"
  }

  final case class DownstreamError(error: String) extends ServiceError {
    val errorMessage: String = s"Downstream error: $error"
  }

}
