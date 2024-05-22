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

package models.frontend

import models.ForeignPension
import models.common.Country
import play.api.libs.json.{Json, OFormat}

final case class IncomeFromOverseasPensionsAnswers(
    paymentsFromOverseasPensionsQuestion: Option[Boolean] = None,
    overseasIncomePensionSchemes: Seq[PensionScheme] = Nil
) {
  def toForeignPension: Seq[ForeignPension] =
    overseasIncomePensionSchemes.map { pensionScheme =>
      ForeignPension(
        countryCode = Country.get3AlphaCodeFrom2AlphaCode(pensionScheme.alphaTwoCode.get),
        taxableAmount = pensionScheme.taxableAmount.getOrElse(0.0),
        amountBeforeTax = pensionScheme.pensionPaymentAmount,
        taxTakenOff = pensionScheme.pensionPaymentTaxPaid,
        specialWithholdingTax = pensionScheme.specialWithholdingTaxAmount,
        foreignTaxCreditRelief = pensionScheme.foreignTaxCreditReliefQuestion
      )
    }
}

case class PensionScheme(alphaThreeCode: Option[String] = None,
                         alphaTwoCode: Option[String] = None,
                         pensionPaymentAmount: Option[BigDecimal] = None,
                         pensionPaymentTaxPaid: Option[BigDecimal] = None,
                         specialWithholdingTaxQuestion: Option[Boolean] = None,
                         specialWithholdingTaxAmount: Option[BigDecimal] = None,
                         foreignTaxCreditReliefQuestion: Option[Boolean] = None,
                         taxableAmount: Option[BigDecimal] = None)

object IncomeFromOverseasPensionsAnswers {
  implicit val format: OFormat[IncomeFromOverseasPensionsAnswers] = Json.format[IncomeFromOverseasPensionsAnswers]
}

object PensionScheme {
  implicit val format: OFormat[PensionScheme] = Json.format[PensionScheme]
}
