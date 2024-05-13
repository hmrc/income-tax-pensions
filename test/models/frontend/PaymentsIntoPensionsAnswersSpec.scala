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

import models.PensionReliefs
import org.scalatest.wordspec.AnyWordSpecLike

class PaymentsIntoPensionsAnswersSpec extends AnyWordSpecLike {

  "toPensionReliefs" should {
    "create Pension Relief" in {
      val answers = PaymentsIntoPensionsAnswers(
        rasPensionPaymentQuestion = true,
        totalRASPaymentsAndTaxRelief = Some(1.0),
        oneOffRasPaymentPlusTaxReliefQuestion = Some(true),
        totalOneOffRasPaymentPlusTaxRelief = Some(2.0),
        pensionTaxReliefNotClaimedQuestion = true,
        retirementAnnuityContractPaymentsQuestion = Some(true),
        totalRetirementAnnuityContractPayments = Some(3.0),
        workplacePensionPaymentsQuestion = Some(true),
        totalWorkplacePensionPayments = Some(4.0)
      )
      val result = answers.toPensionReliefs(Some(5.0))
      assert(
        result === PensionReliefs(
          regularPensionContributions = Some(1.0),
          oneOffPensionContributionsPaid = Some(2.0),
          retirementAnnuityPayments = Some(3.0),
          paymentToEmployersSchemeNoTaxRelief = Some(4.0),
          overseasPensionSchemeContributions = Some(5.0)
        ))
    }

    "convert an empty Pension Relief" in {
      val answers = PaymentsIntoPensionsAnswers(
        rasPensionPaymentQuestion = false,
        totalRASPaymentsAndTaxRelief = None,
        oneOffRasPaymentPlusTaxReliefQuestion = None,
        totalOneOffRasPaymentPlusTaxRelief = None,
        pensionTaxReliefNotClaimedQuestion = false,
        retirementAnnuityContractPaymentsQuestion = None,
        totalRetirementAnnuityContractPayments = None,
        workplacePensionPaymentsQuestion = None,
        totalWorkplacePensionPayments = None
      )
      val result = answers.toPensionReliefs(None)
      assert(
        result === PensionReliefs(
          regularPensionContributions = None,
          oneOffPensionContributionsPaid = None,
          retirementAnnuityPayments = None,
          paymentToEmployersSchemeNoTaxRelief = None,
          overseasPensionSchemeContributions = None
        ))
    }
  }
}
