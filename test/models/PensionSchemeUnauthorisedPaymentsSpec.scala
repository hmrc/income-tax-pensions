package models

import models.frontend.UnauthorisedPaymentsAnswers
import org.scalatest.wordspec.AnyWordSpecLike
import testdata.connector.charges.{sampleCharges, sampleChargesWithNones}
import testdata.database.unauthorisedPaymentsStorageAnswers.sampleStorageAnswers

class PensionSchemeUnauthorisedPaymentsSpec extends AnyWordSpecLike {
  "toUnauthorisedPayments" should {
    "create UnauthorisedPaymentsAnswers" in {
      val result = sampleCharges.toUnauthorisedPayments(Some(sampleStorageAnswers))
      assert(
        result === UnauthorisedPaymentsAnswers(
          surchargeQuestion = Some(true),
          noSurchargeQuestion = Some(true),
          surchargeAmount = Some(1.0),
          surchargeTaxAmountQuestion = Some(true),
          surchargeTaxAmount = Some(2.0),
          noSurchargeAmount = Some(3.0),
          noSurchargeTaxAmountQuestion = Some(true),
          noSurchargeTaxAmount = Some(4.0),
          ukPensionSchemesQuestion = Some(true),
          pensionSchemeTaxReference = Some(List("00123456RA"))
        ))
    }

    "take db answers if downstream answers not defined" in {
      val result = sampleChargesWithNones.toUnauthorisedPayments(Some(sampleStorageAnswers))
      assert(
        result === UnauthorisedPaymentsAnswers(
          surchargeQuestion = Some(true),
          noSurchargeQuestion = Some(true),
          surchargeAmount = None,
          surchargeTaxAmountQuestion = Some(true),
          surchargeTaxAmount = None,
          noSurchargeAmount = None,
          noSurchargeTaxAmountQuestion = Some(true),
          noSurchargeTaxAmount = None,
          ukPensionSchemesQuestion = Some(true),
          pensionSchemeTaxReference = Some(List("00123456RA"))
        ))

    }
  }
}
