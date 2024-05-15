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

import models.database.TransfersIntoOverseasPensionsStorageAnswers
import models.frontend.{TransferPensionScheme, TransfersIntoOverseasPensionsAnswers}
import models.{OverseasSchemeProvider, PensionSchemeOverseasTransfers}

object transfersIntoOverseasPensions {
  def transfersIntoOverseasPensionsAnswers: TransfersIntoOverseasPensionsAnswers =
    TransfersIntoOverseasPensionsAnswers(
      Some(true),
      Some(true),
      Some(1.0),
      Some(true),
      Some(2.0),
      Seq(transferPensionSchemeUK, transferPensionSchemeNonUK))
  val annualAllowancesEmptyAnswers: TransfersIntoOverseasPensionsAnswers =
    TransfersIntoOverseasPensionsAnswers(None, None, None, None, None, Seq.empty)
  val annualAllowancesAnswersNoJourney: TransfersIntoOverseasPensionsAnswers =
    TransfersIntoOverseasPensionsAnswers(Some(false), None, None, None, None, Seq.empty)

  val transferPensionSchemeUK: TransferPensionScheme =
    TransferPensionScheme(Some(true), Some("UK Scheme"), Some("PSTR"), Some("QOPS"), Some("Address"), Some("GBR"), Some("GB"))
  val transferPensionSchemeNonUK: TransferPensionScheme =
    TransferPensionScheme(Some(false), Some("Non-UK Scheme"), Some("PSTR"), Some("QOPS"), Some("Address"), Some("FRA"), Some("FR"))

  def pensionSchemeOverseasTransfers: PensionSchemeOverseasTransfers =
    PensionSchemeOverseasTransfers(Seq(overseasSchemeProvider), BigDecimal(1), BigDecimal(2))

  val overseasSchemeProvider: OverseasSchemeProvider = OverseasSchemeProvider("Name", "Address", "GBR", Some(Seq("QOPS")), Some(Seq("PSTR")))

  def transfersIntoOverseasPensionsStorageAnswers: TransfersIntoOverseasPensionsStorageAnswers =
    TransfersIntoOverseasPensionsStorageAnswers(Some(true), Some(true), Some(true))
}