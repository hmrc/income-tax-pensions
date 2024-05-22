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

package models

import cats.implicits.catsSyntaxOptionId
import models.database.{PaymentsIntoOverseasPensionsStorageAnswers, PaymentsIntoPensionsStorageAnswers}
import models.frontend.{PaymentsIntoOverseasPensionsAnswers, PaymentsIntoPensionsAnswers}
import play.api.libs.json.{Json, OFormat}

case class PensionReliefs(regularPensionContributions: Option[BigDecimal],
                          oneOffPensionContributionsPaid: Option[BigDecimal],
                          retirementAnnuityPayments: Option[BigDecimal],
                          paymentToEmployersSchemeNoTaxRelief: Option[BigDecimal],
                          overseasPensionSchemeContributions: Option[BigDecimal]) {
  def nonEmpty: Boolean = regularPensionContributions.isDefined ||
    oneOffPensionContributionsPaid.isDefined ||
    retirementAnnuityPayments.isDefined ||
    paymentToEmployersSchemeNoTaxRelief.isDefined ||
    overseasPensionSchemeContributions.isDefined

  def hasNoPaymentIntoPensionAnswers: Boolean =
    List(regularPensionContributions, oneOffPensionContributionsPaid, retirementAnnuityPayments, paymentToEmployersSchemeNoTaxRelief).forall(
      _.isEmpty)
}

object PensionReliefs {
  implicit val format: OFormat[PensionReliefs] = Json.format[PensionReliefs]

  def empty: PensionReliefs = PensionReliefs(None, None, None, None, None)
}

case class GetPensionReliefsModel(submittedOn: String, deletedOn: Option[String], pensionReliefs: PensionReliefs) {
  // TODO When we finish introducing DB, come back to each journey and make sure we favour IFS answers over our DB state
  def toPaymentsIntoPensions(maybeDbAnswers: Option[PaymentsIntoPensionsStorageAnswers]): Option[PaymentsIntoPensionsAnswers] =
    if (pensionReliefs.hasNoPaymentIntoPensionAnswers && maybeDbAnswers.isEmpty) None
    else {
      // if no IFS answer, but we have something in DB it would mean that user selected No / No answers
      Some(
        PaymentsIntoPensionsAnswers(
          rasPensionPaymentQuestion = pensionReliefs.regularPensionContributions.isDefined,
          totalRASPaymentsAndTaxRelief = pensionReliefs.regularPensionContributions,
          oneOffRasPaymentPlusTaxReliefQuestion = maybeDbAnswers.flatMap(_.oneOffRasPaymentPlusTaxReliefQuestion),
          totalOneOffRasPaymentPlusTaxRelief = pensionReliefs.oneOffPensionContributionsPaid,
          pensionTaxReliefNotClaimedQuestion =
            maybeDbAnswers.exists(_.pensionTaxReliefNotClaimedQuestion), // TODO we should probably calculate this from IFS answers
          retirementAnnuityContractPaymentsQuestion = maybeDbAnswers.flatMap(_.retirementAnnuityContractPaymentsQuestion),
          totalRetirementAnnuityContractPayments = pensionReliefs.retirementAnnuityPayments,
          workplacePensionPaymentsQuestion = maybeDbAnswers.flatMap(_.workplacePensionPaymentsQuestion),
          totalWorkplacePensionPayments = pensionReliefs.paymentToEmployersSchemeNoTaxRelief
        )
      )
    }

  def toPaymentsIntoOverseasPensionsAnswers(
      maybeIncomes: Option[GetPensionIncomeModel],
      maybeDbAnswers: Option[PaymentsIntoOverseasPensionsStorageAnswers]): Option[PaymentsIntoOverseasPensionsAnswers] = {
    val apiHasAmount: Boolean                                           = pensionReliefs.overseasPensionSchemeContributions.nonEmpty
    val overseasPensionContributions: List[OverseasPensionContribution] = maybeSeqToList(maybeIncomes.flatMap(_.overseasPensionContribution))
    if (!apiHasAmount && maybeDbAnswers.isEmpty) None
    else
      PaymentsIntoOverseasPensionsAnswers(
        paymentsIntoOverseasPensionsQuestions = apiHasAmount.some,
        paymentsIntoOverseasPensionsAmount = pensionReliefs.overseasPensionSchemeContributions,
        employerPaymentsQuestion = if (overseasPensionContributions.nonEmpty) true.some else maybeDbAnswers.flatMap(_.employerPaymentsQuestion),
        taxPaidOnEmployerPaymentsQuestion =
          if (overseasPensionContributions.nonEmpty) false.some else maybeDbAnswers.flatMap(_.taxPaidOnEmployerPaymentsQuestion),
        schemes = overseasPensionContributions.map(_.toOverseasPensionScheme)
      ).some
  }

}

object GetPensionReliefsModel {
  implicit val format: OFormat[GetPensionReliefsModel] = Json.format[GetPensionReliefsModel]

  def empty: GetPensionReliefsModel = GetPensionReliefsModel("", None, PensionReliefs.empty)
}
