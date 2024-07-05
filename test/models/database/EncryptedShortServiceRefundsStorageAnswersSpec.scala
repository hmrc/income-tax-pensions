package models.database

import org.scalatest.wordspec.AnyWordSpecLike
import stubs.services.StubEncryptionService
import testdata.encryption._

class EncryptedShortServiceRefundsStorageAnswersSpec extends AnyWordSpecLike {
  "unsafeDecrypted" should {
    "decrypt data" in {
      val encryptedAnswers  = EncryptedShortServiceRefundsStorageAnswers(Some(encryptedTrue), Some(encryptedFalse))
      val encryptionService = StubEncryptionService()

      val actual = encryptedAnswers.unsafeDecrypted(encryptionService, textAndKey)

      assert(actual === ShortServiceRefundsStorageAnswers(Some(true), Some(false)))
    }
  }
}
