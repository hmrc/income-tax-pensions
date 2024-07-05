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
import models.frontend.AnnualAllowancesAnswers
import play.api.libs.json.{Json, OFormat}
import services.EncryptionService
import utils.DecryptableSyntax._
import utils.DecryptorInstances.booleanDecryptor
import utils.EncryptableSyntax.EncryptableOps
import utils.EncryptorInstances.booleanEncryptor

final case class AnnualAllowancesStorageAnswers(aboveAnnualAllowanceQuestion: Option[Boolean],
                                                pensionProvidePaidAnnualAllowanceQuestion: Option[Boolean])
    extends StorageAnswers[AnnualAllowancesStorageAnswers] {
  def toAnnualAllowancesAnswers: AnnualAllowancesAnswers =
    AnnualAllowancesAnswers.empty.copy(
      aboveAnnualAllowanceQuestion = aboveAnnualAllowanceQuestion,
      pensionProvidePaidAnnualAllowanceQuestion = pensionProvidePaidAnnualAllowanceQuestion
    )

  def encrypted(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKeyAes): EncryptedStorageAnswers[AnnualAllowancesStorageAnswers] =
    EncryptedAnnualAllowancesStorageAnswers(aboveAnnualAllowanceQuestion.map(_.encrypted), pensionProvidePaidAnnualAllowanceQuestion.map(_.encrypted))
}

object AnnualAllowancesStorageAnswers {
  implicit val format: OFormat[AnnualAllowancesStorageAnswers] = Json.format[AnnualAllowancesStorageAnswers]

  def fromJourneyAnswers(answers: AnnualAllowancesAnswers): AnnualAllowancesStorageAnswers =
    AnnualAllowancesStorageAnswers(
      answers.aboveAnnualAllowanceQuestion,
      answers.pensionProvidePaidAnnualAllowanceQuestion
    )
}

final case class EncryptedAnnualAllowancesStorageAnswers(aboveAnnualAllowanceQuestion: Option[EncryptedValue],
                                                         pensionProvidePaidAnnualAllowanceQuestion: Option[EncryptedValue])
    extends EncryptedStorageAnswers[AnnualAllowancesStorageAnswers] {

  protected[database] def unsafeDecrypted(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKeyAes): AnnualAllowancesStorageAnswers =
    AnnualAllowancesStorageAnswers(
      aboveAnnualAllowanceQuestion.map(_.decrypted[Boolean]),
      pensionProvidePaidAnnualAllowanceQuestion.map(_.decrypted[Boolean]))
}

object EncryptedAnnualAllowancesStorageAnswers {
  implicit val format: OFormat[EncryptedAnnualAllowancesStorageAnswers] = Json.format[EncryptedAnnualAllowancesStorageAnswers]
}
