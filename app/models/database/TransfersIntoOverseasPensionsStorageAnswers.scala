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
import models.charges.{GetPensionChargesRequestModel, OverseasSchemeProvider, PensionSchemeOverseasTransfers}
import models.frontend.TransfersIntoOverseasPensionsAnswers
import models.isNonZero
import play.api.libs.json.{Json, OFormat}

final case class TransfersIntoOverseasPensionsStorageAnswers(transferPensionSavings: Option[Boolean],
                                                             overseasTransferCharge: Option[Boolean],
                                                             pensionSchemeTransferCharge: Option[Boolean]) {

  private def isEmpty: Boolean = transferPensionSavings.isEmpty && pensionSchemeTransferCharge.isEmpty && pensionSchemeTransferCharge.isEmpty

  def toTransfersIntoOverseasPensions(maybeCharges: Option[GetPensionChargesRequestModel]): Option[TransfersIntoOverseasPensionsAnswers] = {
    val maybePSOT: Option[PensionSchemeOverseasTransfers] = maybeCharges.flatMap(_.pensionSchemeOverseasTransfers)
    val apiHasAnswers: Boolean                            = maybePSOT.exists(_.transferCharge != BigDecimal(0))
    val transferChargeGateway                             = maybePSOT.exists(x => isNonZero(x.transferCharge))
    val transferChargeTaxGateway                          = maybePSOT.exists(x => isNonZero(x.transferChargeTaxPaid))
    val osp: Seq[OverseasSchemeProvider]                  = maybePSOT.map(_.overseasSchemeProvider).getOrElse(Seq.empty)
    if (!apiHasAnswers && isEmpty) None
    else
      TransfersIntoOverseasPensionsAnswers(
        transferPensionSavings = if (transferChargeGateway) true.some else transferPensionSavings,
        overseasTransferCharge = if (transferChargeGateway) true.some else overseasTransferCharge,
        overseasTransferChargeAmount = if (transferChargeGateway) maybePSOT.map(_.transferCharge) else None,
        pensionSchemeTransferCharge = if (transferChargeTaxGateway) true.some else pensionSchemeTransferCharge,
        pensionSchemeTransferChargeAmount = if (transferChargeTaxGateway) maybePSOT.map(_.transferChargeTaxPaid) else None,
        transferPensionScheme = if (osp.nonEmpty) osp.map(_.toTransferPensionScheme) else Nil
      ).some
  }
}

object TransfersIntoOverseasPensionsStorageAnswers {
  implicit val format: OFormat[TransfersIntoOverseasPensionsStorageAnswers] = Json.format[TransfersIntoOverseasPensionsStorageAnswers]

  def empty = TransfersIntoOverseasPensionsStorageAnswers(None, None, None)

  def fromJourneyAnswers(answers: TransfersIntoOverseasPensionsAnswers): TransfersIntoOverseasPensionsStorageAnswers =
    TransfersIntoOverseasPensionsStorageAnswers(
      answers.transferPensionSavings,
      answers.overseasTransferCharge,
      answers.pensionSchemeTransferCharge
    )
}
