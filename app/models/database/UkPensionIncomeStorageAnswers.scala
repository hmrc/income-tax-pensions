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
import models.frontend.ukpensionincome.UkPensionIncomeAnswers
import play.api.libs.json.{Json, OFormat}
import services.EncryptionService
import utils.DecryptableSyntax.DecryptableOps
import utils.DecryptorInstances.booleanDecryptor
import utils.EncryptableSyntax.EncryptableOps
import utils.EncryptorInstances.booleanEncryptor

final case class UkPensionIncomeStorageAnswers(uKPensionIncomesQuestion: Boolean) extends StorageAnswers[UkPensionIncomeStorageAnswers] {
  def encrypted(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKeyAes): EncryptedStorageAnswers[UkPensionIncomeStorageAnswers] =
    EncryptedUkPensionIncomeStorageAnswers(
      uKPensionIncomesQuestion.encrypted
    )
}

object UkPensionIncomeStorageAnswers {
  implicit val format: OFormat[UkPensionIncomeStorageAnswers] = Json.format[UkPensionIncomeStorageAnswers]

  def fromJourneyAnswers(answers: UkPensionIncomeAnswers): UkPensionIncomeStorageAnswers =
    UkPensionIncomeStorageAnswers(answers.uKPensionIncomesQuestion)
}

final case class EncryptedUkPensionIncomeStorageAnswers(uKPensionIncomesQuestion: EncryptedValue)
    extends EncryptedStorageAnswers[UkPensionIncomeStorageAnswers] {

  protected[database] def unsafeDecrypted(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKeyAes): UkPensionIncomeStorageAnswers =
    UkPensionIncomeStorageAnswers(
      uKPensionIncomesQuestion.decrypted[Boolean]
    )
}

object EncryptedUkPensionIncomeStorageAnswers {
  implicit val format: OFormat[EncryptedUkPensionIncomeStorageAnswers] = Json.format[EncryptedUkPensionIncomeStorageAnswers]
}
