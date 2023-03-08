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

import connectors.httpParsers.CreateOrAmendPensionIncomeHttpParser.CreateOrAmendPensionIncomeResponse
import controllers.predicates.AuthorisedAction
import models.{CreateUpdatePensionIncomeModel, DesErrorBodyModel}
import play.api.Logging
import play.api.libs.json.{JsSuccess, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.PensionIncomeService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PensionIncomeController @Inject()(
                                         service: PensionIncomeService,
                                         auth: AuthorisedAction,
                                         cc: ControllerComponents
                                       )(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def getPensionIncome(nino: String, taxYear: Int): Action[AnyContent] = auth.async { implicit user =>
    service.getPensionIncome(nino, taxYear).map {
      case Right(None) => NotFound(Json.toJson(DesErrorBodyModel.noDataFound))
      case Right(model) => Ok(Json.toJson(model))
      case Left(errorModel) => Status(errorModel.status)(errorModel.toJson)
    }
  }

  def createOrAmendPensionIncome(nino: String, taxYear: Int): Action[AnyContent] = auth.async { implicit user =>
    user.body.asJson.map(_.validate[CreateUpdatePensionIncomeModel]) match {
      case Some(JsSuccess(model, _)) => responseHandler(service.createOrAmendPensionIncome(nino, taxYear, model))
      case _ => Future.successful(BadRequest)
    }
  }

  def responseHandler(serviceResponse: Future[CreateOrAmendPensionIncomeResponse]): Future[Result] = {
    serviceResponse.map {
      case Right(_) => NoContent
      case Left(errorModel) => Status(errorModel.status)(Json.toJson(errorModel.toJson))
    }
  }

  def deletePensionIncome(nino: String, taxYear: Int): Action[AnyContent] = auth.async { implicit user =>
    service.deletePensionIncome(nino, taxYear).map {
      case Right(_) => NoContent
      case Left(errorModel) => Status(errorModel.status)(errorModel.toJson)
    }
  }


  def savePensionIncomeSessionData(nino: String, taxYear: Int): Action[AnyContent] = auth.async { implicit user =>
    user.body.asJson.map(_.validate[CreateUpdatePensionIncomeModel]) match {
      case Some(JsSuccess(model, _)) => service.savePensionIncomeSessionData(nino, taxYear, user.mtditid, model).map {
        case Left(_) => InternalServerError
        case Right(_) => NoContent
      }
      case _ => Future.successful(BadRequest)
    }
  }
}
