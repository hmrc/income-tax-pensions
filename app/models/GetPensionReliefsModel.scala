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

import models.database.PaymentsIntoPensionsStorageAnswers
import models.frontend.PaymentsIntoPensionsAnswers
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
}

object PensionReliefs {
  implicit val format: OFormat[PensionReliefs] = Json.format[PensionReliefs]

  def empty: PensionReliefs = PensionReliefs(None, None, None, None, None)
}

case class GetPensionReliefsModel(submittedOn: String, deletedOn: Option[String], pensionReliefs: PensionReliefs) {
  // TODO When we finish introducing DB, come back to each journey and make sure we favour IFS answers over our DB state
  def toPaymentsIntoPensions(maybeDbAnswers: Option[PaymentsIntoPensionsStorageAnswers]): Option[PaymentsIntoPensionsAnswers] =
    maybeDbAnswers.map { dbAnswers =>
      PaymentsIntoPensionsAnswers(
        rasPensionPaymentQuestion = dbAnswers.rasPensionPaymentQuestion,
        totalRASPaymentsAndTaxRelief = pensionReliefs.regularPensionContributions,
        oneOffRasPaymentPlusTaxReliefQuestion = dbAnswers.oneOffRasPaymentPlusTaxReliefQuestion,
        totalOneOffRasPaymentPlusTaxRelief = pensionReliefs.oneOffPensionContributionsPaid,
        pensionTaxReliefNotClaimedQuestion = dbAnswers.pensionTaxReliefNotClaimedQuestion,
        retirementAnnuityContractPaymentsQuestion = dbAnswers.retirementAnnuityContractPaymentsQuestion,
        totalRetirementAnnuityContractPayments = pensionReliefs.retirementAnnuityPayments,
        workplacePensionPaymentsQuestion = dbAnswers.workplacePensionPaymentsQuestion,
        totalWorkplacePensionPayments = pensionReliefs.paymentToEmployersSchemeNoTaxRelief
      )

    }
}

object GetPensionReliefsModel {
  implicit val format: OFormat[GetPensionReliefsModel] = Json.format[GetPensionReliefsModel]
}
