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

import play.api.libs.json.{Json, OFormat}

case class PensionSchemeOverseasTransfers(
    overseasSchemeProvider: Seq[OverseasSchemeProvider], // TODO journey does not have compulsory schemes but these are compulsory to API
    transferCharge: BigDecimal,
    transferChargeTaxPaid: BigDecimal) {

  def isEmpty: Boolean = overseasSchemeProvider.isEmpty && transferCharge != 0 && transferChargeTaxPaid != 0

  def nonEmpty: Boolean = !isEmpty
}

object PensionSchemeOverseasTransfers {
  implicit val format: OFormat[PensionSchemeOverseasTransfers] = Json.format[PensionSchemeOverseasTransfers]

  def empty: PensionSchemeOverseasTransfers = PensionSchemeOverseasTransfers(Nil, 0.0, 0.0)
}
