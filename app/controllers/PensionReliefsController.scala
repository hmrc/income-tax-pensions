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

import connectors.httpParsers.CreateOrAmendPensionReliefsHttpParser.CreateOrAmendPensionReliefsResponse
import controllers.predicates.AuthorisedAction
import models.{CreateOrUpdatePensionReliefsModel, DesErrorBodyModel, ServiceErrorModel}
import play.api.Logging
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.PensionReliefsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PensionReliefsController @Inject() (pensionReliefsService: PensionReliefsService, auth: AuthorisedAction, cc: ControllerComponents)(implicit
    ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getPensionReliefs(nino: String, taxYear: Int): Action[AnyContent] = auth.async { implicit user =>
    pensionReliefsService.getPensionReliefs(nino, taxYear).map {
      case Right(None)      => NotFound(Json.toJson(DesErrorBodyModel.noDataFound))
      case Right(model)     => Ok(Json.toJson(model))
      case Left(errorModel) => Status(errorModel.status)(errorModel.toJson)
    }
  }

  def createOrAmendPensionReliefs(nino: String, taxYear: Int): Action[AnyContent] = auth.async { implicit user =>
    user.body.asJson.map(_.validate[CreateOrUpdatePensionReliefsModel]) match {
      case Some(JsSuccess(model, _)) =>
        saveUserDataHandler(pensionReliefsService.saveUserPensionReliefsData(nino, user.mtditid, taxYear, model))
      case _ => Future.successful(BadRequest)
    }
  }

  def savePensionReliefsUserData(nino: String, taxYear: Int): Action[AnyContent] = auth.async { implicit user =>
    user.body.asJson.map(_.validate[CreateOrUpdatePensionReliefsModel]) match {
      case Some(data: JsSuccess[CreateOrUpdatePensionReliefsModel]) =>
        saveUserDataHandler(pensionReliefsService.saveUserPensionReliefsData(nino, user.mtditid, taxYear, data.value))
      case _ =>
        val logMessage = "[PensionReliefsController][savePensionReliefsUserData] Save pension relief request is invalid"
        logger.warn(logMessage)
        Future.successful(BadRequest)
    }
  }

  def deletePensionReliefsUserData(nino: String, taxYear: Int): Action[AnyContent] = auth.async { implicit user =>
    pensionReliefsService.deleteUserPensionReliefsData(nino, user.mtditid, taxYear).map {
      case Right(_)         => NoContent
      case Left(errorModel) => Status(errorModel.status)(Json.toJson(errorModel.toJson))
    }
  }

  private def saveUserDataHandler(saveResponsee: Future[Either[ServiceErrorModel, Unit]]): Future[Result] =
    saveResponsee.map {
      case Right(_)         => NoContent
      case Left(errorModel) => Status(errorModel.status)(Json.toJson(errorModel.toJson))
    }

  def responseHandler(serviceResponse: Future[CreateOrAmendPensionReliefsResponse]): Future[Result] =
    serviceResponse.map {
      case Right(_)         => NoContent
      case Left(errorModel) => Status(errorModel.status)(Json.toJson(errorModel.toJson))
    }

  def deletePensionReliefs(nino: String, taxYear: Int): Action[AnyContent] = auth.async { implicit user =>
    pensionReliefsService.deleteUserPensionReliefsData(nino, user.mtditid, taxYear).map {
      case Right(_)         => NoContent
      case Left(errorModel) => Status(errorModel.status)(errorModel.toJson)
    }
  }

}
