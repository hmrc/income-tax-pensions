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

package models.charges

import models.database.ShortServiceRefundsStorageAnswers
import models.frontend.ShortServiceRefundsAnswers
import play.api.libs.json.{Json, OFormat}

case class OverseasPensionContributions(overseasSchemeProvider: Seq[OverseasSchemeProvider],
                                        shortServiceRefund: BigDecimal,
                                        shortServiceRefundTaxPaid: BigDecimal) {
  def nonEmpty: Boolean = overseasSchemeProvider.nonEmpty

  def toShortServiceRefundsAnswers(maybeDbAnswers: Option[ShortServiceRefundsStorageAnswers]): Option[ShortServiceRefundsAnswers] =
    maybeDbAnswers map { dbAnswers =>
      val sSRGateway: Boolean    = shortServiceRefund != 0
      val sSRTaxGateway: Boolean = shortServiceRefund != 0

      ShortServiceRefundsAnswers(
        shortServiceRefund = if (sSRGateway || sSRTaxGateway) Some(true) else dbAnswers.shortServiceRefunds,
        shortServiceRefundAmount = if (sSRGateway) Some(shortServiceRefund) else None,
        shortServiceRefundTaxPaid = if (sSRTaxGateway) Some(true) else dbAnswers.nonUKTaxOnShortServiceRefunds,
        shortServiceRefundTaxPaidAmount = if (sSRTaxGateway) Some(shortServiceRefundTaxPaid) else None,
        overseasSchemeProvider = Some(overseasSchemeProvider)
      )
    }
}

object OverseasPensionContributions {
  implicit val format: OFormat[OverseasPensionContributions] = Json.format[OverseasPensionContributions]

  def empty: OverseasPensionContributions = OverseasPensionContributions(Nil, 0.0, 0.0)
}
