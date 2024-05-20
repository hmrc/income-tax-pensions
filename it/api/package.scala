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

import models._
import play.api.libs.json.{JsObject, JsValue, Json}

package object api {

  object SubmissionsTestData {}

  object PensionsReliefsTestData {
    val GetPensionReliefDesResponseBody: String =
      """
        |{
        |    "submittedOn": "2020-01-04T05:01:01Z",
        |    "deletedOn": "2020-01-04T05:01:01Z",
        |    "pensionReliefs": {
        |        "regularPensionContributions": 100.11,
        |        "oneOffPensionContributionsPaid": 200.22,
        |        "retirementAnnuityPayments": 300.33,
        |        "paymentToEmployersSchemeNoTaxRelief": 400.44,
        |        "overseasPensionSchemeContributions": 500.55
        |    }
        |}
        |""".stripMargin

    val createOrUpdatePensionReliefs: CreateOrUpdatePensionReliefsModel =
      CreateOrUpdatePensionReliefsModel(
        PensionReliefs(
          regularPensionContributions = Some(10.22),
          oneOffPensionContributionsPaid = Some(11.33),
          retirementAnnuityPayments = Some(12.44),
          paymentToEmployersSchemeNoTaxRelief = Some(13.55),
          overseasPensionSchemeContributions = Some(14.66)
        ))

    val pensionsReliefPayload: JsValue = Json.parse("""{
        |	"pensionReliefs": {
        |		"regularPensionContributions": 10.22,
        |		"oneOffPensionContributionsPaid": 11.33,
        |		"retirementAnnuityPayments": 12.44,
        |		"paymentToEmployersSchemeNoTaxRelief": 13.55,
        |		"overseasPensionSchemeContributions": 14.66
        |	}
        |}""".stripMargin)
  }
  object PensionsChargesTestData {
    val GetPensionChargesDesResponseBody: String =
      """
      {
        | "submittedOn": "2020-07-27T17:00:19Z",
        |	"pensionSavingsTaxCharges": {
        |		"pensionSchemeTaxReference": [
        |			"00123456RA"
        |		],
        |		"lumpSumBenefitTakenInExcessOfLifetimeAllowance": {
        |			"amount": 123.45,
        |			"taxPaid": 12.45
        |		},
        |		"benefitInExcessOfLifetimeAllowance": {
        |			"amount": 123.45,
        |			"taxPaid": 12.34
        |		},
        |		"isAnnualAllowanceReduced": true,
        |		"taperedAnnualAllowance": true,
        |		"moneyPurchasedAllowance": false
        |	},
        |	"pensionSchemeOverseasTransfers": {
        |		"overseasSchemeProvider": [{
        |			"providerName": "Overseas Pensions Plc",
        |			"providerAddress": "111 Some Street, Some Town, Some Place",
        |			"providerCountryCode": "ESP",
        |			"qualifyingRecognisedOverseasPensionScheme": [
        |				"Q123456"
        |			]
        |		}],
        |		"transferCharge": 123.45,
        |		"transferChargeTaxPaid": 0
        |	},
        |	"pensionSchemeUnauthorisedPayments": {
        |		"pensionSchemeTaxReference": [
        |			"00123456RA"
        |		],
        |		"surcharge": {
        |			"amount": 123.45,
        |			"foreignTaxPaid": 123.45
        |		},
        |		"noSurcharge": {
        |			"amount": 123.45,
        |			"foreignTaxPaid": 123.45
        |		}
        |	},
        |	"pensionContributions": {
        |		"pensionSchemeTaxReference": [
        |			"00123456RA"
        |		],
        |		"inExcessOfTheAnnualAllowance": 123.45,
        |		"annualAllowanceTaxPaid": 123.45
        |	},
        |	"overseasPensionContributions": {
        |		"overseasSchemeProvider": [{
        |			"providerName": "Overseas Pensions Plc",
        |			"providerAddress": "112 Some Street, Some Town, Some Place",
        |			"providerCountryCode": "ESP",
        |			"pensionSchemeTaxReference": [
        |				"00123456RA"
        |			]
        |		}],
        |		"shortServiceRefund": 123.45,
        |		"shortServiceRefundTaxPaid": 0
        |	}
        |}""".stripMargin

    val createUpdatePensionChargesRequest = CreateUpdatePensionChargesRequestModel(
      pensionSavingsTaxCharges = Some(
        PensionSavingsTaxCharges(
          pensionSchemeTaxReference = Some(Seq("00123456RA")),
          lumpSumBenefitTakenInExcessOfLifetimeAllowance = Some(LifetimeAllowance(amount = 123.45, taxPaid = 12.45)),
          benefitInExcessOfLifetimeAllowance = Some(LifetimeAllowance(amount = 123.45, taxPaid = 12.34))
        )),
      pensionSchemeOverseasTransfers = Some(
        PensionSchemeOverseasTransfers(
          overseasSchemeProvider = Seq(OverseasSchemeProvider(
            providerName = "Overseas Pensions Plc",
            providerAddress = "111 Some Street, Some Town, Some Place",
            providerCountryCode = "ESP",
            qualifyingRecognisedOverseasPensionScheme = Some(Seq("Q123456")),
            pensionSchemeTaxReference = None
          )),
          transferCharge = 123.45,
          transferChargeTaxPaid = 0
        )),
      pensionSchemeUnauthorisedPayments = Some(
        PensionSchemeUnauthorisedPayments(
          pensionSchemeTaxReference = Some(List("00123456RA")),
          surcharge = Some(
            Charge(
              amount = 123.45,
              foreignTaxPaid = 123.45
            )),
          noSurcharge = Some(
            Charge(
              amount = 123.45,
              foreignTaxPaid = 123.45
            ))
        )),
      pensionContributions = Some(
        PensionContributions(
          pensionSchemeTaxReference = Seq("00123456RA"),
          inExcessOfTheAnnualAllowance = 123.45,
          annualAllowanceTaxPaid = 123.45,
          isAnnualAllowanceReduced = Some(true),
          taperedAnnualAllowance = Some(true),
          moneyPurchasedAllowance = Some(false)
        )),
      overseasPensionContributions = Some(
        OverseasPensionContributions(
          overseasSchemeProvider = Seq(OverseasSchemeProvider(
            providerName = "Overseas Pensions Plc",
            providerAddress = "112 Some Street, Some Town, Some Place",
            providerCountryCode = "ESP",
            qualifyingRecognisedOverseasPensionScheme = None,
            pensionSchemeTaxReference = Some(Seq("00123456RA"))
          )),
          shortServiceRefund = 123.45,
          shortServiceRefundTaxPaid = 0
        ))
    )
    val createUpdatePensionChargesJsonStr: String =
      """
      {
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
      |		],
      |		"lumpSumBenefitTakenInExcessOfLifetimeAllowance": {
      |			"amount": 123.45,
      |			"taxPaid": 12.45
      |		},
      |		"benefitInExcessOfLifetimeAllowance": {
      |			"amount": 123.45,
      |			"taxPaid": 12.34
      |		}
      |	},
      |	"pensionSchemeOverseasTransfers": {
      |		"overseasSchemeProvider": [{
      |			"providerName": "Overseas Pensions Plc",
      |			"providerAddress": "111 Some Street, Some Town, Some Place",
      |			"providerCountryCode": "ESP",
      |			"qualifyingRecognisedOverseasPensionScheme": [
      |				"Q123456"
      |			]
      |		}],
      |		"transferCharge": 123.45,
      |		"transferChargeTaxPaid": 0
      |	},
      |	"pensionSchemeUnauthorisedPayments": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
      |		],
      |		"surcharge": {
      |			"amount": 123.45,
      |			"foreignTaxPaid": 123.45
      |		},
      |		"noSurcharge": {
      |			"amount": 123.45,
      |			"foreignTaxPaid": 123.45
      |		}
      |	},
      |	"pensionContributions": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
      |		],
      |		"inExcessOfTheAnnualAllowance": 123.45,
      |		"annualAllowanceTaxPaid": 123.45,
      |   "isAnnualAllowanceReduced": true,
      |		"taperedAnnualAllowance": true,
      |		"moneyPurchasedAllowance": false
      |	},
      |	"overseasPensionContributions": {
      |		"overseasSchemeProvider": [{
      |			"providerName": "Overseas Pensions Plc",
      |			"providerAddress": "112 Some Street, Some Town, Some Place",
      |			"providerCountryCode": "ESP",
      |			"pensionSchemeTaxReference": [
      |				"00123456RA"
      |			]
      |		}],
      |		"shortServiceRefund": 123.45,
      |		"shortServiceRefundTaxPaid": 0
      |	}
      |}""".stripMargin

    val createUpdatePensionChargesPayload: JsValue = Json.parse(createUpdatePensionChargesJsonStr)

    val minimumRequestPayload: JsObject =
      Json.toJsObject(
        CreateUpdatePensionChargesRequestModel(
          pensionSavingsTaxCharges = None,
          pensionContributions = Some(
            PensionContributions(
              Seq("00123456RA"),
              10.0,
              20.0,
              isAnnualAllowanceReduced = Some(true),
              taperedAnnualAllowance = Some(true),
              moneyPurchasedAllowance = Some(false))),
          pensionSchemeOverseasTransfers = None,
          pensionSchemeUnauthorisedPayments = None,
          overseasPensionContributions = None
        ))
  }
}
