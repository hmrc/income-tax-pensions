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

import models.domain.ApiResultT
import models.error.ServiceError
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}

package object controllers {

  def handleOptionalApiResult[A: Writes](result: ApiResultT[Option[A]])(implicit ec: ExecutionContext, logger: Logger): Future[Result] = {
    val resultT = result.map(r => r.fold(NoContent)(_ => Ok(Json.toJson(r))))
    handleResultT(resultT)
  }

  def handleApiResultT[A: Writes](result: ApiResultT[A])(implicit ec: ExecutionContext, logger: Logger): Future[Result] = {
    val resultT = result.map(r => Ok(Json.toJson(r)))
    handleResultT(resultT)
  }

  def handleApiUnitResultT(result: ApiResultT[Unit])(implicit ec: ExecutionContext, logger: Logger): Future[Result] = {
    val resultT = result.map(_ => NoContent)
    handleResultT(resultT)
  }

  def handleResultT(result: ApiResultT[Result])(implicit ec: ExecutionContext, logger: Logger): Future[Result] =
    result.leftMap { error =>
      handleError(error)
    }.merge

  private def handleError(error: ServiceError)(implicit logger: Logger) = {
    logger.error(s"HttpError encountered: ${error.errorMessage}")
    InternalServerError(error.errorMessage)
  }
}
