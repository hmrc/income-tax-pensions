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
import models.common.{JourneyContextWithNino, Nino, TaxYear}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.PensionsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Logging

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CommonTaskListController @Inject() (service: PensionsService, auth: AuthorisedAction, cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getCommonTaskList(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleApiResultT(service.getCommonTaskList(JourneyContextWithNino(taxYear, user.getMtditid, nino)))
  }
}
