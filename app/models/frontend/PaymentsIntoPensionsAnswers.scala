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

import models.{PensionReliefs, Zero}
import play.api.libs.json.{Json, OFormat}
import cats.implicits._

final case class PaymentsIntoPensionsAnswers(rasPensionPaymentQuestion: Option[Boolean] = None,
                                             totalRASPaymentsAndTaxRelief: Option[BigDecimal] = None,
                                             oneOffRasPaymentPlusTaxReliefQuestion: Option[Boolean] = None,
                                             totalOneOffRasPaymentPlusTaxRelief: Option[BigDecimal] = None,
                                             pensionTaxReliefNotClaimedQuestion: Option[Boolean] = None,
                                             retirementAnnuityContractPaymentsQuestion: Option[Boolean] = None,
                                             totalRetirementAnnuityContractPayments: Option[BigDecimal] = None,
                                             workplacePensionPaymentsQuestion: Option[Boolean] = None,
                                             totalWorkplacePensionPayments: Option[BigDecimal] = None) {
  def toPensionReliefs(overseasPensionSchemeContributions: Option[BigDecimal]): PensionReliefs = PensionReliefs(
    regularPensionContributions = totalRASPaymentsAndTaxRelief.getOrElse(Zero).some,
    oneOffPensionContributionsPaid = totalOneOffRasPaymentPlusTaxRelief.getOrElse(Zero).some,
    retirementAnnuityPayments = totalRetirementAnnuityContractPayments.getOrElse(Zero).some,
    paymentToEmployersSchemeNoTaxRelief = totalWorkplacePensionPayments.getOrElse(Zero).some,
    overseasPensionSchemeContributions = overseasPensionSchemeContributions // not part of this journey, but if we set None it will be wiped out
  )
}

object PaymentsIntoPensionsAnswers {
  implicit val format: OFormat[PaymentsIntoPensionsAnswers] = Json.format[PaymentsIntoPensionsAnswers]
}
