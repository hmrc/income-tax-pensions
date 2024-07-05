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
import models.frontend.statepension.{IncomeFromPensionsStatePensionAnswers, StateBenefitAnswers}
import play.api.libs.json.{Json, OFormat}
import services.EncryptionService
import utils.DecryptableSyntax.DecryptableOps
import utils.DecryptorInstances.booleanDecryptor
import utils.EncryptableSyntax.EncryptableOps
import utils.EncryptorInstances.booleanEncryptor

final case class IncomeFromPensionsStatePensionStorageAnswers(statePension: Option[Boolean], statePensionLumpSum: Option[Boolean])
    extends StorageAnswers[IncomeFromPensionsStatePensionStorageAnswers] {

  def toIncomeFromPensionsStatePensionAnswers: IncomeFromPensionsStatePensionAnswers =
    IncomeFromPensionsStatePensionAnswers(
      statePension = statePension.map(answer => StateBenefitAnswers.empty.copy(amountPaidQuestion = Some(answer))),
      statePensionLumpSum = statePensionLumpSum.map(answer => StateBenefitAnswers.empty.copy(amountPaidQuestion = Some(answer))),
      sessionId = None
    )

  def encrypted(implicit
      aesGCMCrypto: EncryptionService,
      textAndKey: TextAndKey): EncryptedStorageAnswers[IncomeFromPensionsStatePensionStorageAnswers] =
    EncryptedIncomeFromPensionsStatePensionStorageAnswers(
      statePension.map(_.encrypted),
      statePensionLumpSum.map(_.encrypted)
    )
}

object IncomeFromPensionsStatePensionStorageAnswers {
  implicit val format: OFormat[IncomeFromPensionsStatePensionStorageAnswers] = Json.format[IncomeFromPensionsStatePensionStorageAnswers]

  def fromJourneyAnswers(answers: IncomeFromPensionsStatePensionAnswers): IncomeFromPensionsStatePensionStorageAnswers =
    IncomeFromPensionsStatePensionStorageAnswers(
      answers.statePension.flatMap(_.amountPaidQuestion),
      answers.statePensionLumpSum.flatMap(_.amountPaidQuestion)
    )
}
final case class EncryptedIncomeFromPensionsStatePensionStorageAnswers(statePension: Option[EncryptedValue],
                                                                       statePensionLumpSum: Option[EncryptedValue])
    extends EncryptedStorageAnswers[IncomeFromPensionsStatePensionStorageAnswers] {
  protected[database] def unsafeDecrypted(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKey): IncomeFromPensionsStatePensionStorageAnswers =
    IncomeFromPensionsStatePensionStorageAnswers(
      statePension.map(_.decrypted[Boolean]),
      statePensionLumpSum.map(_.decrypted[Boolean])
    )
}

object EncryptedIncomeFromPensionsStatePensionStorageAnswers {
  implicit val format: OFormat[EncryptedIncomeFromPensionsStatePensionStorageAnswers] =
    Json.format[EncryptedIncomeFromPensionsStatePensionStorageAnswers]
}
