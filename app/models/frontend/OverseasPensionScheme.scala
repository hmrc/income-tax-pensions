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

import play.api.libs.json.{Json, OFormat}

case class OverseasPensionScheme(
    customerReference: Option[String] = None,
    employerPaymentsAmount: Option[BigDecimal] = None,
    reliefType: Option[String] = None,
    alphaTwoCountryCode: Option[String] = None,
    alphaThreeCountryCode: Option[String] = None,
    doubleTaxationArticle: Option[String] = None,
    doubleTaxationTreaty: Option[String] = None,
    doubleTaxationReliefAmount: Option[BigDecimal] = None,
    qopsReference: Option[String] = None,
    sf74Reference: Option[String] = None
)

object OverseasPensionScheme {
  implicit val format: OFormat[OverseasPensionScheme] = Json.format[OverseasPensionScheme]
}
