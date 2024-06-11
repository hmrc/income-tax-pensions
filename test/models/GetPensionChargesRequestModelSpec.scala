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

import models.charges._
import play.api.libs.json.{JsValue, Json}
import utils.TestUtils

class GetPensionChargesRequestModelSpec extends TestUtils {

  val minmalModel: GetPensionChargesRequestModel =
    GetPensionChargesRequestModel("2020-07-27T17:00:19Z", None, None, None, None)

  val minimalJson: JsValue = Json.parse("""{"submittedOn": "2020-07-27T17:00:19Z"}""")

  val fullPensionChargesJson: JsValue = Json.parse("""{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSchemeOverseasTransfers": {
      |		"overseasSchemeProvider": [{
      |			"providerName": "overseas providerName 1 qualifying scheme",
      |			"providerAddress": "overseas address 1",
      |			"providerCountryCode": "ESP",
      |			"qualifyingRecognisedOverseasPensionScheme": ["Q100000", "Q100002"]
      |		}, {
      |			"providerName": "overseas providerName 2 qualifying scheme",
      |			"providerAddress": "overseas address 2",
      |			"providerCountryCode": "ESP",
      |			"qualifyingRecognisedOverseasPensionScheme": ["Q100000", "Q100002"]
      |		}],
      |		"transferCharge": 22.77,
      |		"transferChargeTaxPaid": 33.88
      |	},
      |	"pensionSchemeUnauthorisedPayments": {
      |		"pensionSchemeTaxReference": ["00123456RA", "00123456RB"],
      |		"surcharge": {
      |			"amount": 124.44,
      |			"foreignTaxPaid": 123.33
      |		},
      |		"noSurcharge": {
      |			"amount": 222.44,
      |			"foreignTaxPaid": 223.33
      |		}
      |	},
      |	"pensionContributions": {
      |		"pensionSchemeTaxReference": ["00123456RA", "00123456RB"],
      |		"inExcessOfTheAnnualAllowance": 150.67,
      |		"annualAllowanceTaxPaid": 178.65,
      |   "isAnnualAllowanceReduced":false,
      |   "taperedAnnualAllowance":false,
      |   "moneyPurchasedAllowance":false
      |	},
      |	"overseasPensionContributions": {
      |		"overseasSchemeProvider": [{
      |			"providerName": "overseas providerName 1 tax ref",
      |			"providerAddress": "overseas address 1",
      |			"providerCountryCode": "ESP",
      |			"pensionSchemeTaxReference": ["00123456RA", "00123456RB"]
      |		}, {
      |			"providerName": "overseas providerName 1 tax ref",
      |			"providerAddress": "overseas address 1",
      |			"providerCountryCode": "ESP",
      |			"pensionSchemeTaxReference": ["00123456RA", "00123456RB"]
      |		}],
      |		"shortServiceRefund": 1.11,
      |		"shortServiceRefundTaxPaid": 2.22
      |	}
      |}""".stripMargin)

  val fullJsonWithQualifyingAndTaxRefsSwapped: JsValue = Json.parse("""{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSchemeOverseasTransfers": {
      |		"overseasSchemeProvider": [{
      |			"providerName": "overseas providerName 1 tax ref",
      |			"providerAddress": "overseas address 1",
      |			"providerCountryCode": "ESP",
      |			"pensionSchemeTaxReference": ["00123456RA", "00123456RB"]
      |		}, {
      |			"providerName": "overseas providerName 2 tax ref",
      |			"providerAddress": "overseas address 2",
      |			"providerCountryCode": "ESP",
      |			"pensionSchemeTaxReference": ["00123456RA", "00123456RB"]
      |		}],
      |		"transferCharge": 22.77,
      |		"transferChargeTaxPaid": 33.88
      |	},
      |	"pensionSchemeUnauthorisedPayments": {
      |		"pensionSchemeTaxReference": ["00123456RA", "00123456RB"],
      |		"surcharge": {
      |			"amount": 124.44,
      |			"foreignTaxPaid": 123.33
      |		},
      |		"noSurcharge": {
      |			"amount": 222.44,
      |			"foreignTaxPaid": 223.33
      |		}
      |	},
      |	"pensionContributions": {
      |		"pensionSchemeTaxReference": ["00123456RA", "00123456RB"],
      |		"inExcessOfTheAnnualAllowance": 150.67,
      |		"annualAllowanceTaxPaid": 178.65,
      |   "isAnnualAllowanceReduced":false,
      |   "taperedAnnualAllowance":false,
      |   "moneyPurchasedAllowance":false
      |	},
      |	"overseasPensionContributions": {
      |		"overseasSchemeProvider": [{
      |			"providerName": "overseas providerName 1 qualifying scheme",
      |			"providerAddress": "overseas address 1",
      |			"providerCountryCode": "ESP",
      |			"qualifyingRecognisedOverseasPensionScheme": ["Q100000", "Q100002"]
      |		}, {
      |			"providerName": "overseas providerName 2 qualifying scheme",
      |			"providerAddress": "overseas address 2",
      |			"providerCountryCode": "ESP",
      |			"qualifyingRecognisedOverseasPensionScheme": ["Q100000", "Q100002"]
      |		}],
      |		"shortServiceRefund": 1.11,
      |		"shortServiceRefundTaxPaid": 2.22
      |	}
      |}""".stripMargin)

