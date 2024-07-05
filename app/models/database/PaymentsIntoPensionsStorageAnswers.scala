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
import models.frontend.PaymentsIntoPensionsAnswers
import play.api.libs.json.{Json, OFormat}
import services.EncryptionService
import utils.DecryptableSyntax.DecryptableOps
import utils.DecryptorInstances.booleanDecryptor
import utils.EncryptableSyntax.EncryptableOps
import utils.EncryptorInstances.booleanEncryptor

final case class PaymentsIntoPensionsStorageAnswers(rasPensionPaymentQuestion: Boolean,
                                                    oneOffRasPaymentPlusTaxReliefQuestion: Option[Boolean],
                                                    pensionTaxReliefNotClaimedQuestion: Boolean,
                                                    retirementAnnuityContractPaymentsQuestion: Option[Boolean],
                                                    workplacePensionPaymentsQuestion: Option[Boolean])
    extends StorageAnswers[PaymentsIntoPensionsStorageAnswers] {

  def encrypted(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKeyAes): EncryptedPaymentsIntoPensionsStorageAnswers =
    EncryptedPaymentsIntoPensionsStorageAnswers(
      rasPensionPaymentQuestion.encrypted,
      oneOffRasPaymentPlusTaxReliefQuestion.map(_.encrypted),
      pensionTaxReliefNotClaimedQuestion.encrypted,
      retirementAnnuityContractPaymentsQuestion.map(_.encrypted),
      workplacePensionPaymentsQuestion.map(_.encrypted)
    )

}

object PaymentsIntoPensionsStorageAnswers {
  implicit val format: OFormat[PaymentsIntoPensionsStorageAnswers] = Json.format[PaymentsIntoPensionsStorageAnswers]

  def fromJourneyAnswers(answers: PaymentsIntoPensionsAnswers): PaymentsIntoPensionsStorageAnswers =
    PaymentsIntoPensionsStorageAnswers(
      answers.rasPensionPaymentQuestion,
      answers.oneOffRasPaymentPlusTaxReliefQuestion,
      answers.pensionTaxReliefNotClaimedQuestion,
      answers.retirementAnnuityContractPaymentsQuestion,
      answers.workplacePensionPaymentsQuestion
    )
}

final case class EncryptedPaymentsIntoPensionsStorageAnswers(rasPensionPaymentQuestion: EncryptedValue,
                                                             oneOffRasPaymentPlusTaxReliefQuestion: Option[EncryptedValue],
                                                             pensionTaxReliefNotClaimedQuestion: EncryptedValue,
                                                             retirementAnnuityContractPaymentsQuestion: Option[EncryptedValue],
                                                             workplacePensionPaymentsQuestion: Option[EncryptedValue])
    extends EncryptedStorageAnswers[PaymentsIntoPensionsStorageAnswers] {

  protected[database] def unsafeDecrypted(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKeyAes): PaymentsIntoPensionsStorageAnswers =
    PaymentsIntoPensionsStorageAnswers(
      rasPensionPaymentQuestion.decrypted[Boolean],
      oneOffRasPaymentPlusTaxReliefQuestion.map(_.decrypted[Boolean]),
      pensionTaxReliefNotClaimedQuestion.decrypted[Boolean],
      retirementAnnuityContractPaymentsQuestion.map(_.decrypted[Boolean]),
      workplacePensionPaymentsQuestion.map(_.decrypted[Boolean])
    )

}

object EncryptedPaymentsIntoPensionsStorageAnswers {
  implicit val format: OFormat[EncryptedPaymentsIntoPensionsStorageAnswers] = Json.format[EncryptedPaymentsIntoPensionsStorageAnswers]
}
