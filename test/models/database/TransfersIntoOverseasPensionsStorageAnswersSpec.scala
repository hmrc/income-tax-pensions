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

import cats.implicits.catsSyntaxOptionId
import com.codahale.metrics.SharedMetricRegistries
import models.encryption.EncryptedValue
import stubs.services.StubEncryptionService
import testdata.connector.getPensionChargesRequestModel.getPensionChargesRequestModel
import testdata.encryption.textAndKeyAes
import testdata.transfersIntoOverseasPensions.{transfersIntoOverseasPensionsAnswers, transfersIntoOverseasPensionsStorageAnswers}
import utils.TestUtils

class TransfersIntoOverseasPensionsStorageAnswersSpec extends TestUtils {
  SharedMetricRegistries.clear()

  "toTransfersIntoOverseasPensions" should {
    "return None when there are no DB or API answers" in {
      assert(TransfersIntoOverseasPensionsStorageAnswers.empty.toTransfersIntoOverseasPensions(None) == None)
    }
    "return full journey answers, combining API and DB answers" in {
      assert(
        transfersIntoOverseasPensionsStorageAnswers.toTransfersIntoOverseasPensions(
          getPensionChargesRequestModel.some) == transfersIntoOverseasPensionsAnswers.some)
    }
    "return full journey answers, overruling DB answers when API answers are different" in {
      assert(
        TransfersIntoOverseasPensionsStorageAnswers.empty.toTransfersIntoOverseasPensions(
          getPensionChargesRequestModel.some) == transfersIntoOverseasPensionsAnswers.some)
    }
  }

  "encrypted" should {
    "encrypt data" in {
      val answers           = TransfersIntoOverseasPensionsStorageAnswers(Some(true), Some(false), Some(true))
      val encryptionService = StubEncryptionService()

      val actual = answers.encrypted(encryptionService, textAndKeyAes)

      assert(
        actual === EncryptedTransfersIntoOverseasPensionsStorageAnswers(
          Some(EncryptedValue("encrypted-true", "nonce")),
          Some(EncryptedValue("encrypted-false", "nonce")),
          Some(EncryptedValue("encrypted-true", "nonce"))))
    }
  }
}
