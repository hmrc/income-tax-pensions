package models.database

import org.scalatest.wordspec.AnyWordSpecLike
import stubs.services.StubEncryptionService
import testdata.encryption._

class EncryptedTransfersIntoOverseasPensionsStorageAnswersSpec extends AnyWordSpecLike {
  "unsafeDecrypted" should {
    "decrypt data" in {
      val encryptedAnswers  = EncryptedTransfersIntoOverseasPensionsStorageAnswers(Some(encryptedTrue), Some(encryptedFalse), Some(encryptedTrue))
      val encryptionService = StubEncryptionService()

      val actual = encryptedAnswers.unsafeDecrypted(encryptionService, textAndKey)

      assert(actual === TransfersIntoOverseasPensionsStorageAnswers(Some(true), Some(false), Some(true)))
    }
  }
}
