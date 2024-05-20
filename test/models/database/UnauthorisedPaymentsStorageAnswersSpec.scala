package models.database

import org.scalatest.wordspec.AnyWordSpecLike
import testdata.unauthorisedPayments.unauthorisedPaymentsAnswers

class UnauthorisedPaymentsStorageAnswersSpec extends AnyWordSpecLike {
  "fromJourneyAnswers" should {
    "create UnauthorisedPaymentsStorageAnswers" in {
      val result = UnauthorisedPaymentsStorageAnswers.fromJourneyAnswers(unauthorisedPaymentsAnswers)
      assert(result === UnauthorisedPaymentsStorageAnswers(Some(true), Some(true), Some(true), Some(true), Some(true)))
    }
  }
}
