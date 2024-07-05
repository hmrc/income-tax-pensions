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
import models.frontend.ShortServiceRefundsAnswers
import org.scalatest.wordspec.AnyWordSpecLike
import stubs.services.StubEncryptionService
import testdata.connector.getPensionChargesRequestModel._
import testdata.encryption.textAndKey
import testdata.shortServiceRefunds._

class ShortServiceRefundsStorageAnswersSpec extends AnyWordSpecLike {

  "fromJourneyAnswers" should {
    "create a ShortServiceRefundsStorageAnswers" in {
      val result = ShortServiceRefundsStorageAnswers.fromJourneyAnswers(shortServiceRefundsAnswers)
      assert(result === shortServiceRefundsCtxStorageAnswers)
    }
  }

  "toShortServiceRefundsAnswers" should {
    "create a ShortServiceRefundsAnswers if there are existing API and DB answers" in {
      val result = shortServiceRefundsCtxStorageAnswers.toShortServiceRefundsAnswers(Some(getPensionChargesRequestModel))
      assert(result === Some(shortServiceRefundsAnswers))
    }

    "create a ShortServiceRefundsAnswers with API answers overruling DB answers" in {
      val emptyStorageAnswers = ShortServiceRefundsStorageAnswers()
      val result              = emptyStorageAnswers.toShortServiceRefundsAnswers(Some(getPensionChargesRequestModel))
      assert(result === Some(shortServiceRefundsAnswers))
    }

    "create a ShortServiceRefundsAnswers with DB answers only" in {
      val shortServiceRefundsAnswers = ShortServiceRefundsAnswers(Some(true), None, Some(true), None, Seq())
      val result                     = shortServiceRefundsCtxStorageAnswers.toShortServiceRefundsAnswers(None)
      assert(result === Some(shortServiceRefundsAnswers))
    }

    "return None if there are no API and DB answers" in {
      val emptyStorageAnswers = ShortServiceRefundsStorageAnswers()
      val result              = emptyStorageAnswers.toShortServiceRefundsAnswers(None)
      assert(result === None)
    }
  }

  "encrypted" should {
    "encrypt data" in {
      val answers           = ShortServiceRefundsStorageAnswers(Some(true), Some(false))
      val encryptionService = StubEncryptionService()

      val actual = answers.encrypted(encryptionService, textAndKey)

      assert(
        actual === EncryptedShortServiceRefundsStorageAnswers(
          Some(EncryptedValue("encrypted-true", "nonce")),
          Some(EncryptedValue("encrypted-false", "nonce"))))

    }
  }
}
