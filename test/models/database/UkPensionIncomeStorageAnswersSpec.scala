package models.database

import models.encryption.EncryptedValue
import org.scalatest.wordspec.AnyWordSpecLike
import stubs.services.StubEncryptionService
import testdata.encryption.textAndKey

class UkPensionIncomeStorageAnswersSpec extends AnyWordSpecLike {
  "encrypted" should {
    "encrypt data" in {
      val answers           = UkPensionIncomeStorageAnswers(true)
      val encryptionService = StubEncryptionService()

      val actual = answers.encrypted(encryptionService, textAndKey)

      assert(actual === EncryptedUkPensionIncomeStorageAnswers(EncryptedValue("encrypted-true", "nonce")))
    }
  }
}
