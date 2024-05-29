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

package models.charges

import models.database.UnauthorisedPaymentsStorageAnswers
import models.frontend.UnauthorisedPaymentsAnswers
import play.api.libs.json.{Json, OFormat}

case class PensionSchemeUnauthorisedPayments(pensionSchemeTaxReference: Option[List[String]],
                                             surcharge: Option[Charge],
                                             noSurcharge: Option[Charge]) {
  // TODO Determine what we should send on selecting No - if None, the below logic should only consider db answers for Questions
  def toUnauthorisedPayments(maybeDbAnswers: Option[UnauthorisedPaymentsStorageAnswers]): UnauthorisedPaymentsAnswers =
    UnauthorisedPaymentsAnswers(
      surchargeQuestion = surcharge.map(_.amount != 0).orElse(maybeDbAnswers.flatMap(_.surchargeQuestion)),
      noSurchargeQuestion = noSurcharge.map(_.amount != 0).orElse(maybeDbAnswers.flatMap(_.noSurchargeQuestion)),
      surchargeAmount = surcharge.map(_.amount),
      surchargeTaxAmountQuestion = surcharge.map(_.foreignTaxPaid != 0).orElse(maybeDbAnswers.flatMap(_.surchargeTaxAmountQuestion)),
      surchargeTaxAmount = surcharge.map(_.foreignTaxPaid),
      noSurchargeAmount = noSurcharge.map(_.amount),
      noSurchargeTaxAmountQuestion = noSurcharge.map(_.foreignTaxPaid != 0).orElse(maybeDbAnswers.flatMap(_.noSurchargeTaxAmountQuestion)),
      noSurchargeTaxAmount = noSurcharge.map(_.foreignTaxPaid),
      ukPensionSchemesQuestion = pensionSchemeTaxReference.map(_.nonEmpty).orElse(maybeDbAnswers.flatMap(_.ukPensionSchemesQuestion)),
      pensionSchemeTaxReference = pensionSchemeTaxReference
    )

  def nonEmpty: Boolean = pensionSchemeTaxReference.exists(_.nonEmpty) || surcharge.isDefined || noSurcharge.isDefined
}

case class Charge(amount: BigDecimal, foreignTaxPaid: BigDecimal)

object Charge {
  implicit val format: OFormat[Charge] = Json.format[Charge]
}

object PensionSchemeUnauthorisedPayments {
  implicit val format: OFormat[PensionSchemeUnauthorisedPayments] = Json.format[PensionSchemeUnauthorisedPayments]

  def empty: PensionSchemeUnauthorisedPayments = PensionSchemeUnauthorisedPayments(None, None, None)
}
