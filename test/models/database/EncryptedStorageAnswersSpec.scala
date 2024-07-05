package models.database

import models.encryption.EncryptedValue
import models.error.ServiceError.CannotDecryptStorageDataError
import org.scalatest.wordspec.AnyWordSpecLike
import services.EncryptionService
import stubs.services.StubEncryptionService
import testdata.encryption.{encryptedFalse, textAndKey}
import utils.EitherTTestOps.convertScalaFuture

import scala.concurrent.ExecutionContext.Implicits.global

class EncryptedStorageAnswersSpec extends AnyWordSpecLike {
  "decryptedT" should {
    final case class Answer(value: Boolean)

    "return error if cannot be decrypted" in {
      final case class EncryptedAnswer(value: EncryptedValue) extends EncryptedStorageAnswers[Answer] {
        protected[database] def unsafeDecrypted(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKey): Answer =
          throw new RuntimeException("cannot be decrypted")
      }
      val encryptedAnswer   = EncryptedAnswer(encryptedFalse)
      val encryptionService = StubEncryptionService()

      val actual = encryptedAnswer.decryptedT(encryptionService, textAndKey).value.futureValue
      assert(actual === Left(CannotDecryptStorageDataError("cannot be decrypted")))
    }

    "return decrypted value" in {
      final case class EncryptedAnswer(value: EncryptedValue) extends EncryptedStorageAnswers[Answer] {
        protected[database] def unsafeDecrypted(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKey): Answer =
          Answer(true)
      }
      val encryptedAnswer   = EncryptedAnswer(encryptedFalse)
      val encryptionService = StubEncryptionService()

      val actual = encryptedAnswer.decryptedT(encryptionService, textAndKey).value.futureValue
      assert(actual === Right(Answer(true)))

    }
  }
}
