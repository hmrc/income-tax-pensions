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

import cats.implicits.none
import models.charges.{OverseasSchemeProvider, addQopsSubmissionPrefix}
import play.api.libs.json.{Json, OFormat}
import utils.Constants.GBAlpha3Code

case class TransferPensionScheme(ukTransferCharge: Option[Boolean] = None,
                                 name: Option[String] = None,
                                 schemeReference: Option[String] = None,
                                 providerAddress: Option[String] = None,
                                 alphaTwoCountryCode: Option[String] = None,
                                 alphaThreeCountryCode: Option[String] = None) {

  def isFinished: Boolean =
    name.isDefined && providerAddress.isDefined && ukTransferCharge.isDefined && schemeReference.isDefined && alphaThreeCountryCode.isDefined

  def toOverseasSchemeProvider: OverseasSchemeProvider = {
    val isUkScheme = ukTransferCharge.contains(true)
    val pstr       = if (isUkScheme) schemeReference.map(Seq(_)) else none[Seq[String]]
    val qops       = if (isUkScheme) none[Seq[String]] else schemeReference.map(qops => Seq(addQopsSubmissionPrefix(qops)))
    OverseasSchemeProvider(
      providerName = name.getOrElse(""),
      providerAddress = providerAddress.getOrElse(""),
      providerCountryCode = if (isUkScheme) GBAlpha3Code else alphaThreeCountryCode.getOrElse(""),
      qualifyingRecognisedOverseasPensionScheme = qops,
      pensionSchemeTaxReference = pstr
    )
  }
}

object TransferPensionScheme {
  implicit val format: OFormat[TransferPensionScheme] = Json.format[TransferPensionScheme]
}
