package models.database

import org.scalatest.wordspec.AnyWordSpecLike
import stubs.services.StubEncryptionService
import testdata.encryption._

class EncryptedIncomeFromOverseasPensionsStorageAnswersSpec extends AnyWordSpecLike {
  "unsafeDecrypted" should {
    "decrypt data" in {
      val encryptedAnswers = EncryptedIncomeFromOverseasPensionsStorageAnswers(
        Some(encryptedTrue),
        List(EncryptedPensionSchemeStorageAnswers(Some(encryptedFalse), Some(encryptedFalse))))
      val encryptionService = StubEncryptionService()

      val actual = encryptedAnswers.unsafeDecrypted(encryptionService, textAndKey)

      assert(actual === IncomeFromOverseasPensionsStorageAnswers(Some(true), List(PensionSchemeStorageAnswers(Some(false), Some(false)))))
    }
  }
}
