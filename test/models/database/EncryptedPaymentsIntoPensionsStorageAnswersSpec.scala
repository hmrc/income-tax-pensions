package models.database

import org.scalatest.wordspec.AnyWordSpecLike
import stubs.services.StubEncryptionService
import testdata.encryption._

class EncryptedPaymentsIntoPensionsStorageAnswersSpec extends AnyWordSpecLike {
  "unsafeDecrypted" should {
    "decrypt data" in {
      val encryptedAnswers =
        EncryptedPaymentsIntoPensionsStorageAnswers(encryptedTrue, Some(encryptedFalse), encryptedFalse, Some(encryptedTrue), Some(encryptedFalse))
      val encryptionService = StubEncryptionService()

      val actual = encryptedAnswers.unsafeDecrypted(encryptionService, textAndKey)

      assert(actual === PaymentsIntoPensionsStorageAnswers(true, Some(false), false, Some(true), Some(false)))
    }
  }
}
