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

package controllers

import connectors.httpParsers.CreateUpdatePensionChargesHttpParser.CreateUpdatePensionChargesResponse
import controllers.predicates.AuthorisedAction
import models.{CreateUpdatePensionChargesRequestModel, DesErrorBodyModel}
import play.api.Logging
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.PensionChargesService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PensionChargesController @Inject()(service: PensionChargesService,
                                         authorisedAction: AuthorisedAction,
                                         cc: ControllerComponents
                                        )(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def getPensionCharges(nino: String, taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit user =>

    service.getPensionCharges(nino, taxYear).map {
      case Right(None) => NotFound(Json.toJson(DesErrorBodyModel.noDataFound))
      case Right(model) => Ok(Json.toJson(model))
      case Left(errorModel) => Status(errorModel.status)(errorModel.toJson)
    }

  }

  def deletePensionCharges(nino: String, taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit user =>

    service.deletePensionCharges(nino, taxYear).map {
      case Right(_) => NoContent
      case Left(errorModel) => Status(errorModel.status)(errorModel.toJson)
    }
  }

  def createUpdatePensionCharges(nino: String, taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit user =>
    user.body.asJson.map(_.validate[CreateUpdatePensionChargesRequestModel]) match {
      case Some(JsSuccess(model, _)) => responseHandler(service.createUpdatePensionCharges(nino, taxYear, model))
      case _ => Future.successful(BadRequest)
    }
  }

  def responseHandler(serviceResponse: Future[CreateUpdatePensionChargesResponse]): Future[Result] = {
    serviceResponse.map {
      case Right(_) => NoContent
      case Left(errorModel) => Status(errorModel.status)(errorModel.toJson)
    }
  }
}
