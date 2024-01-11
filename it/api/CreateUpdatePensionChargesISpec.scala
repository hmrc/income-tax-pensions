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

package api

import api.PensionsChargesTestData._
import helpers.WiremockSpec
import models._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import play.api.http.Status._
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.libs.json.{JsValue, Json}
import utils.TaxYearHelper.desIfTaxYearConverter

class CreateUpdatePensionChargesISpec extends WiremockSpec with ScalaFutures {

  trait Setup {
    val timeSpan: Long = 5
    implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(timeSpan, Seconds))
    val nino: String = "AA123123A"
    val taxYear: Int = 2021
    val mtditidHeader: (String, String) = ("mtditid", "555555555")
    val mtdBearerToken : (String, String) = ("Authorization", "Bearer:XYZ")
    val requestHeaders: Seq[(String, String)] = Seq(mtditidHeader, mtdBearerToken)
    val desUrl: String = s"/income-tax/charges/pensions/$nino/${desIfTaxYearConverter(taxYear)}"
    val serviceUrl: String = s"/income-tax-pensions/pension-charges/nino/$nino/taxYear/$taxYear"
    auditStubs()
  }

  "create or update pension charges" should {

    "return 204 No Content on success" in new Setup {

      stubPutWithoutResponseBody(desUrl, createUpdatePensionChargesJsonStr, NO_CONTENT)
      authorised()

      whenReady(buildClient(serviceUrl)
        .withHttpHeaders(requestHeaders:_*)
        .put(createUpdatePensionChargesPayload)) {
        result =>
          result.status mustBe NO_CONTENT
      }
    }

    "return 400 if body payload validation fails" in new Setup {
      authorised()
      val badJson: JsValue = Json.parse(
        """{
          |	"submittedOn": "2020-07-27T17:00:19Z",
          |	"pensionSavingsTaxCharges": {
          |		"pensionSchemeTaxReference": ["00123456RA", "00123456RB"],
          |		"benefitInExcessOfLifetimeAllowance": {
          |			"amount": "bad",
          |			"taxPaid": 200.02
          |		}
          |	}
          |}""".stripMargin)

      whenReady(buildClient(serviceUrl)
        .withHttpHeaders(requestHeaders:_*)
        .put(badJson)) {
        result =>
          result.status mustBe BAD_REQUEST
      }
    }

    "return 400 if there is an invalid tax year" in new Setup {

      val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
        "INVALID_TAX_YEAR", "Submission has not passed validation. Invalid parameter taxYear.")).toString()

      stubPutWithResponseBody(
        url = desUrl,
        status = BAD_REQUEST,
        requestBody = createUpdatePensionChargesJsonStr,
        responseBody = errorResponseBody
      )
      authorised()

      whenReady(buildClient(serviceUrl)
        .withHttpHeaders(requestHeaders:_*)
        .put(createUpdatePensionChargesPayload)) {
        result =>
          result.status mustBe BAD_REQUEST
          Json.parse(result.body) mustBe Json.obj(
            "code" -> "INVALID_TAX_YEAR", "reason" -> "Submission has not passed validation. Invalid parameter taxYear.")
      }
    }

    "return 400 if a there is an invalid taxable entity id (nino)" in new Setup {

      val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
        "INVALID_TAXABLE_ENTITY_ID", "Submission has not passed validation. Invalid parameter taxableEntityId.")).toString()

      stubPutWithResponseBody(
        url = desUrl,
        status = BAD_REQUEST,
        requestBody = createUpdatePensionChargesJsonStr,
        responseBody = errorResponseBody
      )
      authorised()

      whenReady(buildClient(serviceUrl)
        .withHttpHeaders(requestHeaders:_*)
        .put(createUpdatePensionChargesPayload)) {
        result =>
          result.status mustBe BAD_REQUEST
          Json.parse(result.body) mustBe Json.obj(
            "code" -> "INVALID_TAXABLE_ENTITY_ID", "reason" -> "Submission has not passed validation. Invalid parameter taxableEntityId.")
      }
    }

    "return 400 if a there is an invalid header correlation id" in new Setup {

      val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
        "INVALID_CORRELATIONID", "Submission has not passed validation. Invalid Header parameter CorrelationId.")).toString()

      stubPutWithResponseBody(
        url = desUrl,
        status = BAD_REQUEST,
        requestBody = createUpdatePensionChargesJsonStr,
        responseBody = errorResponseBody
      )
      authorised()

      whenReady(buildClient(serviceUrl)
        .withHttpHeaders(requestHeaders:_*)
        .put(createUpdatePensionChargesPayload)) {
        {
          result =>
            result.status mustBe BAD_REQUEST
            Json.parse(result.body) mustBe Json.obj(
              "code" -> "INVALID_CORRELATIONID", "reason" -> "Submission has not passed validation. Invalid Header parameter CorrelationId.")
        }
      }
    }

    "return 400 if a there is an invalid payload" in new Setup {

      val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
        "INVALID_PAYLOAD", "Submission has not passed validation. Invalid payload.")).toString()

      stubPutWithResponseBody(
        url = desUrl,
        status = BAD_REQUEST,
        requestBody = "{}",
        responseBody = errorResponseBody
      )
      authorised()

      whenReady(buildClient(serviceUrl)
        .withHttpHeaders(requestHeaders:_*)
        .put(Json.obj())) {
        result =>
          result.status mustBe BAD_REQUEST
          Json.parse(result.body) mustBe Json.obj(
            "code" -> "INVALID_PAYLOAD", "reason" -> "Submission has not passed validation. Invalid payload.")
      }
    }

    "return 500 if a downstream internal server error occurs" in new Setup {

      val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
        "SERVER_ERROR", "DES is currently experiencing problems that require live service intervention.")).toString()

      stubPutWithResponseBody(desUrl, createUpdatePensionChargesJsonStr, errorResponseBody, INTERNAL_SERVER_ERROR)

      authorised()

      whenReady(buildClient(serviceUrl)
        .withHttpHeaders(requestHeaders:_*)
        .put(createUpdatePensionChargesPayload)) {
        result =>
          result.status mustBe INTERNAL_SERVER_ERROR
          result.body mustBe errorResponseBody
          Json.parse(result.body) mustBe Json.obj(
            "code" -> "SERVER_ERROR", "reason" -> "DES is currently experiencing problems that require live service intervention.")
      }
    }

    "return 503 if a downstream service unavailable error occurs" in new Setup {

      val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
        "SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")).toString()

      stubPutWithResponseBody(desUrl, createUpdatePensionChargesJsonStr, errorResponseBody, SERVICE_UNAVAILABLE)

      authorised()

      whenReady(buildClient(serviceUrl)
        .withHttpHeaders(requestHeaders:_*)
        .put(createUpdatePensionChargesPayload)) {
        result =>
          result.status mustBe SERVICE_UNAVAILABLE
          result.body mustBe errorResponseBody
          Json.parse(result.body) mustBe Json.obj("code" -> "SERVICE_UNAVAILABLE", "reason" -> "Dependent systems are currently not responding.")
      }
    }

    "return 401" when {
      "user has no HMRC-MTD-IT enrolment" in new Setup {
        unauthorisedOtherEnrolment()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(requestHeaders:_*)
          .put(minimumRequestPayload)) {
          result =>
            result.status mustBe UNAUTHORIZED
            result.body mustBe ""
        }
      }

      "user has no MTDITID header present" in new Setup {
        whenReady(buildClient(serviceUrl)
          .put(minimumRequestPayload)) {
          result =>
            result.status mustBe UNAUTHORIZED
            result.body mustBe ""
        }
      }
    }
  }
}
