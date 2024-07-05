package models.database

import org.scalatest.wordspec.AnyWordSpecLike
import stubs.services.StubEncryptionService
import testdata.encryption._

class EncryptedPaymentsIntoOverseasPensionsStorageAnswersSpec extends AnyWordSpecLike {
  "unsafeDecrypted" should {
    "decrypt data" in {
      val encryptedAnswers  = EncryptedPaymentsIntoOverseasPensionsStorageAnswers(Some(encryptedTrue), Some(encryptedFalse), Some(encryptedFalse))
      val encryptionService = StubEncryptionService()

      val actual = encryptedAnswers.unsafeDecrypted(encryptionService, textAndKey)

      assert(actual === PaymentsIntoOverseasPensionsStorageAnswers(Some(true), Some(false), Some(false)))
    }
  }
}
