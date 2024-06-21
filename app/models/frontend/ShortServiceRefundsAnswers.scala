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

package models.frontend

import models.charges.{GetPensionChargesRequestModel, OverseasPensionContributions, OverseasRefundPensionScheme}
import models.database.ShortServiceRefundsStorageAnswers
import models.domain.PensionAnswers
import play.api.libs.json.{Json, OFormat}

final case class ShortServiceRefundsAnswers(shortServiceRefund: Option[Boolean] = None,
                                            shortServiceRefundCharge: Option[BigDecimal] = None,
                                            shortServiceRefundTaxPaid: Option[Boolean] = None,
                                            shortServiceRefundTaxPaidCharge: Option[BigDecimal] = None,
                                            refundPensionScheme: Seq[OverseasRefundPensionScheme] = Nil)
    extends PensionAnswers {
  def isFinished: Boolean =
    shortServiceRefund.exists { bool =>
      if (bool)
        shortServiceRefundCharge.isDefined &&
        shortServiceRefundTaxPaid.exists(bool => if (bool) shortServiceRefundTaxPaidCharge.isDefined else true) &&
        refundPensionScheme.nonEmpty &&
        refundPensionScheme.forall(_.isFinished)
      else true
    }

  def toOverseasPensions: OverseasPensionContributions = OverseasPensionContributions(
    overseasSchemeProvider = this.refundPensionScheme.map(_.toOverseasSchemeProvider),
    shortServiceRefund = this.shortServiceRefundCharge.getOrElse(BigDecimal(0)),
    shortServiceRefundTaxPaid = this.shortServiceRefundTaxPaidCharge.getOrElse(BigDecimal(0))
  )
}

object ShortServiceRefundsAnswers {
  implicit val format: OFormat[ShortServiceRefundsAnswers] = Json.format[ShortServiceRefundsAnswers]

  def mkAnswers(maybeDownstreamAnswers: Option[GetPensionChargesRequestModel],
                maybeDbAnswers: Option[ShortServiceRefundsStorageAnswers]): Option[ShortServiceRefundsAnswers] =
    maybeDbAnswers
      .getOrElse(ShortServiceRefundsStorageAnswers.empty)
      .toShortServiceRefundsAnswers(maybeDownstreamAnswers)
}
