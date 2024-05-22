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

package models

import play.api.libs.json.{Json, OFormat}

case class ForeignPension(
    countryCode: String,
    taxableAmount: BigDecimal,
    amountBeforeTax: Option[BigDecimal],
    taxTakenOff: Option[BigDecimal],
    specialWithholdingTax: Option[BigDecimal],
    foreignTaxCreditRelief: Option[Boolean]
)

object ForeignPension {
  implicit val format: OFormat[ForeignPension] = Json.format[ForeignPension]
}

case class OverseasPensionContribution(
    customerReference: Option[String],
    exemptEmployersPensionContribs: BigDecimal,
    migrantMemReliefQopsRefNo: Option[String],
    dblTaxationRelief: Option[BigDecimal],
    dblTaxationCountry: Option[String],
    dblTaxationArticle: Option[String],
    dblTaxationTreaty: Option[String],
    sf74Reference: Option[String]
)

object OverseasPensionContribution {
  implicit val format: OFormat[OverseasPensionContribution] = Json.format[OverseasPensionContribution]
}

case class GetPensionIncomeModel(
    submittedOn: String,
    deletedOn: Option[String],
    foreignPension: Option[Seq[ForeignPension]],
    overseasPensionContribution: Option[Seq[OverseasPensionContribution]]
)
object GetPensionIncomeModel {
  implicit val format: OFormat[GetPensionIncomeModel] = Json.format[GetPensionIncomeModel]

  def empty: GetPensionIncomeModel = GetPensionIncomeModel("", None, None, None)
}
