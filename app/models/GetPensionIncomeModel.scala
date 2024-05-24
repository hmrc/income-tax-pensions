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

package models

import cats.implicits.catsSyntaxOptionId
import models.common.Country
import models.database.IncomeFromOverseasPensionsStorageAnswers
import models.frontend.OverseasPensionScheme.{DoubleTaxationRelief, MigrantMemberRelief, NoTaxRelief, TransitionalCorrespondingRelief}
import models.frontend.{IncomeFromOverseasPensionsAnswers, OverseasPensionScheme, PensionScheme}
import play.api.libs.json.{Json, OFormat}

case class ForeignPension(countryCode: String,
                          taxableAmount: BigDecimal,
                          amountBeforeTax: Option[BigDecimal],
                          taxTakenOff: Option[BigDecimal],
                          specialWithholdingTax: Option[BigDecimal],
                          foreignTaxCreditRelief: Option[Boolean]) {
  def toPensionScheme: PensionScheme = PensionScheme(
    alphaThreeCode = Some(countryCode),
    alphaTwoCode = Country.get2AlphaCodeFrom3AlphaCode(Some(countryCode)),
    pensionPaymentAmount = amountBeforeTax,
    pensionPaymentTaxPaid = taxTakenOff,
    specialWithholdingTaxQuestion = specialWithholdingTax.fold(Some(false))(_ => Some(true)),
    specialWithholdingTaxAmount = specialWithholdingTax,
    foreignTaxCreditReliefQuestion = foreignTaxCreditRelief,
    taxableAmount = Some(taxableAmount)
  )
}

object ForeignPension {
  implicit val format: OFormat[ForeignPension] = Json.format[ForeignPension]
}

case class OverseasPensionContribution(customerReference: Option[String],
                                       exemptEmployersPensionContribs: BigDecimal,
                                       migrantMemReliefQopsRefNo: Option[String],
                                       dblTaxationRelief: Option[BigDecimal],
                                       dblTaxationCountry: Option[String],
                                       dblTaxationArticle: Option[String],
                                       dblTaxationTreaty: Option[String],
                                       sf74Reference: Option[String]) {

  def toOverseasPensionScheme: OverseasPensionScheme = OverseasPensionScheme(
    customerReference = customerReference,
    employerPaymentsAmount = exemptEmployersPensionContribs.some,
    reliefType = getReliefType.some,
    alphaTwoCountryCode = Country.get2AlphaCodeFrom3AlphaCode(dblTaxationCountry),
    alphaThreeCountryCode = dblTaxationCountry,
    doubleTaxationArticle = dblTaxationArticle,
    doubleTaxationTreaty = dblTaxationTreaty,
    doubleTaxationReliefAmount = dblTaxationRelief,
    qopsReference = migrantMemReliefQopsRefNo,
    sf74Reference = sf74Reference
  )

  private def getReliefType: String =
    (sf74Reference, dblTaxationRelief, migrantMemReliefQopsRefNo) match {
      case (Some(_), _, _) => TransitionalCorrespondingRelief
      case (_, Some(_), _) => DoubleTaxationRelief
      case (_, _, Some(_)) => MigrantMemberRelief
      case _               => NoTaxRelief
    }
}

object OverseasPensionContribution {
  implicit val format: OFormat[OverseasPensionContribution] = Json.format[OverseasPensionContribution]
}

case class GetPensionIncomeModel(
    submittedOn: String,
    deletedOn: Option[String],
    foreignPension: Option[Seq[ForeignPension]],
    overseasPensionContribution: Option[Seq[OverseasPensionContribution]]
) {
  def toCreateUpdatePensionIncomeModel: CreateUpdatePensionIncomeModel = CreateUpdatePensionIncomeModel(foreignPension, overseasPensionContribution)

  def toIncomeFromOverseasPensions(maybeDbAnswers: Option[IncomeFromOverseasPensionsStorageAnswers]): Option[IncomeFromOverseasPensionsAnswers] =
    maybeDbAnswers.map { dbAnswers =>
      IncomeFromOverseasPensionsAnswers(
        dbAnswers.paymentsFromOverseasPensionsQuestion,
        foreignPension.getOrElse(Seq.empty).map(_.toPensionScheme)
      )
    }
}
object GetPensionIncomeModel {
  implicit val format: OFormat[GetPensionIncomeModel] = Json.format[GetPensionIncomeModel]

  def empty: GetPensionIncomeModel = GetPensionIncomeModel("", None, None, None)
}
