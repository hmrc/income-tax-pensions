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
import models.{Charge, PensionSchemeUnauthorisedPayments}
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
) {
  def toPensionCharges: PensionSchemeUnauthorisedPayments =
    PensionSchemeUnauthorisedPayments(
      pensionSchemeTaxReference = pensionSchemeTaxReference,
      surcharge = determineCharge(surchargeQuestion, surchargeAmount, surchargeTaxAmount).some,
      noSurcharge = determineCharge(noSurchargeQuestion, noSurchargeAmount, noSurchargeTaxAmount).some
    )

  // TODO Decide if we need to send 0 - it was copied from Frontend. Maybe we should send None?
  private def determineCharge(maybeBaseQ: Option[Boolean], maybeAmount: Option[BigDecimal], maybeTaxAmount: Option[BigDecimal]): Charge = {
    val blankSubmission = Charge(0.00, 0.00)

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
}
