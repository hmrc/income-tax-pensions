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

package controllers

import controllers.predicates.AuthorisedAction
import models.common.{Nino, TaxYear}
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.PensionsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class GetAllPensionsController @Inject() (service: PensionsService, auth: AuthorisedAction, cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getAllPensions(nino: Nino, taxYear: TaxYear): Action[AnyContent] = auth.async { implicit user =>
    val result = service.getAllPensionsData(nino, taxYear, user.getMtditid).value

    result.map {
      case Right(pensions) if pensions.isEmpty => NoContent
      case Right(model)                        => Ok(Json.toJson(model))
      case Left(errorModel)                    =>
        val errorStr = errorModel.toJson
        print(errorStr)
        Status(errorModel.status)(errorStr)
    }
  }
}
