/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json.{JsValue, Json}
import utils.TestUtils

class GetPensionIncomeModelSpec extends TestUtils {

  val fullPensionIncomeJson: JsValue = Json.parse(
    """
      | {
      |    "submittedOn": "2022-07-28T07:59:39.041Z",
      |    "deletedOn": "2022-07-28T07:59:39.041Z",
      |    "foreignPension": [
      |      {
      |        "countryCode": "FRA",
      |        "amountBeforeTax": 1999.99,
      |        "taxTakenOff": 1999.99,
      |        "specialWithholdingTax": 1999.99,
      |        "foreignTaxCreditRelief": false,
      |        "taxableAmount": 1999.99
      |      }
      |    ],
      |    "overseasPensionContribution": [
      |      {
      |        "customerReference": "PENSIONINCOME245",
      |        "exemptEmployersPensionContribs": 1999.99,
      |        "migrantMemReliefQopsRefNo": "QOPS000000",
      |        "dblTaxationRelief": 1999.99,
      |        "dblTaxationCountry": "FRA",
      |        "dblTaxationArticle": "AB3211-1",
      |        "dblTaxationTreaty": "Munich",
      |        "sf74Reference": "SF74-123456"
      |      }
      |    ]
      |  }
      |""".stripMargin
  )

  val minimumPensionIncomeJson: JsValue = Json.parse(
    """
      | {
      |    "submittedOn": "2022-07-28T07:59:39.041Z",
      |    "foreignPension": [
      |      {
      |        "countryCode": "FRA",
      |        "taxableAmount": 1999.99
      |      }
      |    ],
      |    "overseasPensionContribution": [
      |      {
      |        "exemptEmployersPensionContribs": 1999.99
      |      }
      |    ]
      |  }
      |""".stripMargin
  )

  val pensionIncomeJsonWithMinForeignPension: JsValue = Json.parse(
    """
      | {
      |    "submittedOn": "2022-07-28T07:59:39.041Z",
      |    "deletedOn": "2022-07-28T07:59:39.041Z",
      |    "foreignPension": [
      |      {
      |        "countryCode": "FRA",
      |        "taxableAmount": 1999.99
      |      }
      |    ],
      |    "overseasPensionContribution": [
      |     {
      |        "customerReference": "PENSIONINCOME245",
      |        "exemptEmployersPensionContribs": 1999.99,
      |        "migrantMemReliefQopsRefNo": "QOPS000000",
      |        "dblTaxationRelief": 1999.99,
      |        "dblTaxationCountry": "FRA",
      |        "dblTaxationArticle": "AB3211-1",
      |        "dblTaxationTreaty": "Munich",
      |        "sf74Reference": "SF74-123456"
      |      }
      |    ]
      |  }
      |""".stripMargin
  )

  val pensionIncomeJsonWithMinOverseasPensionContribution: JsValue = Json.parse(
    """
      | {
      |    "submittedOn": "2022-07-28T07:59:39.041Z",
      |    "deletedOn": "2022-07-28T07:59:39.041Z",
      |    "foreignPension": [
      |     {
      |        "countryCode": "FRA",
      |        "amountBeforeTax": 1999.99,
      |        "taxTakenOff": 1999.99,
      |        "specialWithholdingTax": 1999.99,
      |        "foreignTaxCreditRelief": false,
      |        "taxableAmount": 1999.99
      |      }
      |    ],
      |    "overseasPensionContribution": [
      |     {
      |        "exemptEmployersPensionContribs": 1999.99
      |      }
      |    ]
      |  }
      |""".stripMargin
  )