  val pensionSchemeOverseasTransfersOnlyJson: JsValue = Json.parse("""{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSchemeOverseasTransfers": {
      |		"overseasSchemeProvider": [{
      |			"providerName": "overseas providerName 1 tax ref",
      |			"providerAddress": "overseas address 1",
      |			"providerCountryCode": "ESP",
      |			"pensionSchemeTaxReference": ["00123456RA", "00123456RB"]
      |		}, {
      |			"providerName": "overseas providerName 2 tax ref",
      |			"providerAddress": "overseas address 2",
      |			"providerCountryCode": "ESP",
      |			"pensionSchemeTaxReference": ["00123456RA", "00123456RB"]
      |		}],
      |		"transferCharge": 22.77,
      |		"transferChargeTaxPaid": 33.88
      |	}
      |}""".stripMargin)

  val pensionSchemeUnauthorisedPaymentsOnlyJson: JsValue = Json.parse("""{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSchemeUnauthorisedPayments": {
      |		"pensionSchemeTaxReference": ["00123456RA", "00123456RB"],
      |		"surcharge": {
      |			"amount": 124.44,
      |			"foreignTaxPaid": 123.33
      |		},
      |		"noSurcharge": {
      |			"amount": 222.44,
      |			"foreignTaxPaid": 223.33
      |		}
      |	}
      |}""".stripMargin)

  val pensionContributionsOnlyJson: JsValue = Json.parse("""{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionContributions": {
      |		"pensionSchemeTaxReference": ["00123456RA", "00123456RB"],
      |		"inExcessOfTheAnnualAllowance": 150.67,
      |		"annualAllowanceTaxPaid": 178.65,
      |   "isAnnualAllowanceReduced":false,
      |   "taperedAnnualAllowance":false,
      |   "moneyPurchasedAllowance":false
      |	}
      |}""".stripMargin)

  val overseasPensionContributionsOnlyJson: JsValue = Json.parse("""{
      |	"submittedOn": "2020-07-27T17:00:19Z",
      |	"overseasPensionContributions": {
      |		"overseasSchemeProvider": [{
      |			"providerName": "overseas providerName 1 qualifying scheme",
      |			"providerAddress": "overseas address 1",
      |			"providerCountryCode": "ESP",
      |			"qualifyingRecognisedOverseasPensionScheme": ["Q100000", "Q100002"]
      |		}, {
      |			"providerName": "overseas providerName 2 qualifying scheme",
      |			"providerAddress": "overseas address 2",
      |			"providerCountryCode": "ESP",
      |			"qualifyingRecognisedOverseasPensionScheme": ["Q100000", "Q100002"]
      |		}],
      |		"shortServiceRefund": 1.11,
      |		"shortServiceRefundTaxPaid": 2.22
      |	}
      |}""".stripMargin)

