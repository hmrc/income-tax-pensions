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

import cats.data.NonEmptyList
import models.common.Country
import models.database.IncomeFromOverseasPensionsStorageAnswers
import models.domain.PensionAnswers
import models.{ForeignPension, GetPensionIncomeModel}
import play.api.libs.json.{Json, OFormat}

final case class IncomeFromOverseasPensionsAnswers(
    paymentsFromOverseasPensionsQuestion: Option[Boolean] = None,
    overseasIncomePensionSchemes: Seq[PensionScheme] = Nil
) extends PensionAnswers {
  def isFinished: Boolean =
    paymentsFromOverseasPensionsQuestion.exists(x =>
      if (!x) true else overseasIncomePensionSchemes.nonEmpty && overseasIncomePensionSchemes.forall(_.isFinished))

  def toForeignPension: Option[NonEmptyList[ForeignPension]] = {
    val foreignPensions = overseasIncomePensionSchemes.toList.map { scheme =>
      ForeignPension(
        countryCode = Country.get3AlphaCodeFrom2AlphaCode(scheme.alphaTwoCode.get), // TODO unsafe use of get. Fix me
        taxableAmount = scheme.taxableAmount.getOrElse(0.0),
        amountBeforeTax = scheme.pensionPaymentAmount,
        taxTakenOff = scheme.pensionPaymentTaxPaid,
        specialWithholdingTax = scheme.specialWithholdingTaxAmount,
        foreignTaxCreditRelief = scheme.foreignTaxCreditReliefQuestion
      )
    }

    NonEmptyList.fromList(foreignPensions)
  }
}

object IncomeFromOverseasPensionsAnswers {
  implicit val format: OFormat[IncomeFromOverseasPensionsAnswers] = Json.format[IncomeFromOverseasPensionsAnswers]

  def mkAnswers(maybeDownstreamAnswers: Option[GetPensionIncomeModel],
                maybeDbAnswers: Option[IncomeFromOverseasPensionsStorageAnswers]): Option[IncomeFromOverseasPensionsAnswers] =
    maybeDownstreamAnswers
      .getOrElse(GetPensionIncomeModel.empty)
      .toIncomeFromOverseasPensions
}

case class PensionScheme(alphaThreeCode: Option[String] = None,
                         alphaTwoCode: Option[String] = None,
                         pensionPaymentAmount: Option[BigDecimal] = None,
                         pensionPaymentTaxPaid: Option[BigDecimal] = None,
                         specialWithholdingTaxQuestion: Option[Boolean] = None,
                         specialWithholdingTaxAmount: Option[BigDecimal] = None,
                         foreignTaxCreditReliefQuestion: Option[Boolean] = None,
                         taxableAmount: Option[BigDecimal] = None) {
  def isFinished: Boolean =
    this.alphaTwoCode.isDefined &&
      this.pensionPaymentAmount.isDefined &&
      this.pensionPaymentTaxPaid.isDefined &&
      this.specialWithholdingTaxQuestion.exists(value => !value || (value && this.specialWithholdingTaxAmount.nonEmpty)) &&
      foreignTaxCreditReliefQuestion.isDefined &&
      taxableAmount.isDefined
}

object PensionScheme {
  implicit val format: OFormat[PensionScheme] = Json.format[PensionScheme]

}
