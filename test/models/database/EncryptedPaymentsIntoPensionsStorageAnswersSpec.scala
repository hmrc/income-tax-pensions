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

import org.scalatest.wordspec.AnyWordSpecLike
import stubs.services.StubEncryptionService
import testdata.encryption._

class EncryptedPaymentsIntoPensionsStorageAnswersSpec extends AnyWordSpecLike {
  "unsafeDecrypted" should {
    "decrypt data" in {
      val encryptedAnswers =
        EncryptedPaymentsIntoPensionsStorageAnswers(encryptedTrue, Some(encryptedFalse), encryptedFalse, Some(encryptedTrue), Some(encryptedFalse))
      val encryptionService = StubEncryptionService()

      val actual = encryptedAnswers.unsafeDecrypted(encryptionService, textAndKeyAes)

      assert(actual === PaymentsIntoPensionsStorageAnswers(true, Some(false), false, Some(true), Some(false)))
    }
  }
}
