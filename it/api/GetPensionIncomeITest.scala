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
import models.DesErrorBodyModel
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import play.api.http.Status._
import play.api.libs.json.Json
import utils.DESTaxYearHelper.desTaxYearConverter

class GetPensionIncomeITest extends WiremockSpec with ScalaFutures {

  trait Setup {
    implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(5.0, Seconds))
    val nino: String = "AA123123A"
    val taxYear = 2021
    val agentClientCookie: Map[String, String] = Map("MTDITID" -> "555555555")
    val mtditidHeader: (String, String) = ("mtditid", "555555555")
    val requestHeaders: Seq[HttpHeader] = Seq(new HttpHeader("mtditid", "555555555"))
    val desUrl = s"/income-tax/income/pensions/$nino/${desTaxYearConverter(taxYear)}"
    val serviceUrl: String = s"/income-tax-pensions/pension-income/nino/$nino/taxYear/$taxYear"
    auditStubs()
  }

  val GetPensionIncomeDesResponseBody: String =
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


  "get pension income" when {

    "the user is an individual" must {
      "return a 200 and the pension income" in new Setup {

        stubGetWithResponseBody(desUrl, OK, GetPensionIncomeDesResponseBody)

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .get) {
          result =>
            result.status mustBe OK
            Json.parse(result.body) mustBe Json.parse(GetPensionIncomeDesResponseBody)
        }
      }

      "return 400 if there is an invalid tax year" in new Setup {
        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "INVALID_TAX_YEAR", "Submission has not passed validation. Invalid parameter taxYear.")).toString()

        stubGetWithResponseBody(desUrl, BAD_REQUEST, errorResponseBody)
        authorised()
        whenReady(buildClient(serviceUrl)
        .withHttpHeaders(mtditidHeader)
        .get) {
          result =>
            result.status mustBe BAD_REQUEST
            Json.parse(result.body) mustBe Json.obj(
              "code" -> "INVALID_TAX_YEAR", "reason" -> "Submission has not passed validation. Invalid parameter taxYear.")
        }
      }

      "return 400 if a there is an invalid taxable entity id (nino)" in new Setup {
        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "INVALID_TAXABLE_ENTITY_ID", "Submission has not passed validation. Invalid parameter taxableEntityId.")).toString()

        stubGetWithResponseBody(desUrl, BAD_REQUEST, errorResponseBody)
        authorised()
        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .get) {
          result =>
            result.status mustBe BAD_REQUEST
            Json.parse(result.body) mustBe Json.obj(
              "code" -> "INVALID_TAXABLE_ENTITY_ID", "reason" -> "Submission has not passed validation. Invalid parameter taxableEntityId.")
        }
      }

      "return 404 if a user has no recorded pension incomes" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel.noDataFound).toString()

        stubGetWithResponseBody(
          url = desUrl,
          status = NOT_FOUND,
          response = errorResponseBody
        )

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .get) {
          result =>
            result.status mustBe NOT_FOUND
            result.body mustBe errorResponseBody
        }
      }

      "return 503 if a downstream error occurs" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")).toString()

        stubGetWithResponseBody(
          url = desUrl,
          status = SERVICE_UNAVAILABLE,
          response = errorResponseBody
        )

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .get) {
          result =>
            result.status mustBe SERVICE_UNAVAILABLE
            Json.parse(result.body) mustBe Json.obj("code" -> "SERVICE_UNAVAILABLE", "reason" -> "Dependent systems are currently not responding.")
        }
      }

      "return 500 if a downstream error occurs" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "SERVER_ERROR", "DES is currently experiencing problems that require live service intervention.")).toString()

        stubGetWithResponseBody(
          url = desUrl,
          status = INTERNAL_SERVER_ERROR,
          response = errorResponseBody
        )

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .get) {
          result =>
            result.status mustBe INTERNAL_SERVER_ERROR
            Json.parse(result.body) mustBe Json.obj(
              "code" -> "SERVER_ERROR", "reason" -> "DES is currently experiencing problems that require live service intervention.")
        }
      }

      "return 401 if the user has no HMRC-MTD-IT enrolment" in new Setup {
        unauthorisedOtherEnrolment()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .get) {
          result =>
            result.status mustBe UNAUTHORIZED
            result.body mustBe ""
        }
      }

      "return 401 if the request has no MTDITID header present" in new Setup {
        whenReady(buildClient(serviceUrl)
          .get) {
          result =>
            result.status mustBe UNAUTHORIZED
            result.body mustBe ""
        }
      }
    }
  }

}
