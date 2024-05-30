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

package testdata.gens

import models.frontend._
import org.scalacheck.Gen

object JourneyAnswersGen {

  val paymentsIntoPensionsAnswersGen: Gen[PaymentsIntoPensionsAnswers] = for {
    rasPensionPaymentQuestion                 <- booleanGen
    totalRASPaymentsAndTaxRelief              <- Gen.option(bigDecimalGen)
    oneOffRasPaymentPlusTaxReliefQuestion     <- Gen.option(booleanGen)
    totalOneOffRasPaymentPlusTaxRelief        <- Gen.option(bigDecimalGen)
    pensionTaxReliefNotClaimedQuestion        <- booleanGen
    retirementAnnuityContractPaymentsQuestion <- Gen.option(booleanGen)
    totalRetirementAnnuityContractPayments    <- Gen.option(bigDecimalGen)
    workplacePensionPaymentsQuestion          <- Gen.option(booleanGen)
    totalWorkplacePensionPayments             <- Gen.option(bigDecimalGen)
  } yield PaymentsIntoPensionsAnswers(
    rasPensionPaymentQuestion,
    totalRASPaymentsAndTaxRelief,
    oneOffRasPaymentPlusTaxReliefQuestion,
    totalOneOffRasPaymentPlusTaxRelief,
    pensionTaxReliefNotClaimedQuestion,
    retirementAnnuityContractPaymentsQuestion,
    totalRetirementAnnuityContractPayments,
    workplacePensionPaymentsQuestion,
    totalWorkplacePensionPayments
  )

  val annualAllowancesAnswersGen: Gen[AnnualAllowancesAnswers] = for {
    reducedAnnualAllowanceQuestion            <- Gen.option(booleanGen)
    moneyPurchaseAnnualAllowance              <- Gen.option(booleanGen)
    taperedAnnualAllowance                    <- Gen.option(booleanGen)
    aboveAnnualAllowanceQuestion              <- Gen.option(booleanGen)
    aboveAnnualAllowance                      <- Gen.option(bigDecimalGen)
    pensionProvidePaidAnnualAllowanceQuestion <- Gen.option(booleanGen)
    taxPaidByPensionProvider                  <- Gen.option(bigDecimalGen)
    pensionSchemeTaxReferences                <- Gen.option(stringSeqGen())
  } yield AnnualAllowancesAnswers(
    reducedAnnualAllowanceQuestion,
    moneyPurchaseAnnualAllowance,
    taperedAnnualAllowance,
    aboveAnnualAllowanceQuestion,
    aboveAnnualAllowance,
    pensionProvidePaidAnnualAllowanceQuestion,
    taxPaidByPensionProvider,
    pensionSchemeTaxReferences
  )

  val unauthorisedPaymentsAnswersGen: Gen[UnauthorisedPaymentsAnswers] = for {
    surchargeQuestion            <- Gen.option(booleanGen)
    noSurchargeQuestion          <- Gen.option(booleanGen)
    surchargeAmount              <- Gen.option(bigDecimalGen)
    surchargeTaxAmountQuestion   <- Gen.option(booleanGen)
    surchargeTaxAmount           <- Gen.option(bigDecimalGen)
    noSurchargeAmount            <- Gen.option(bigDecimalGen)
    noSurchargeTaxAmountQuestion <- Gen.option(booleanGen)
    noSurchargeTaxAmount         <- Gen.option(bigDecimalGen)
    ukPensionSchemesQuestion     <- Gen.option(booleanGen)
    pensionSchemeTaxReference    <- Gen.option(stringSeqGen())
  } yield UnauthorisedPaymentsAnswers(
    surchargeQuestion,
    noSurchargeQuestion,
    surchargeAmount,
    surchargeTaxAmountQuestion,
    surchargeTaxAmount,
    noSurchargeAmount,
    noSurchargeTaxAmountQuestion,
    noSurchargeTaxAmount,
    ukPensionSchemesQuestion,
    pensionSchemeTaxReference.map(_.toList)
  )

