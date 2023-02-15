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

import connectors.httpParsers.CreateUpdatePensionChargesHttpParser.CreateUpdatePensionChargesResponse
import controllers.predicates.AuthorisedAction
import models.{CreateUpdatePensionChargesRequestModel, DesErrorBodyModel}
import play.api.Logging
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.{PensionChargesService, PensionsService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PensionChargesController @Inject()(service: PensionChargesService,
                                         authorisedAction: AuthorisedAction,
                                         cc: ControllerComponents,
                                         pensionService: PensionsService
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

  def savePensionChargesData(nino: String, taxYear: Int): Action[AnyContent] = authorisedAction.async { implicit user =>


    user.body.asJson.map(_.validate[CreateUpdatePensionChargesRequestModel]) match {
      case Some(data: JsSuccess[CreateUpdatePensionChargesRequestModel]) => handleSaveUserData(nino,user.mtditid, taxYear, data.value)
      case _ =>
        val logMessage = "[PensionChargesController][savePensionChargesData] Save pension charges request is invalid"
        logger.warn(logMessage)
        Future.successful(BadRequest)
    }
  }

  private def handleSaveUserData(nino: String, mtditid: String, taxYear: Int, userData: CreateUpdatePensionChargesRequestModel)
                                (implicit hc: HeaderCarrier): Future[Result] = {
    pensionService.saveUserPensionChargesData(nino, mtditid, taxYear, userData).map {
      case Left(_) => InternalServerError
      case Right(_) => NoContent
    }
  }

  def responseHandler(serviceResponse: Future[CreateUpdatePensionChargesResponse]): Future[Result] = {
    serviceResponse.map {
      case Right(_) => NoContent
      case Left(errorModel) => Status(errorModel.status)(errorModel.toJson)
    }
  }
}
