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

import cats.implicits.catsSyntaxOptionId
import models.common.Country
import models.database.{AnnualAllowancesStorageAnswers, TransfersIntoOverseasPensionsStorageAnswers}
import models.frontend.{AnnualAllowancesAnswers, TransferPensionScheme, TransfersIntoOverseasPensionsAnswers}
import play.api.libs.json.{Json, OFormat}

case class Charge(amount: BigDecimal, foreignTaxPaid: BigDecimal)

object Charge {
  implicit val format: OFormat[Charge] = Json.format[Charge]
}

case class PensionSchemeOverseasTransfers(overseasSchemeProvider: Seq[OverseasSchemeProvider],
                                          transferCharge: BigDecimal,
                                          transferChargeTaxPaid: BigDecimal) {
  def isEmpty: Boolean = this.overseasSchemeProvider.isEmpty && transferCharge != 0 && transferChargeTaxPaid != 0

  def toTransfersIntoOverseasPensions(
      maybeDbAnswers: Option[TransfersIntoOverseasPensionsStorageAnswers]): Option[TransfersIntoOverseasPensionsAnswers] =
    maybeDbAnswers.map { dbAnswers =>
      val transferChargeGateway: Boolean    = transferCharge != 0
      val transferChargeTaxGateway: Boolean = transferChargeTaxPaid != 0
      TransfersIntoOverseasPensionsAnswers(
        transferPensionSavings = dbAnswers.transferPensionSavings,
        overseasTransferCharge = if (transferChargeGateway) true.some else dbAnswers.overseasTransferCharge,
        overseasTransferChargeAmount = if (transferChargeGateway) transferCharge.some else None,
        pensionSchemeTransferCharge = if (transferChargeTaxGateway) true.some else dbAnswers.pensionSchemeTransferCharge,
        pensionSchemeTransferChargeAmount = if (transferChargeTaxGateway) transferChargeTaxPaid.some else None,
        transferPensionScheme = overseasSchemeProvider.map(_.toTransferPensionScheme)
      )
    }
}

object PensionSchemeOverseasTransfers {
  implicit val format: OFormat[PensionSchemeOverseasTransfers] = Json.format[PensionSchemeOverseasTransfers]
}

case class PensionContributions(pensionSchemeTaxReference: Seq[String],
                                inExcessOfTheAnnualAllowance: BigDecimal,
                                annualAllowanceTaxPaid: BigDecimal,
                                isAnnualAllowanceReduced: Option[Boolean],
                                taperedAnnualAllowance: Option[Boolean],
                                moneyPurchasedAllowance: Option[Boolean]) {
  // noinspection ScalaStyle
  def toAnnualAllowances(maybeDbAnswers: Option[AnnualAllowancesStorageAnswers]): Option[AnnualAllowancesAnswers] =
    maybeDbAnswers.map { dbAnswers =>
      val aboveAllowanceGateway: Boolean = inExcessOfTheAnnualAllowance != 0
      val taxPaidGateway: Boolean        = annualAllowanceTaxPaid != 0
      AnnualAllowancesAnswers(
        reducedAnnualAllowanceQuestion = isAnnualAllowanceReduced,
        moneyPurchaseAnnualAllowance = moneyPurchasedAllowance,
        taperedAnnualAllowance = taperedAnnualAllowance,
        aboveAnnualAllowanceQuestion = if (aboveAllowanceGateway) true.some else dbAnswers.aboveAnnualAllowanceQuestion,
        aboveAnnualAllowance = if (aboveAllowanceGateway) inExcessOfTheAnnualAllowance.some else None,
        pensionProvidePaidAnnualAllowanceQuestion = if (taxPaidGateway) true.some else dbAnswers.pensionProvidePaidAnnualAllowanceQuestion,
        taxPaidByPensionProvider = if (taxPaidGateway) annualAllowanceTaxPaid.some else None,
        pensionSchemeTaxReferences = if (pensionSchemeTaxReference.isEmpty) None else pensionSchemeTaxReference.some
      )
    }
}

object PensionContributions {
  implicit val format: OFormat[PensionContributions] = Json.format[PensionContributions]
}

case class OverseasSchemeProvider(providerName: String,
                                  providerAddress: String,
                                  providerCountryCode: String,
                                  qualifyingRecognisedOverseasPensionScheme: Option[Seq[String]],
                                  pensionSchemeTaxReference: Option[Seq[String]]) {
  def toTransferPensionScheme: TransferPensionScheme = TransferPensionScheme(
    ukTransferCharge = Some(providerCountryCode == "GBR"),
    name = Some(providerName),
    pstr = pensionSchemeTaxReference.map(_.head).map(_.replace("Q", "")),
    qops = qualifyingRecognisedOverseasPensionScheme.map(_.head),
    providerAddress = Some(providerAddress),
    alphaTwoCountryCode = Country.get2AlphaCodeFrom3AlphaCode(Some(providerCountryCode)),
    alphaThreeCountryCode = Some(providerCountryCode)
  )
}

object OverseasSchemeProvider {
  implicit val format: OFormat[OverseasSchemeProvider] = Json.format[OverseasSchemeProvider]
}

case class LifetimeAllowance(amount: BigDecimal, taxPaid: BigDecimal)

case class OverseasPensionContributions(overseasSchemeProvider: Seq[OverseasSchemeProvider],
                                        shortServiceRefund: BigDecimal,
                                        shortServiceRefundTaxPaid: BigDecimal)

object OverseasPensionContributions {
  implicit val format: OFormat[OverseasPensionContributions] = Json.format[OverseasPensionContributions]
}

object LifetimeAllowance {
  implicit val format: OFormat[LifetimeAllowance] = Json.format[LifetimeAllowance]
}

case class PensionSavingsTaxCharges(pensionSchemeTaxReference: Option[Seq[String]],
                                    lumpSumBenefitTakenInExcessOfLifetimeAllowance: Option[LifetimeAllowance],
                                    benefitInExcessOfLifetimeAllowance: Option[LifetimeAllowance])
object PensionSavingsTaxCharges {
  implicit val format: OFormat[PensionSavingsTaxCharges] = Json.format[PensionSavingsTaxCharges]
}

case class PensionSchemeUnauthorisedPayments(pensionSchemeTaxReference: Option[Seq[String]], surcharge: Option[Charge], noSurcharge: Option[Charge])

object PensionSchemeUnauthorisedPayments {
  implicit val format: OFormat[PensionSchemeUnauthorisedPayments] = Json.format[PensionSchemeUnauthorisedPayments]
}

case class GetPensionChargesRequestModel(submittedOn: String,
                                         pensionSavingsTaxCharges: Option[PensionSavingsTaxCharges],
                                         pensionSchemeOverseasTransfers: Option[PensionSchemeOverseasTransfers],
                                         pensionSchemeUnauthorisedPayments: Option[PensionSchemeUnauthorisedPayments],
                                         pensionContributions: Option[PensionContributions],
                                         overseasPensionContributions: Option[OverseasPensionContributions]) {

  def toCreateUpdatePensionChargesRequestModel: CreateUpdatePensionChargesRequestModel = CreateUpdatePensionChargesRequestModel(
    pensionSavingsTaxCharges,
    pensionSchemeOverseasTransfers,
    pensionSchemeUnauthorisedPayments,
    pensionContributions,
    overseasPensionContributions)
}

object GetPensionChargesRequestModel {
  implicit val format: OFormat[GetPensionChargesRequestModel] = Json.format[GetPensionChargesRequestModel]
}
