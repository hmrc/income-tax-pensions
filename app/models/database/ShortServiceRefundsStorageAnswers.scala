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
import models.frontend.ShortServiceRefundsAnswers
import play.api.libs.json.{Json, OFormat}

final case class ShortServiceRefundsStorageAnswers(shortServiceRefund: Option[Boolean] = None, nonUKTaxOnShortServiceRefund: Option[Boolean] = None) {

  def isEmpty: Boolean = shortServiceRefund.isEmpty && nonUKTaxOnShortServiceRefund.isEmpty

  def toShortServiceRefundsAnswers(maybeCharges: Option[GetPensionChargesRequestModel]): Option[ShortServiceRefundsAnswers] = {
    val oPC: Option[OverseasPensionContributions] = maybeCharges.flatMap(_.overseasPensionContributions)
    val nonEmptyOPCAnswers: Boolean               = oPC.getOrElse(OverseasPensionContributions.empty).isEmpty

    val sSRGateway: Boolean    = oPC.map(_.shortServiceRefund).exists(_ != 0)
    val sSRTaxGateway: Boolean = oPC.map(_.shortServiceRefundTaxPaid).exists(_ != 0)

    val schemes: Seq[OverseasRefundPensionScheme] = oPC.map(_.overseasSchemeProvider.map(_.toOverseasRefundPensionScheme)).getOrElse(Seq())

    if (nonEmptyOPCAnswers && isEmpty) None
    else
      Some(
        ShortServiceRefundsAnswers(
          shortServiceRefund = if (sSRGateway) Some(true) else shortServiceRefund,
          shortServiceRefundCharge = if (sSRGateway) oPC.map(_.shortServiceRefund) else None,
          shortServiceRefundTaxPaid = if (sSRTaxGateway) Some(true) else nonUKTaxOnShortServiceRefund,
          shortServiceRefundTaxPaidCharge = if (sSRTaxGateway) oPC.map(_.shortServiceRefundTaxPaid) else None,
          refundPensionScheme = if (oPC.nonEmpty) schemes else Seq[OverseasRefundPensionScheme]()
        ))
  }
}

object ShortServiceRefundsStorageAnswers {
  implicit val format: OFormat[ShortServiceRefundsStorageAnswers] = Json.format[ShortServiceRefundsStorageAnswers]

  def fromJourneyAnswers(answers: ShortServiceRefundsAnswers): ShortServiceRefundsStorageAnswers =
    ShortServiceRefundsStorageAnswers(
      answers.shortServiceRefund,
      answers.shortServiceRefundTaxPaid
    )
}
