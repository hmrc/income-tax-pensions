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

package controllers

import controllers.predicates.AuthorisedAction
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.LoadSubmittedDataService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class LoadSubmittedDataController @Inject() (service: LoadSubmittedDataService, auth: AuthorisedAction, cc: ControllerComponents)(implicit
    ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  // TODO: Implement proper error handling.
  def loadEmploymentPension(nino: String, taxYear: Int): Action[AnyContent] = auth.async { implicit req =>
    service.loadEmployment(nino, taxYear, req.mtditid).map {
      case Right(employmentPension) if employmentPension.isEmpty => NoContent
      case Right(employmentPension)                              => Ok(Json.toJson(employmentPension))
      case Left(error)                                           => Status(error.status)(error.toJson)
    }
  }
}
