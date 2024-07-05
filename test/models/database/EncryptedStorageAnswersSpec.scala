/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        protected[database] def unsafeDecrypted(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKeyAes): Answer =
          throw new RuntimeException("cannot be decrypted")
      }
      val encryptedAnswer   = EncryptedAnswer(encryptedFalse)
      val encryptionService = StubEncryptionService()

      val actual = encryptedAnswer.decryptedT(encryptionService, textAndKey).value.futureValue
      assert(actual === Left(CannotDecryptStorageDataError("cannot be decrypted")))
    }

    "return decrypted value" in {
      final case class EncryptedAnswer(value: EncryptedValue) extends EncryptedStorageAnswers[Answer] {
        protected[database] def unsafeDecrypted(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKeyAes): Answer =
          Answer(true)
      }
      val encryptedAnswer   = EncryptedAnswer(encryptedFalse)
      val encryptionService = StubEncryptionService()

      val actual = encryptedAnswer.decryptedT(encryptionService, textAndKey).value.futureValue
      assert(actual === Right(Answer(true)))

    }
  }
}
