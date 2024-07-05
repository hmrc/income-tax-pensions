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

import models.database.PaymentsIntoPensionsStorageAnswers._
import models.encryption.EncryptedValue
import org.scalatest.wordspec.AnyWordSpecLike
import stubs.services.StubEncryptionService
import testdata.encryption.textAndKey
import testdata.paymentsIntoPensions

class PaymentsIntoPensionsStorageAnswersSpec extends AnyWordSpecLike {

  "fromJourneyAnswers" should {
    "convert answers to a storage model" in {
      val answers = paymentsIntoPensions.paymentsIntoPensionsAnswers
      val result  = fromJourneyAnswers(answers)
      assert(result === PaymentsIntoPensionsStorageAnswers(true, Some(true), true, Some(true), Some(true)))
    }
  }

  "encrypted" should {
    "encrypt data" in {
      val answers           = PaymentsIntoPensionsStorageAnswers(true, Some(true), true, Some(true), Some(true))
      val encryptionService = StubEncryptionService()

      val actual = answers.encrypted(encryptionService, textAndKey)

      assert(
        actual === EncryptedPaymentsIntoPensionsStorageAnswers(
          EncryptedValue("encrypted-true", "nonce"),
          Some(EncryptedValue("encrypted-true", "nonce")),
          EncryptedValue("encrypted-true", "nonce"),
          Some(EncryptedValue("encrypted-true", "nonce")),
          Some(EncryptedValue("encrypted-true", "nonce"))
        ))
    }
  }
}