  val taxPaid: BigDecimal                      = 200.02
  val amount: BigDecimal                       = 800.02
  val transferCharge: BigDecimal               = 22.77
  val transferChargeTaxPaid: BigDecimal        = 33.88
  val surcharge: Charge                        = Charge(124.44, 123.33)
  val noSurcharge: Charge                      = Charge(222.44, 223.33)
  val inExcessOfTheAnnualAllowance: BigDecimal = 150.67
  val annualAllowanceTaxPaid: BigDecimal       = 178.65
  val shortServiceRefund: BigDecimal           = 1.11
  val shortServiceRefundTaxPaid: BigDecimal    = 2.22

  val pensionSchemeTaxRef: Option[List[String]]                      = Some(List("00123456RA", "00123456RB"))
  val qualifyingRecognisedOverseasPensionScheme: Option[Seq[String]] = Some(Seq("Q100000", "Q100002"))

  val pensionContributions: PensionContributions = PensionContributions(
    pensionSchemeTaxRef.get,
    inExcessOfTheAnnualAllowance,
    annualAllowanceTaxPaid,
    isAnnualAllowanceReduced = Some(false),
    taperedAnnualAllowance = Some(false),
    moneyPurchasedAllowance = Some(false)
  )

  val overseasSchemeProviderWithQualifyingScheme1: OverseasSchemeProvider = OverseasSchemeProvider(
    providerName = "overseas providerName 1 qualifying scheme",
    providerAddress = "overseas address 1",
    providerCountryCode = "ESP",
    qualifyingRecognisedOverseasPensionScheme,
    pensionSchemeTaxReference = None
  )
  val overseasSchemeProviderWithQualifyingScheme2: OverseasSchemeProvider = OverseasSchemeProvider(
    providerName = "overseas providerName 2 qualifying scheme",
    providerAddress = "overseas address 2",
    providerCountryCode = "ESP",
    qualifyingRecognisedOverseasPensionScheme,
    pensionSchemeTaxReference = None
  )

  val overseasSchemeProviderWithTaxRef1: OverseasSchemeProvider = OverseasSchemeProvider(
    providerName = "overseas providerName 1 tax ref",
    providerAddress = "overseas address 1",
    providerCountryCode = "ESP",
    None,
    pensionSchemeTaxReference = pensionSchemeTaxRef
  )

  val overseasSchemeProviderWithTaxRef2: OverseasSchemeProvider = OverseasSchemeProvider(
    providerName = "overseas providerName 2 tax ref",
    providerAddress = "overseas address 2",
    providerCountryCode = "ESP",
    None,
    pensionSchemeTaxReference = pensionSchemeTaxRef
  )

  val overseasPensionContributionsWithTaxRef: OverseasPensionContributions =
    OverseasPensionContributions(
      Seq(overseasSchemeProviderWithTaxRef1, overseasSchemeProviderWithTaxRef1),
      shortServiceRefund,
      shortServiceRefundTaxPaid)

  val overseasPensionContributionsWithQualifyingScheme: OverseasPensionContributions =
    OverseasPensionContributions(
      Seq(overseasSchemeProviderWithQualifyingScheme1, overseasSchemeProviderWithQualifyingScheme2),
      shortServiceRefund,
      shortServiceRefundTaxPaid)

  val pensionSchemeUnauthorisedPayments: PensionSchemeUnauthorisedPayments =
    PensionSchemeUnauthorisedPayments(pensionSchemeTaxRef, surcharge = Some(surcharge), noSurcharge = Some(noSurcharge))

  val pensionSchemeOverseasTransfersWithTaxRef: PensionSchemeOverseasTransfers = PensionSchemeOverseasTransfers(
    Seq(overseasSchemeProviderWithTaxRef1, overseasSchemeProviderWithTaxRef2),
    transferCharge,
    transferChargeTaxPaid
  )

  val pensionSchemeOverseasTransfersWithQualifyingScheme: PensionSchemeOverseasTransfers = PensionSchemeOverseasTransfers(
    Seq(overseasSchemeProviderWithQualifyingScheme1, overseasSchemeProviderWithQualifyingScheme2),
    transferCharge,
    transferChargeTaxPaid
  )

