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
import models.frontend.IncomeFromOverseasPensionsAnswers
import play.api.libs.json.{Json, OFormat}
import services.EncryptionService
import utils.DecryptableSyntax.DecryptableOps
import utils.DecryptorInstances.booleanDecryptor
import utils.EncryptableSyntax.EncryptableOps
import utils.EncryptorInstances.booleanEncryptor

final case class IncomeFromOverseasPensionsStorageAnswers(paymentsFromOverseasPensionsQuestion: Option[Boolean] = None,
                                                          overseasIncomePensionSchemes: Seq[PensionSchemeStorageAnswers] = Nil)
    extends StorageAnswers[IncomeFromOverseasPensionsStorageAnswers] {
  def encrypted(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKey): EncryptedStorageAnswers[IncomeFromOverseasPensionsStorageAnswers] =
    EncryptedIncomeFromOverseasPensionsStorageAnswers(
      paymentsFromOverseasPensionsQuestion.map(_.encrypted),
      overseasIncomePensionSchemes.map(_.encrypted)
    )
}

final case class PensionSchemeStorageAnswers(specialWithholdingTaxQuestion: Option[Boolean] = None,
                                             foreignTaxCreditReliefQuestion: Option[Boolean] = None) {
  def encrypted(implicit aesGCMvCrypto: EncryptionService, textAndKey: TextAndKey): EncryptedPensionSchemeStorageAnswers =
    EncryptedPensionSchemeStorageAnswers(
      specialWithholdingTaxQuestion.map(_.encrypted),
      foreignTaxCreditReliefQuestion.map(_.encrypted)
    )
}

final case class EncryptedPensionSchemeStorageAnswers(
    specialWithholdingTaxQuestion: Option[EncryptedValue],
    foreignTaxCreditReliefQuestion: Option[EncryptedValue]
) extends EncryptedStorageAnswers[PensionSchemeStorageAnswers] {
  def unsafeDecrypted(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKey): PensionSchemeStorageAnswers =
    PensionSchemeStorageAnswers(
      specialWithholdingTaxQuestion.map(_.decrypted[Boolean]),
      foreignTaxCreditReliefQuestion.map(_.decrypted[Boolean])
    )
}

final case class EncryptedIncomeFromOverseasPensionsStorageAnswers(paymentsFromOverseasPensionsQuestion: Option[EncryptedValue],
                                                                   overseasIncomePensionSchemes: Seq[EncryptedPensionSchemeStorageAnswers])
    extends EncryptedStorageAnswers[IncomeFromOverseasPensionsStorageAnswers] {
  protected[database] def unsafeDecrypted(implicit
      aesGCMCrypto: EncryptionService,
      textAndKey: TextAndKey): IncomeFromOverseasPensionsStorageAnswers =
    IncomeFromOverseasPensionsStorageAnswers(
      paymentsFromOverseasPensionsQuestion.map(_.decrypted[Boolean]),
      overseasIncomePensionSchemes.map(_.unsafeDecrypted)
    )
}

object IncomeFromOverseasPensionsStorageAnswers {
  implicit val format: OFormat[IncomeFromOverseasPensionsStorageAnswers] = Json.format[IncomeFromOverseasPensionsStorageAnswers]

  def fromJourneyAnswers(answers: IncomeFromOverseasPensionsAnswers): IncomeFromOverseasPensionsStorageAnswers =
    IncomeFromOverseasPensionsStorageAnswers(
      answers.paymentsFromOverseasPensionsQuestion,
      answers.overseasIncomePensionSchemes.map(answer =>
        PensionSchemeStorageAnswers(
          answer.specialWithholdingTaxQuestion,
          answer.foreignTaxCreditReliefQuestion
        ))
    )
}

object PensionSchemeStorageAnswers {
  implicit val format: OFormat[PensionSchemeStorageAnswers] = Json.format[PensionSchemeStorageAnswers]
}

object EncryptedIncomeFromOverseasPensionsStorageAnswers {
  implicit val format: OFormat[EncryptedIncomeFromOverseasPensionsStorageAnswers] = Json.format[EncryptedIncomeFromOverseasPensionsStorageAnswers]
}

object EncryptedPensionSchemeStorageAnswers {
  implicit val format: OFormat[EncryptedPensionSchemeStorageAnswers] = Json.format[EncryptedPensionSchemeStorageAnswers]
}
