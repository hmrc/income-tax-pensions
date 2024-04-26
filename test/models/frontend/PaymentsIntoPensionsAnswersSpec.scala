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
      assert(result === PensionReliefs(
        regularPensionContributions = Some(1.0),
        oneOffPensionContributionsPaid = Some(2.0),
        retirementAnnuityPayments = Some(3.0),
        paymentToEmployersSchemeNoTaxRelief = Some(4.0),
        overseasPensionSchemeContributions = Some(5.0)
      ))
    }
  }
}
