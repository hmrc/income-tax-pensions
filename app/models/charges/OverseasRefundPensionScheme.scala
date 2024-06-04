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

package models.charges

import play.api.libs.json.{Json, OFormat}

case class OverseasRefundPensionScheme(
    name: Option[String] = None,
    qualifyingRecognisedOverseasPensionScheme: Option[String] = None,
    providerAddress: Option[String] = None,
    alphaTwoCountryCode: Option[String] = None,
    alphaThreeCountryCode: Option[String] = None
) {

  def toOverseasSchemeProvider: OverseasSchemeProvider = OverseasSchemeProvider(
    providerName = name.getOrElse(""),
    providerAddress = providerAddress.getOrElse(""),
    providerCountryCode = alphaThreeCountryCode.getOrElse(""),
    qualifyingRecognisedOverseasPensionScheme = qualifyingRecognisedOverseasPensionScheme.map(qops => Seq(addQopsSubmissionPrefix(qops))),
    pensionSchemeTaxReference = None
  )

  // Why do we have alpha 2 and 3 country codes?
  def isFinished: Boolean =
    name.isDefined &&
      providerAddress.isDefined &&
      qualifyingRecognisedOverseasPensionScheme.isDefined &&
      alphaTwoCountryCode.isDefined &&
      alphaThreeCountryCode.isDefined

}

object OverseasRefundPensionScheme {
  implicit val format: OFormat[OverseasRefundPensionScheme] = Json.format[OverseasRefundPensionScheme]
}