  val overseasPensionSchemeGen: Gen[OverseasPensionScheme] = for {
    customerReference          <- Gen.option(stringGen)
    employerPaymentsAmount     <- Gen.option(bigDecimalGen)
    reliefType                 <- Gen.option(stringGen)
    alphaTwoCountryCode        <- Gen.option(stringGen)
    alphaThreeCountryCode      <- Gen.option(stringGen)
    doubleTaxationArticle      <- Gen.option(stringGen)
    doubleTaxationTreaty       <- Gen.option(stringGen)
    doubleTaxationReliefAmount <- Gen.option(bigDecimalGen)
    qopsReference              <- Gen.option(stringGen)
    sf74Reference              <- Gen.option(stringGen)
  } yield OverseasPensionScheme(
    customerReference,
    employerPaymentsAmount,
    reliefType,
    alphaTwoCountryCode,
    alphaThreeCountryCode,
    doubleTaxationArticle,
    doubleTaxationTreaty,
    doubleTaxationReliefAmount,
    qopsReference,
    sf74Reference
  )

  val paymentsIntoOverseasPensionsAnswersGen: Gen[PaymentsIntoOverseasPensionsAnswers] = for {
    paymentsIntoOverseasPensionsQuestions <- Gen.option(booleanGen)
    paymentsIntoOverseasPensionsAmount    <- Gen.option(bigDecimalGen)
    employerPaymentsQuestion              <- Gen.option(booleanGen)
    taxPaidOnEmployerPaymentsQuestion     <- Gen.option(booleanGen)
    schemes                               <- Gen.listOfN(1, overseasPensionSchemeGen)
  } yield PaymentsIntoOverseasPensionsAnswers(
    paymentsIntoOverseasPensionsQuestions,
    paymentsIntoOverseasPensionsAmount,
    employerPaymentsQuestion,
    taxPaidOnEmployerPaymentsQuestion,
    schemes
  )

  val transferPensionSchemeGen: Gen[TransferPensionScheme] = for {
    ukTransferCharge      <- Gen.option(booleanGen)
    name                  <- Gen.option(stringGen)
    schemeReference       <- Gen.option(stringGen)
    providerAddress       <- Gen.option(stringGen)
    alphaTwoCountryCode   <- Gen.option(stringGen)
    alphaThreeCountryCode <- Gen.option(stringGen)
  } yield TransferPensionScheme(
    ukTransferCharge,
    name,
    schemeReference,
    providerAddress,
    alphaTwoCountryCode,
    alphaThreeCountryCode
  )

  val transfersIntoOverseasPensionsAnswersGen: Gen[TransfersIntoOverseasPensionsAnswers] = for {
    transferPensionSavings            <- Gen.option(booleanGen)
    overseasTransferCharge            <- Gen.option(booleanGen)
    overseasTransferChargeAmount      <- Gen.option(bigDecimalGen)
    pensionSchemeTransferCharge       <- Gen.option(booleanGen)
    pensionSchemeTransferChargeAmount <- Gen.option(bigDecimalGen)
    transferPensionScheme             <- Gen.listOfN(1, transferPensionSchemeGen)
  } yield TransfersIntoOverseasPensionsAnswers(
    transferPensionSavings,
    overseasTransferCharge,
    overseasTransferChargeAmount,
    pensionSchemeTransferCharge,
    pensionSchemeTransferChargeAmount,
    transferPensionScheme
  )

  val pensionSchemeGen: Gen[PensionScheme] = for {
    alphaThreeCode                 <- Gen.option(stringGen)
    alphaTwoCode                   <- Gen.option(stringGen)
    pensionPaymentAmount           <- Gen.option(bigDecimalGen)
    pensionPaymentTaxPaid          <- Gen.option(bigDecimalGen)
    specialWithholdingTaxQuestion  <- Gen.option(booleanGen)
    specialWithholdingTaxAmount    <- Gen.option(bigDecimalGen)
    foreignTaxCreditReliefQuestion <- Gen.option(booleanGen)
    taxableAmount                  <- Gen.option(bigDecimalGen)
  } yield PensionScheme(
    alphaThreeCode,
    alphaTwoCode,
    pensionPaymentAmount,
    pensionPaymentTaxPaid,
    specialWithholdingTaxQuestion,
    specialWithholdingTaxAmount,
    foreignTaxCreditReliefQuestion,
    taxableAmount
  )

  val incomeFromOverseasPensionsAnswersGen: Gen[IncomeFromOverseasPensionsAnswers] = for {
    paymentsFromOverseasPensionsQuestion <- Gen.option(booleanGen)
    overseasIncomePensionSchemes         <- Gen.listOfN(1, pensionSchemeGen)
  } yield IncomeFromOverseasPensionsAnswers(
    paymentsFromOverseasPensionsQuestion,
    overseasIncomePensionSchemes
  )

}
