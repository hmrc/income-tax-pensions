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

package api

import com.github.tomakehurst.wiremock.http.HttpHeader
import helpers.WiremockSpec
import models.{CreateUpdatePensionIncomeModel, DesErrorBodyModel, ForeignPension, GetPensionIncomeModel, OverseasPensionContribution}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import utils.DESTaxYearHelper.desTaxYearConverter

class CreateOrAmendPensionIncomeITest extends WiremockSpec with ScalaFutures {


  val minimumPensionIncomeModel: CreateUpdatePensionIncomeModel = CreateUpdatePensionIncomeModel(
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
  val fullPensionIncomeModel: CreateUpdatePensionIncomeModel =
    CreateUpdatePensionIncomeModel(
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
    
  val fullPensionIncomeJson: JsValue = Json.parse(
    """
      | {
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



  trait Setup {
    val timeSpan: Long = 5
    implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(timeSpan, Seconds))
    val nino: String = "AA123123A"
    val taxYear: Int = 2021
    val mtditidHeader: (String, String) = ("mtditid", "555555555")
    val requestHeaders: Seq[HttpHeader] = Seq(new HttpHeader("mtditid", "555555555"))
    val desUrl = s"/income-tax/income/pensions/$nino/${desTaxYearConverter(taxYear)}"
    val serviceUrl: String = s"/income-tax-pensions/pension-income/nino/$nino/taxYear/$taxYear"
    auditStubs()
  }

  "create or amend pension income" when {

    "the user is an individual" must {
      "return a No content Success response" in new Setup {

        stubPutWithoutResponseBody(desUrl, Json.toJson(fullPensionIncomeModel).toString(), NO_CONTENT)

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .put(fullPensionIncomeJson)) {
          result =>
            result.status mustBe NO_CONTENT

        }
      }

      "return 400 if the body payload validation fails" in new Setup {

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .put(Json.obj())) {
          result =>
            result.status mustBe BAD_REQUEST
        }
      }

      "return 400 if a downstream bad request error occurs" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "INVALID_TAXABLE_ENTITY_ID", "Submission has not passed validation. Invalid parameter taxableEntityId.")).toString()

        stubPutWithResponseBody(desUrl, Json.toJson(fullPensionIncomeModel).toString(), errorResponseBody, BAD_REQUEST)

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .put(fullPensionIncomeJson)) {
          result =>
            result.status mustBe BAD_REQUEST
            result.body mustBe errorResponseBody
        }
      }

      "return 500 if an unexpected error is returned from DES user" in new Setup {

        // e,g, 404 not found is not expected as create or update will create if not found
        val errorResponseBody: String = Json.toJson(DesErrorBodyModel.parsingError).toString()

        stubPutWithResponseBody(desUrl, Json.toJson(fullPensionIncomeModel).toString(), errorResponseBody, NOT_FOUND)

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .put(fullPensionIncomeJson)) {
          result =>
            result.status mustBe INTERNAL_SERVER_ERROR
            result.body mustBe errorResponseBody
        }
      }

      "return 503 if a downstream service unavailable error occurs" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")).toString()

        stubPutWithResponseBody(desUrl, Json.toJson(fullPensionIncomeModel).toString(), errorResponseBody, SERVICE_UNAVAILABLE)

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .put(fullPensionIncomeJson)) {
          result =>
            result.status mustBe SERVICE_UNAVAILABLE
            result.body mustBe errorResponseBody
            Json.parse(result.body) mustBe Json.obj("code" -> "SERVICE_UNAVAILABLE", "reason" -> "Dependent systems are currently not responding.")
        }
      }

      "return 500 if a downstream internal server error occurs" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "SERVER_ERROR", "DES is currently experiencing problems that require live service intervention.")).toString()

        stubPutWithResponseBody(desUrl, Json.toJson(fullPensionIncomeModel).toString(), errorResponseBody, INTERNAL_SERVER_ERROR)

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .put(fullPensionIncomeJson)) {
          result =>
            result.status mustBe INTERNAL_SERVER_ERROR
            result.body mustBe errorResponseBody
            Json.parse(result.body) mustBe Json.obj(
              "code" -> "SERVER_ERROR", "reason" -> "DES is currently experiencing problems that require live service intervention.")
        }
      }

      "return 401 if the user has no HMRC-MTD-IT enrolment" in new Setup {
        unauthorisedOtherEnrolment()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .put(fullPensionIncomeJson)) {
          result =>
            result.status mustBe UNAUTHORIZED
            result.body mustBe ""
        }
      }

      "return 401 if the request has no MTDITID header present" in new Setup {
        whenReady(buildClient(serviceUrl)
          .put(fullPensionIncomeJson)) {
          result =>
            result.status mustBe UNAUTHORIZED
            result.body mustBe ""
        }
      }
    }

  }

}
