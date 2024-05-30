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

import models.charges.{OverseasPensionContributions, OverseasSchemeProvider}
import play.api.libs.json.{Json, OFormat}

final case class ShortServiceRefundsAnswers(shortServiceRefund: Option[Boolean] = None,
                                            shortServiceRefundAmount: Option[BigDecimal] = None,
                                            shortServiceRefundTaxPaid: Option[Boolean] = None,
                                            shortServiceRefundTaxPaidAmount: Option[BigDecimal] = None,
                                            overseasSchemeProvider: Option[Seq[OverseasSchemeProvider]] = None) {
  def toOverseasPensions: OverseasPensionContributions = OverseasPensionContributions(
    overseasSchemeProvider = this.overseasSchemeProvider.getOrElse(Seq[OverseasSchemeProvider]()),
    shortServiceRefund = this.shortServiceRefundAmount.getOrElse(BigDecimal(0)),
    shortServiceRefundTaxPaid = this.shortServiceRefundTaxPaidAmount.getOrElse(BigDecimal(0))
  )
}

object ShortServiceRefundsAnswers {
  implicit val format: OFormat[ShortServiceRefundsAnswers] = Json.format[ShortServiceRefundsAnswers]
}
