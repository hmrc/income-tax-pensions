package models.database

import org.scalatest.wordspec.AnyWordSpecLike
import stubs.services.StubEncryptionService
import testdata.encryption._

class EncryptedUkPensionIncomeStorageAnswersSpec extends AnyWordSpecLike {
  "unsafeDecrypted" should {
    "decrypt data" in {
      val encryptedAnswers  = EncryptedUkPensionIncomeStorageAnswers(encryptedTrue)
      val encryptionService = StubEncryptionService()

      val actual = encryptedAnswers.unsafeDecrypted(encryptionService, textAndKey)

      assert(actual === UkPensionIncomeStorageAnswers(true))
    }
  }
}
