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

package models.frontend

import cats.implicits.catsSyntaxOptionId
import models.charges.{Charge, GetPensionChargesRequestModel, PensionSchemeUnauthorisedPayments}
import models.database.UnauthorisedPaymentsStorageAnswers
import models.domain.PensionAnswers
import play.api.libs.json.{Json, OFormat}

case class UnauthorisedPaymentsAnswers(
    surchargeQuestion: Option[Boolean],
    noSurchargeQuestion: Option[Boolean],
    surchargeAmount: Option[BigDecimal],
    surchargeTaxAmountQuestion: Option[Boolean],
    surchargeTaxAmount: Option[BigDecimal],
    noSurchargeAmount: Option[BigDecimal],
    noSurchargeTaxAmountQuestion: Option[Boolean],
    noSurchargeTaxAmount: Option[BigDecimal],
    ukPensionSchemesQuestion: Option[Boolean],
    pensionSchemeTaxReference: Option[List[String]]
) extends PensionAnswers {

  // TODO Copied from frontend, requires simplification/review
  def isFinished: Boolean = {
    // `surchargeQuestion` and `noSurchargeQuestion` represent (optional) checkboxes in the UI. If they are not checked,
    //    their corresponding optional boolean values here will be `Some(false)`
    val isSurchargeComplete = surchargeQuestion.exists { bool =>
      if (bool) surchargeAmount.isDefined && isTaxQuestionComplete(surchargeTaxAmountQuestion, surchargeTaxAmount)
      else true
    }
    val isNoSurchargeComplete = noSurchargeQuestion.exists { bool =>
      if (bool) noSurchargeAmount.isDefined && isTaxQuestionComplete(noSurchargeTaxAmountQuestion, noSurchargeTaxAmount)
      else true
    }

    val hasPensionSchemesAndPSTR = ukPensionSchemesQuestion.exists { bool =>
      if (bool) pensionSchemeTaxReference.nonEmpty else true
    }

    val arePstrQuestionsComplete =
      if (surchargeQuestion.contains(true) || noSurchargeQuestion.contains(true))
        hasPensionSchemesAndPSTR
      else
        isSurchargeComplete && isNoSurchargeComplete // if both surcharges are false, we don't need to ask about UK pension schemes

    isSurchargeComplete && isNoSurchargeComplete && arePstrQuestionsComplete
  }

  private def isTaxQuestionComplete(maybeBool: Option[Boolean], amountField: Option[BigDecimal]): Boolean =
    maybeBool.exists { bool =>
      if (bool) amountField.nonEmpty
      else true
    }

  def toPensionCharges: PensionSchemeUnauthorisedPayments =
    PensionSchemeUnauthorisedPayments(
      pensionSchemeTaxReference = pensionSchemeTaxReference,
      surcharge = determineCharge(surchargeQuestion, surchargeAmount, surchargeTaxAmount).some,
      noSurcharge = determineCharge(noSurchargeQuestion, noSurchargeAmount, noSurchargeTaxAmount).some
    )

  // TODO Decide if we need to send 0 - it was copied from Frontend. Maybe we should send None?
  private def determineCharge(maybeBaseQ: Option[Boolean], maybeAmount: Option[BigDecimal], maybeTaxAmount: Option[BigDecimal]): Charge = {
    val blankSubmission = Charge(amount = 0.00, foreignTaxPaid = 0.00)

    (maybeBaseQ, maybeAmount, maybeTaxAmount) match {
      case (Some(_), Some(am), taxAm) =>
        taxAm.fold(ifEmpty = Charge(am, 0.00)) { t =>
          Charge(am, t)
        }
      case _ => blankSubmission
    }
  }
}

object UnauthorisedPaymentsAnswers {
  implicit val format: OFormat[UnauthorisedPaymentsAnswers] = Json.format[UnauthorisedPaymentsAnswers]

  def mkAnswers(maybeDownstreamAnswers: Option[GetPensionChargesRequestModel],
                maybeDbAnswers: Option[UnauthorisedPaymentsStorageAnswers]): Option[UnauthorisedPaymentsAnswers] =
    maybeDownstreamAnswers.flatMap(
      _.pensionSchemeUnauthorisedPayments.map(_.toUnauthorisedPayments(maybeDbAnswers))
    )

}
