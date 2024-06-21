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

import models.database.PaymentsIntoPensionsStorageAnswers
import models.domain.PensionAnswers
import models.{GetPensionReliefsModel, PensionReliefs}
import play.api.libs.json.{Json, OFormat}

final case class PaymentsIntoPensionsAnswers(rasPensionPaymentQuestion: Boolean,
                                             totalRASPaymentsAndTaxRelief: Option[BigDecimal],
                                             oneOffRasPaymentPlusTaxReliefQuestion: Option[Boolean],
                                             totalOneOffRasPaymentPlusTaxRelief: Option[BigDecimal],
                                             pensionTaxReliefNotClaimedQuestion: Boolean,
                                             retirementAnnuityContractPaymentsQuestion: Option[Boolean],
                                             totalRetirementAnnuityContractPayments: Option[BigDecimal],
                                             workplacePensionPaymentsQuestion: Option[Boolean],
                                             totalWorkplacePensionPayments: Option[BigDecimal])
    extends PensionAnswers {

  def isFinished: Boolean = {
    val hasRasPensionPaymentAnswer = hasAnswer(rasPensionPaymentQuestion, totalRASPaymentsAndTaxRelief)
    val hasOneOffRASPaymentsAnswer = hasAnswer(rasPensionPaymentQuestion, oneOffRasPaymentPlusTaxReliefQuestion, totalOneOffRasPaymentPlusTaxRelief)
    val hasRetirementAnnuityContractPaymentsAnswer =
      hasAnswer(pensionTaxReliefNotClaimedQuestion, retirementAnnuityContractPaymentsQuestion, totalRetirementAnnuityContractPayments)
    val hasWorkplacePensionPaymentsAnswer =
      hasAnswer(pensionTaxReliefNotClaimedQuestion, workplacePensionPaymentsQuestion, totalWorkplacePensionPayments)

    List(
      hasRasPensionPaymentAnswer,
      hasOneOffRASPaymentsAnswer,
      hasRetirementAnnuityContractPaymentsAnswer,
      hasWorkplacePensionPaymentsAnswer
    ).forall(identity)
  }

  def toPensionReliefs(overseasPensionSchemeContributions: Option[BigDecimal]): PensionReliefs = PensionReliefs(
    regularPensionContributions = totalRASPaymentsAndTaxRelief,
    oneOffPensionContributionsPaid = totalOneOffRasPaymentPlusTaxRelief,
    retirementAnnuityPayments = totalRetirementAnnuityContractPayments,
    paymentToEmployersSchemeNoTaxRelief = totalWorkplacePensionPayments,
    overseasPensionSchemeContributions = overseasPensionSchemeContributions // not part of this journey, but if we set None it will be wiped out
  )
}

object PaymentsIntoPensionsAnswers {
  implicit val format: OFormat[PaymentsIntoPensionsAnswers] = Json.format[PaymentsIntoPensionsAnswers]

  def mkAnswers(maybeDownstreamAnswers: Option[GetPensionReliefsModel],
                maybeDbAnswers: Option[PaymentsIntoPensionsStorageAnswers]): Option[PaymentsIntoPensionsAnswers] =
    maybeDownstreamAnswers
      .getOrElse(GetPensionReliefsModel("", None, PensionReliefs.empty))
      .toPaymentsIntoPensions(maybeDbAnswers)

}
