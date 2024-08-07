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

import models.charges.{GetPensionChargesRequestModel, OverseasPensionContributions, OverseasRefundPensionScheme}
import models.encryption.EncryptedValue
import models.frontend.ShortServiceRefundsAnswers
import play.api.libs.json.{Json, OFormat}
import services.EncryptionService
import utils.DecryptableSyntax.DecryptableOps
import utils.DecryptorInstances.booleanDecryptor
import utils.EncryptableSyntax.EncryptableOps
import utils.EncryptorInstances.booleanEncryptor

final case class ShortServiceRefundsStorageAnswers(shortServiceRefund: Option[Boolean] = None, nonUKTaxOnShortServiceRefund: Option[Boolean] = None)
    extends StorageAnswers[ShortServiceRefundsStorageAnswers] {

  def isEmpty: Boolean = shortServiceRefund.isEmpty && nonUKTaxOnShortServiceRefund.isEmpty

  def toShortServiceRefundsAnswers(maybeCharges: Option[GetPensionChargesRequestModel]): Option[ShortServiceRefundsAnswers] = {
    val oPCAnswers: Option[OverseasPensionContributions] = maybeCharges.flatMap(_.overseasPensionContributions)
    val isEmptyOPCAnswers: Boolean                       = oPCAnswers.getOrElse(OverseasPensionContributions.empty).isEmpty

    val sSRGateway: Boolean    = oPCAnswers.map(_.shortServiceRefund).exists(_ != 0)
    val sSRTaxGateway: Boolean = oPCAnswers.map(_.shortServiceRefundTaxPaid).exists(_ != 0)

    val schemes: Seq[OverseasRefundPensionScheme] = oPCAnswers.map(_.overseasSchemeProvider.map(_.toOverseasRefundPensionScheme)).getOrElse(Seq())

    if (isEmptyOPCAnswers && isEmpty) None
    else
      Some(
        ShortServiceRefundsAnswers(
          shortServiceRefund = if (sSRGateway) Some(true) else shortServiceRefund,
          shortServiceRefundCharge = if (sSRGateway) oPCAnswers.map(_.shortServiceRefund) else None,
          shortServiceRefundTaxPaid = if (sSRTaxGateway) Some(true) else nonUKTaxOnShortServiceRefund,
          shortServiceRefundTaxPaidCharge = if (sSRTaxGateway) oPCAnswers.map(_.shortServiceRefundTaxPaid) else None,
          refundPensionScheme = if (oPCAnswers.nonEmpty) schemes else Seq[OverseasRefundPensionScheme]()
        ))
  }

  def encrypted(implicit aesGCMCrypto: EncryptionService, textAndKeyAes: TextAndKey): EncryptedStorageAnswers[ShortServiceRefundsStorageAnswers] =
    EncryptedShortServiceRefundsStorageAnswers(
      shortServiceRefund.map(_.encrypted),
      nonUKTaxOnShortServiceRefund.map(_.encrypted)
    )
}

object ShortServiceRefundsStorageAnswers {
  implicit val format: OFormat[ShortServiceRefundsStorageAnswers] = Json.format[ShortServiceRefundsStorageAnswers]

  def empty: ShortServiceRefundsStorageAnswers = ShortServiceRefundsStorageAnswers(None, None)

  def fromJourneyAnswers(answers: ShortServiceRefundsAnswers): ShortServiceRefundsStorageAnswers =
    ShortServiceRefundsStorageAnswers(
      answers.shortServiceRefund,
      answers.shortServiceRefundTaxPaid
    )
}

final case class EncryptedShortServiceRefundsStorageAnswers(shortServiceRefund: Option[EncryptedValue] = None,
                                                            nonUKTaxOnShortServiceRefund: Option[EncryptedValue] = None)
    extends EncryptedStorageAnswers[ShortServiceRefundsStorageAnswers] {

  protected[database] def unsafeDecrypted(implicit aesGCMCrypto: EncryptionService, textAndKeyAes: TextAndKey): ShortServiceRefundsStorageAnswers =
    ShortServiceRefundsStorageAnswers(
      shortServiceRefund.map(_.decrypted[Boolean]),
      nonUKTaxOnShortServiceRefund.map(_.decrypted[Boolean])
    )
}

object EncryptedShortServiceRefundsStorageAnswers {
  implicit val format: OFormat[EncryptedShortServiceRefundsStorageAnswers] = Json.format[EncryptedShortServiceRefundsStorageAnswers]
}
