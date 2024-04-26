package models.database

import models.frontend.PaymentsIntoPensionsAnswers
import org.scalatest.wordspec.AnyWordSpecLike
import PaymentsIntoPensionsStorageAnswers._

class PaymentsIntoPensionsStorageAnswersSpec extends AnyWordSpecLike {

  "fromJourneyAnswers" should {
    "convert answers to a storage model" in {
      val answers =
        PaymentsIntoPensionsAnswers(true, Some(1.0), Some(true), Some(2.0), true, Some(true), Some(3.0), Some(true), Some(4.0))
      val result = fromJourneyAnswers(answers)
      assert(result === PaymentsIntoPensionsStorageAnswers(true, Some(true), true, Some(true), Some(true)))
    }
  }
}
