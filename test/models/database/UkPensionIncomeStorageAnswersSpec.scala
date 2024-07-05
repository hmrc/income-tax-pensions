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
