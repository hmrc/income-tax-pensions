/*
 * Copyright 2023 HM Revenue & Customs
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

package models.frontend

import models.charges.{GetPensionChargesRequestModel, PensionSchemeOverseasTransfers}
import models.database.TransfersIntoOverseasPensionsStorageAnswers
import models.domain.PensionAnswers
import play.api.libs.json.{Json, OFormat}
import utils.Constants.zero

case class TransfersIntoOverseasPensionsAnswers(transferPensionSavings: Option[Boolean] = None,
                                                overseasTransferCharge: Option[Boolean] = None,
                                                overseasTransferChargeAmount: Option[BigDecimal] = None,
                                                pensionSchemeTransferCharge: Option[Boolean] = None,
                                                pensionSchemeTransferChargeAmount: Option[BigDecimal] = None,
                                                transferPensionScheme: Seq[TransferPensionScheme] = Nil)
    extends PensionAnswers {
  def isFinished: Boolean =
    transferPensionSavings.exists(x =>
      !x || overseasTransferCharge.exists(x =>
        !x || overseasTransferChargeAmount.isDefined && pensionSchemeTransferCharge.exists(x =>
          !x || pensionSchemeTransferChargeAmount.isDefined && transferPensionScheme.nonEmpty && transferPensionScheme.forall(tps =>
            tps.isFinished))))

  def toPensionSchemeOverseasTransfers: Option[PensionSchemeOverseasTransfers] =
    if (transferPensionSavings.contains(true) && overseasTransferCharge.contains(true)) {
      Some(
        PensionSchemeOverseasTransfers(
          overseasSchemeProvider = transferPensionScheme.map(_.toOverseasSchemeProvider),
          transferCharge = overseasTransferChargeAmount.getOrElse(zero),
          transferChargeTaxPaid = pensionSchemeTransferChargeAmount.getOrElse(zero)
        ))
    } else None
}

object TransfersIntoOverseasPensionsAnswers {
  implicit val format: OFormat[TransfersIntoOverseasPensionsAnswers] = Json.format[TransfersIntoOverseasPensionsAnswers]

  def mkAnswers(maybeDownstreamAnswers: Option[GetPensionChargesRequestModel],
                maybeDbAnswers: Option[TransfersIntoOverseasPensionsStorageAnswers]): Option[TransfersIntoOverseasPensionsAnswers] =
    maybeDbAnswers
      .getOrElse(TransfersIntoOverseasPensionsStorageAnswers.empty)
      .toTransfersIntoOverseasPensions(maybeDownstreamAnswers)
}
