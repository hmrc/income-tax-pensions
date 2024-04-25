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
import models.common._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.JourneyStatusService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class JourneyStatusController @Inject() (journeyStatusService: JourneyStatusService, auth: AuthorisedAction, cc: ControllerComponents)(implicit
    ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getAllStatuses(nino: String, taxYear: Int): Action[AnyContent] = auth.async { implicit request =>
    val result = journeyStatusService.getAllStatuses(TaxYear(taxYear), Mtditid(request.mtditid), Nino(nino))
    handleApiResultT(result)
  }
}