  "The GetPensionIncomeModel" should {

    val fullPensionIncomeModel: GetPensionIncomeModel =
      GetPensionIncomeModel(
        submittedOn = "2022-07-28T07:59:39.041Z",
        deletedOn = Some("2022-07-28T07:59:39.041Z"),
        foreignPension = Seq(
          ForeignPension(
            countryCode = "FRA",
            taxableAmount = 1999.99,
            amountBeforeTax = Some(1999.99),
            taxTakenOff = Some(1999.99),
            specialWithholdingTax = Some(1999.99),
            foreignTaxCreditRelief = Some(false)
          )
        ),
        overseasPensionContribution = Seq(
          OverseasPensionContribution(
            customerReference = Some("PENSIONINCOME245"),
            exemptEmployersPensionContribs = 1999.99,
            migrantMemReliefQopsRefNo = Some("QOPS000000"),
            dblTaxationRelief = Some(1999.99),
            dblTaxationCountry = Some("FRA"),
            dblTaxationArticle = Some("AB3211-1"),
            dblTaxationTreaty = Some("Munich"),
            sf74Reference = Some("SF74-123456")

          )
        )
      )

    val pensionIncomeWithMinForeignPensionModel: GetPensionIncomeModel = GetPensionIncomeModel(
      submittedOn = "2022-07-28T07:59:39.041Z",
      deletedOn = Some("2022-07-28T07:59:39.041Z"),
      foreignPension = Seq(
        ForeignPension(
          countryCode = "FRA",
          taxableAmount = 1999.99,
          amountBeforeTax = None,
          taxTakenOff = None,
          specialWithholdingTax = None,
          foreignTaxCreditRelief = None
        )
      ),
      overseasPensionContribution = Seq(
        OverseasPensionContribution(
          customerReference = Some("PENSIONINCOME245"),
          exemptEmployersPensionContribs = 1999.99,
          migrantMemReliefQopsRefNo = Some("QOPS000000"),
          dblTaxationRelief = Some(1999.99),
          dblTaxationCountry = Some("FRA"),
          dblTaxationArticle = Some("AB3211-1"),
          dblTaxationTreaty = Some("Munich"),
          sf74Reference = Some("SF74-123456")

        )
      )
    )

    val minimumPensionIncomeModel: GetPensionIncomeModel = GetPensionIncomeModel(
      submittedOn = "2022-07-28T07:59:39.041Z",
      deletedOn = None,
      foreignPension = Seq(
        ForeignPension(
          countryCode = "FRA",
          taxableAmount = 1999.99,
          amountBeforeTax = None,
          taxTakenOff = None,
          specialWithholdingTax = None,
          foreignTaxCreditRelief = None
        )
      ),
      overseasPensionContribution = Seq(
        OverseasPensionContribution(
          customerReference = None,
          exemptEmployersPensionContribs = 1999.99,
          migrantMemReliefQopsRefNo = None,
          dblTaxationRelief = None,
          dblTaxationCountry = None,
          dblTaxationArticle = None,
          dblTaxationTreaty = None,
          sf74Reference = None
        )
      )
    )


    val pensionIncomeWithMinOverseasPensionContributionModel =
      GetPensionIncomeModel(
        submittedOn = "2022-07-28T07:59:39.041Z",
        deletedOn = Some("2022-07-28T07:59:39.041Z"),
        foreignPension = Seq(
          ForeignPension(
            countryCode = "FRA",
            taxableAmount = 1999.99,
            amountBeforeTax = Some(1999.99),
            taxTakenOff = Some(1999.99),
            specialWithholdingTax = Some(1999.99),
            foreignTaxCreditRelief = Some(false)
          )
        ),
        overseasPensionContribution = Seq(
          OverseasPensionContribution(
            customerReference = None,
            exemptEmployersPensionContribs = 1999.99,
            migrantMemReliefQopsRefNo = None,
            dblTaxationRelief = None,
            dblTaxationCountry = None,
            dblTaxationArticle = None,
            dblTaxationTreaty = None,
            sf74Reference = None
          )
        )
      )
    "serialize valid values" when {
      "there is a full model" in {
        Json.toJson(fullPensionIncomeModel) mustBe fullPensionIncomeJson
      }

      "there is a pension income with minimum values from overseas transfers" in {
        Json.toJson(pensionIncomeWithMinOverseasPensionContributionModel) mustBe pensionIncomeJsonWithMinOverseasPensionContribution
      }
      "there is a pension income with minimum values from foreign pension" in {
        Json.toJson(pensionIncomeWithMinForeignPensionModel) mustBe pensionIncomeJsonWithMinForeignPension
      }

      "there is a minimal model" in {
        Json.toJson(minimumPensionIncomeModel) mustBe minimumPensionIncomeJson
      }
    }

    "deserialize valid values" when {

      "there is a full model" in {
        fullPensionIncomeJson.as[GetPensionIncomeModel] mustBe fullPensionIncomeModel
      }

      "there is a pension income with minimum values from overseas transfers" in {
        pensionIncomeJsonWithMinOverseasPensionContribution.as[GetPensionIncomeModel] mustBe pensionIncomeWithMinOverseasPensionContributionModel
      }
      "there is a pension income with minimum values from foreign pension" in {
        pensionIncomeJsonWithMinForeignPension.as[GetPensionIncomeModel] mustBe pensionIncomeWithMinForeignPensionModel
      }

      "there is a minimal model" in {
        minimumPensionIncomeJson.as[GetPensionIncomeModel] mustBe minimumPensionIncomeModel
      }
    }
  }
}

