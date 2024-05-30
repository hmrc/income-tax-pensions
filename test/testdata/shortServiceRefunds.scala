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

package testdata

import models.charges.{OverseasPensionContributions, OverseasSchemeProvider}
import models.database.ShortServiceRefundsStorageAnswers
import models.frontend.ShortServiceRefundsAnswers
import utils.Constants.GBAlpha3Code

object shortServiceRefunds {

  val pstrReference = "12345678RA"

  def ukOverseasSchemeProvider: OverseasSchemeProvider =
    OverseasSchemeProvider("UK Scheme", "Address", GBAlpha3Code, None, Some(Seq(pstrReference)))

  def overseasPensionContributions: OverseasPensionContributions =
    OverseasPensionContributions(Seq(ukOverseasSchemeProvider), BigDecimal(1.0), BigDecimal(2.0))

  def shortServiceRefundsAnswers: ShortServiceRefundsAnswers =
    ShortServiceRefundsAnswers(Some(true), Some(BigDecimal(1.0)), Some(true), Some(BigDecimal(2.0)), Some(Seq(ukOverseasSchemeProvider)))

  def shortServiceRefundsCtxStorageAnswers: ShortServiceRefundsStorageAnswers =
    ShortServiceRefundsStorageAnswers(Some(true), Some(true))
}
