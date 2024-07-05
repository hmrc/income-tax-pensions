package models.database

import org.scalatest.wordspec.AnyWordSpecLike
import stubs.services.StubEncryptionService
import testdata.encryption._

class EncryptedUnauthorisedPaymentsStorageAnswersSpec extends AnyWordSpecLike {
  "unsafeDecrypted" should {
    "decrypt data" in {
      val encryptedAnswers = EncryptedUnauthorisedPaymentsStorageAnswers(
        Some(encryptedTrue),
        Some(encryptedFalse),
        Some(encryptedFalse),
        Some(encryptedFalse),
        Some(encryptedTrue))
      val encryptionService = StubEncryptionService()

      val actual = encryptedAnswers.unsafeDecrypted(encryptionService, textAndKey)

      assert(actual === UnauthorisedPaymentsStorageAnswers(Some(true), Some(false), Some(false), Some(false), Some(true)))
    }
  }
}
