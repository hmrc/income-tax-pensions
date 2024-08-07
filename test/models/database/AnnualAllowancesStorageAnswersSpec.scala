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

import models.database.AnnualAllowancesStorageAnswers._
import models.encryption.EncryptedValue
import org.scalatest.wordspec.AnyWordSpecLike
import stubs.services.StubEncryptionService
import testdata.annualAllowances.annualAllowancesAnswers
import testdata.encryption.textAndKeyAes

class AnnualAllowancesStorageAnswersSpec extends AnyWordSpecLike {

  "fromJourneyAnswers" should {
    "convert answers to a storage model" in {
      val answers = annualAllowancesAnswers
      val result  = fromJourneyAnswers(answers)
      assert(result === AnnualAllowancesStorageAnswers(Some(true), Some(true)))
    }
  }

  "encrypted" should {
    "encrypt data" in {
      val answers           = AnnualAllowancesStorageAnswers(Some(true), Some(false))
      val encryptionService = StubEncryptionService()

      val actual = answers.encrypted(encryptionService, textAndKeyAes)

      assert(
        actual === EncryptedAnnualAllowancesStorageAnswers(
          Some(EncryptedValue("encrypted-true", "nonce")),
          Some(EncryptedValue("encrypted-false", "nonce"))))
    }
  }
}
