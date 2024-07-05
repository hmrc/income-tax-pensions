package models.database

import org.scalatest.wordspec.AnyWordSpecLike
import stubs.services.StubEncryptionService
import testdata.encryption._

class EncryptedAnnualAllowancesStorageAnswersSpec extends AnyWordSpecLike {
  "unsafeDecrypted" should {
    "decrypt data" in {
      val encryptedAnswers  = EncryptedAnnualAllowancesStorageAnswers(Some(encryptedTrue), Some(encryptedFalse))
      val encryptionService = StubEncryptionService()

      val actual = encryptedAnswers.unsafeDecrypted(encryptionService, textAndKey)

      assert(actual === AnnualAllowancesStorageAnswers(Some(true), Some(false)))
    }
  }
}
