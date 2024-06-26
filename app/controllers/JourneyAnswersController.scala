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
import models.common._
import models.frontend.statepension.IncomeFromPensionsStatePensionAnswers
import models.frontend._
import models.frontend.ukpensionincome.UkPensionIncomeAnswers
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.PensionsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.Logging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class JourneyAnswersController @Inject() (pensionsService: PensionsService, auth: AuthorisedAction, cc: ControllerComponents)(implicit
    ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getPaymentsIntoPensions(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(pensionsService.getPaymentsIntoPensions(JourneyContextWithNino(taxYear, user.getMtditid, nino)))
  }

  def savePaymentsIntoPensions(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[PaymentsIntoPensionsAnswers](taxYear, nino) { (ctx, value) =>
      pensionsService.upsertPaymentsIntoPensions(ctx, value).map(_ => NoContent)
    }
  }

  def getUkPensionIncome(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(pensionsService.getUkPensionIncome(JourneyContextWithNino(taxYear, user.getMtditid, nino)))
  }

  def saveUkPensionIncome(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[UkPensionIncomeAnswers](taxYear, nino) { (ctx, value) =>
      pensionsService.upsertUkPensionIncome(ctx, value).map(_ => NoContent)
    }
  }

  def getStatePension(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(pensionsService.getStatePension(JourneyContextWithNino(taxYear, user.getMtditid, nino)))
  }

  def saveStatePension(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[IncomeFromPensionsStatePensionAnswers](taxYear, nino) { (ctx, value) =>
      pensionsService.upsertStatePension(ctx, value).map(_ => NoContent)
    }
  }

  def getAnnualAllowances(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(pensionsService.getAnnualAllowances(JourneyContextWithNino(taxYear, user.getMtditid, nino)))
  }

  def saveAnnualAllowances(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[AnnualAllowancesAnswers](taxYear, nino) { (ctx, value) =>
      pensionsService.upsertAnnualAllowances(ctx, value).map(_ => NoContent)
    }
  }

  def getUnauthorisedPaymentsFromPensions(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(pensionsService.getUnauthorisedPaymentsFromPensions(JourneyContextWithNino(taxYear, user.getMtditid, nino)))
  }

  def saveUnauthorisedPaymentsFromPensions(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[UnauthorisedPaymentsAnswers](taxYear, nino) { (ctx, value) =>
      pensionsService.upsertUnauthorisedPaymentsFromPensions(ctx, value).map(_ => NoContent)
    }
  }

  def getTransfersIntoOverseasPensions(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(pensionsService.getTransfersIntoOverseasPensions(JourneyContextWithNino(taxYear, user.getMtditid, nino)))
  }

  def saveTransfersIntoOverseasPensions(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[TransfersIntoOverseasPensionsAnswers](taxYear, nino) { (ctx, value) =>
      pensionsService.upsertTransfersIntoOverseasPensions(ctx, value).map(_ => NoContent)
    }
  }

  def getIncomeFromOverseasPensions(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(pensionsService.getIncomeFromOverseasPensions(JourneyContextWithNino(taxYear, user.getMtditid, nino)))
  }

  def saveIncomeFromOverseasPensions(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[IncomeFromOverseasPensionsAnswers](taxYear, nino) { (ctx, value) =>
      pensionsService.upsertIncomeFromOverseasPensions(ctx, value).map(_ => NoContent)
    }
  }

  def getPaymentsIntoOverseasPensions(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(pensionsService.getPaymentsIntoOverseasPensions(JourneyContextWithNino(taxYear, user.getMtditid, nino)))
  }

  def savePaymentsIntoOverseasPensions(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[PaymentsIntoOverseasPensionsAnswers](taxYear, nino) { (ctx, value) =>
      pensionsService.upsertPaymentsIntoOverseasPensions(ctx, value).map(_ => NoContent)
    }
  }

  def getShortServiceRefunds(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    handleOptionalApiResult(pensionsService.getShortServiceRefunds(JourneyContextWithNino(taxYear, user.getMtditid, nino)))
  }

  def saveShortServiceRefunds(taxYear: TaxYear, nino: Nino): Action[AnyContent] = auth.async { implicit user =>
    getBodyWithCtx[ShortServiceRefundsAnswers](taxYear, nino) { (ctx, value) =>
      pensionsService.upsertShortServiceRefunds(ctx, value).map(_ => NoContent)
    }
  }

}
