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
import models.encryption.EncryptedValue
import models.frontend.PaymentsIntoOverseasPensionsAnswers
import models.{GetPensionIncomeModel, GetPensionReliefsModel, OverseasPensionContribution, maybeSeqToList}
import play.api.libs.json.{Json, OFormat}
import services.EncryptionService
import utils.DecryptableSyntax.DecryptableOps
import utils.DecryptorInstances.booleanDecryptor
import utils.EncryptableSyntax.EncryptableOps
import utils.EncryptorInstances.booleanEncryptor

final case class PaymentsIntoOverseasPensionsStorageAnswers(paymentsIntoOverseasPensionsQuestions: Option[Boolean] = None,
                                                            employerPaymentsQuestion: Option[Boolean] = None,
                                                            taxPaidOnEmployerPaymentsQuestion: Option[Boolean] = None)
    extends StorageAnswers[PaymentsIntoOverseasPensionsStorageAnswers] {

  private def isEmpty: Boolean =
    paymentsIntoOverseasPensionsQuestions.isEmpty && employerPaymentsQuestion.isEmpty && taxPaidOnEmployerPaymentsQuestion.isEmpty

  def toPaymentsIntoOverseasPensionsAnswers(maybeIncomes: Option[GetPensionIncomeModel],
                                            maybeReliefs: Option[GetPensionReliefsModel]): Option[PaymentsIntoOverseasPensionsAnswers] = {
    val apiHasAmount: Boolean = maybeReliefs.exists(_.pensionReliefs.overseasPensionSchemeContributions.nonEmpty)
    val overseasPensionContributions: List[OverseasPensionContribution] = maybeSeqToList(maybeIncomes.flatMap(_.overseasPensionContribution))
    if (!apiHasAmount && isEmpty) None
    else
      PaymentsIntoOverseasPensionsAnswers(
        paymentsIntoOverseasPensionsQuestions = apiHasAmount.some,
        paymentsIntoOverseasPensionsAmount = maybeReliefs.flatMap(_.pensionReliefs.overseasPensionSchemeContributions),
        employerPaymentsQuestion = if (!apiHasAmount) None else if (overseasPensionContributions.nonEmpty) true.some else employerPaymentsQuestion,
        taxPaidOnEmployerPaymentsQuestion =
          if (!apiHasAmount) None else if (overseasPensionContributions.nonEmpty) false.some else taxPaidOnEmployerPaymentsQuestion,
        schemes = overseasPensionContributions.map(_.toOverseasPensionScheme)
      ).some
  }

  def encrypted(implicit
      aesGCMCrypto: EncryptionService,
      textAndKey: TextAndKeyAes): EncryptedStorageAnswers[PaymentsIntoOverseasPensionsStorageAnswers] =
    EncryptedPaymentsIntoOverseasPensionsStorageAnswers(
      paymentsIntoOverseasPensionsQuestions.map(_.encrypted),
      employerPaymentsQuestion.map(_.encrypted),
      taxPaidOnEmployerPaymentsQuestion.map(_.encrypted)
    )
}

object PaymentsIntoOverseasPensionsStorageAnswers {
  implicit val format: OFormat[PaymentsIntoOverseasPensionsStorageAnswers] = Json.format[PaymentsIntoOverseasPensionsStorageAnswers]

  def empty: PaymentsIntoOverseasPensionsStorageAnswers = PaymentsIntoOverseasPensionsStorageAnswers(None, None, None)
}

final case class EncryptedPaymentsIntoOverseasPensionsStorageAnswers(paymentsIntoOverseasPensionsQuestions: Option[EncryptedValue],
                                                                     employerPaymentsQuestion: Option[EncryptedValue],
                                                                     taxPaidOnEmployerPaymentsQuestion: Option[EncryptedValue])
    extends EncryptedStorageAnswers[PaymentsIntoOverseasPensionsStorageAnswers] {
  protected[database] def unsafeDecrypted(implicit
      aesGCMCrypto: EncryptionService,
      textAndKey: TextAndKeyAes): PaymentsIntoOverseasPensionsStorageAnswers =
    PaymentsIntoOverseasPensionsStorageAnswers(
      paymentsIntoOverseasPensionsQuestions.map(_.decrypted[Boolean]),
      employerPaymentsQuestion.map(_.decrypted[Boolean]),
      taxPaidOnEmployerPaymentsQuestion.map(_.decrypted[Boolean])
    )
}

object EncryptedPaymentsIntoOverseasPensionsStorageAnswers {
  implicit val format: OFormat[EncryptedPaymentsIntoOverseasPensionsStorageAnswers] = Json.format[EncryptedPaymentsIntoOverseasPensionsStorageAnswers]
}
