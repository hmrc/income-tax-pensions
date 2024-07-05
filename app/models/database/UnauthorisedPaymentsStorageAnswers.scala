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
import models.frontend.UnauthorisedPaymentsAnswers
import play.api.libs.json.{Json, OFormat}
import services.EncryptionService
import utils.DecryptableSyntax.DecryptableOps
import utils.DecryptorInstances.booleanDecryptor
import utils.EncryptableSyntax.EncryptableOps
import utils.EncryptorInstances.booleanEncryptor

final case class UnauthorisedPaymentsStorageAnswers(surchargeQuestion: Option[Boolean],
                                                    noSurchargeQuestion: Option[Boolean],
                                                    surchargeTaxAmountQuestion: Option[Boolean],
                                                    noSurchargeTaxAmountQuestion: Option[Boolean],
                                                    ukPensionSchemesQuestion: Option[Boolean])
    extends StorageAnswers[UnauthorisedPaymentsStorageAnswers] {
  def encrypted(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKeyAes): EncryptedStorageAnswers[UnauthorisedPaymentsStorageAnswers] =
    EncryptedUnauthorisedPaymentsStorageAnswers(
      surchargeQuestion.map(_.encrypted),
      noSurchargeQuestion.map(_.encrypted),
      surchargeTaxAmountQuestion.map(_.encrypted),
      noSurchargeTaxAmountQuestion.map(_.encrypted),
      ukPensionSchemesQuestion.map(_.encrypted)
    )
}

object UnauthorisedPaymentsStorageAnswers {
  implicit val format: OFormat[UnauthorisedPaymentsStorageAnswers] = Json.format[UnauthorisedPaymentsStorageAnswers]

  def fromJourneyAnswers(answers: UnauthorisedPaymentsAnswers): UnauthorisedPaymentsStorageAnswers =
    UnauthorisedPaymentsStorageAnswers(
      answers.surchargeQuestion,
      answers.noSurchargeQuestion,
      answers.surchargeTaxAmountQuestion,
      answers.noSurchargeTaxAmountQuestion,
      answers.ukPensionSchemesQuestion
    )
}

final case class EncryptedUnauthorisedPaymentsStorageAnswers(surchargeQuestion: Option[EncryptedValue],
                                                             noSurchargeQuestion: Option[EncryptedValue],
                                                             surchargeTaxAmountQuestion: Option[EncryptedValue],
                                                             noSurchargeTaxAmountQuestion: Option[EncryptedValue],
                                                             ukPensionSchemesQuestion: Option[EncryptedValue])
    extends EncryptedStorageAnswers[UnauthorisedPaymentsStorageAnswers] {
  protected[database] def unsafeDecrypted(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKeyAes): UnauthorisedPaymentsStorageAnswers =
    UnauthorisedPaymentsStorageAnswers(
      surchargeQuestion.map(_.decrypted[Boolean]),
      noSurchargeQuestion.map(_.decrypted[Boolean]),
      surchargeTaxAmountQuestion.map(_.decrypted[Boolean]),
      noSurchargeTaxAmountQuestion.map(_.decrypted[Boolean]),
      ukPensionSchemesQuestion.map(_.decrypted[Boolean])
    )
}

object EncryptedUnauthorisedPaymentsStorageAnswers {
  implicit val format: OFormat[EncryptedUnauthorisedPaymentsStorageAnswers] = Json.format[EncryptedUnauthorisedPaymentsStorageAnswers]
}