  "The GetPensionChargesRequestModel" should {

    val pensionSchemeOverseasTransfersOnlyModel =
      GetPensionChargesRequestModel("2020-07-27T17:00:19Z", Some(pensionSchemeOverseasTransfersWithTaxRef), None, None, None)

    val pensionSchemeUnauthorisedPaymentsOnlyModel =
      GetPensionChargesRequestModel("2020-07-27T17:00:19Z", None, Some(pensionSchemeUnauthorisedPayments), None, None)

    val pensionContributionsOnlyModel = GetPensionChargesRequestModel("2020-07-27T17:00:19Z", None, None, Some(pensionContributions), None)

    val overseasPensionContributionsOnlyModel =
      GetPensionChargesRequestModel("2020-07-27T17:00:19Z", None, None, None, Some(overseasPensionContributionsWithQualifyingScheme))

    val fullModelWithTaxRefsAndQualifyingSchemesSwapped: GetPensionChargesRequestModel = GetPensionChargesRequestModel(
      "2020-07-27T17:00:19Z",
      Some(pensionSchemeOverseasTransfersWithTaxRef),
      Some(pensionSchemeUnauthorisedPayments),
      Some(pensionContributions),
      Some(overseasPensionContributionsWithQualifyingScheme)
    )

    val fullGetPensionChargesRequestModel: GetPensionChargesRequestModel = GetPensionChargesRequestModel(
      "2020-07-27T17:00:19Z",
      Some(pensionSchemeOverseasTransfersWithQualifyingScheme),
      Some(pensionSchemeUnauthorisedPayments),
      Some(pensionContributions),
      Some(overseasPensionContributionsWithTaxRef)
    )

    "serialize valid values" when {
      "there is a full model" in {
        Json.toJson(fullGetPensionChargesRequestModel) mustBe fullPensionChargesJson
      }
      "there is a full model with the tax references and qualifying schemes flipped in the overseas arrays" in {
        Json.toJson(fullModelWithTaxRefsAndQualifyingSchemesSwapped) mustBe fullJsonWithQualifyingAndTaxRefsSwapped
      }
      "there is a only pension scheme overseas transfers" in {
        Json.toJson(pensionSchemeOverseasTransfersOnlyModel) mustBe pensionSchemeOverseasTransfersOnlyJson
      }
      "there is a only pension scheme unauthorised payments" in {
        Json.toJson(pensionSchemeUnauthorisedPaymentsOnlyModel) mustBe pensionSchemeUnauthorisedPaymentsOnlyJson
      }
      "there is a only pension contributions" in {
        Json.toJson(pensionContributionsOnlyModel) mustBe pensionContributionsOnlyJson
      }
      "there is a only overseas pension contributions" in {
        Json.toJson(overseasPensionContributionsOnlyModel) mustBe overseasPensionContributionsOnlyJson
      }
      "there is a minimal model" in {
        Json.toJson(minmalModel) mustBe minimalJson
      }
    }

    "deserialize valid values" when {
      "parsing full pension charges json" in {
        fullPensionChargesJson.as[GetPensionChargesRequestModel] mustBe fullGetPensionChargesRequestModel
      }
      "parsing full json with the tax references and qualifying schemes flipped in the overseas arrays" in {
        fullJsonWithQualifyingAndTaxRefsSwapped.as[GetPensionChargesRequestModel] mustBe fullModelWithTaxRefsAndQualifyingSchemesSwapped
      }
      "parsing and there are only pension scheme overseas transfers" in {
        pensionSchemeOverseasTransfersOnlyJson.as[GetPensionChargesRequestModel] mustBe pensionSchemeOverseasTransfersOnlyModel
      }
      "parsing and there are only pension Scheme unauthorised payments" in {
        pensionSchemeUnauthorisedPaymentsOnlyJson.as[GetPensionChargesRequestModel] mustBe pensionSchemeUnauthorisedPaymentsOnlyModel
      }
      "parsing and there are only pension contributions" in {
        pensionContributionsOnlyJson.as[GetPensionChargesRequestModel] mustBe pensionContributionsOnlyModel
      }
      "parsing and there are only overseas pension contributions" in {
        overseasPensionContributionsOnlyJson.as[GetPensionChargesRequestModel] mustBe overseasPensionContributionsOnlyModel
      }
      "there is a minimal model" in {
        minimalJson.as[GetPensionChargesRequestModel] mustBe minmalModel
      }
    }
  }

}
