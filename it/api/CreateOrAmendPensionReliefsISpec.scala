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

package api

import helpers.WiremockSpec
import models.{CreateOrUpdatePensionReliefsModel, DesErrorBodyModel, PensionReliefs}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import utils.TaxYearHelper.desIfTaxYearConverter

class CreateOrAmendPensionReliefsISpec extends WiremockSpec with ScalaFutures {

  val minimumPensionReliefs: PensionReliefs = PensionReliefs(
    regularPensionContributions = Some(10.22), None, None, None, None)

  val fullPensionReliefs: PensionReliefs = PensionReliefs(
    regularPensionContributions = Some(10.22),
    oneOffPensionContributionsPaid = Some(11.33),
    retirementAnnuityPayments = Some(12.44),
    paymentToEmployersSchemeNoTaxRelief = Some(13.55),
    overseasPensionSchemeContributions = Some(14.66))

  val fullCreateOrUpdatePensionReliefsData: CreateOrUpdatePensionReliefsModel = CreateOrUpdatePensionReliefsModel(fullPensionReliefs)
  val minEmploymentFinancialData: CreateOrUpdatePensionReliefsModel = CreateOrUpdatePensionReliefsModel(minimumPensionReliefs)

  val fullJson: JsValue = Json.parse(
    """{
      |	"pensionReliefs": {
      |		"regularPensionContributions": 10.22,
      |		"oneOffPensionContributionsPaid": 11.33,
      |		"retirementAnnuityPayments": 12.44,
      |		"paymentToEmployersSchemeNoTaxRelief": 13.55,
      |		"overseasPensionSchemeContributions": 14.66
      |	}
      |}""".stripMargin)

  trait Setup {
    val timeSpan: Long = 5
    implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(timeSpan, Seconds))
    val nino: String = "AA123123A"
    val taxYear: Int = 2021
    val mtditidHeader: (String, String) = ("mtditid", "555555555")
    val mtdBearerToken : (String, String) = ("Authorization", "Bearer:XYZ")
    val requestHeaders: Seq[(String, String)] = Seq(mtditidHeader, mtdBearerToken)
    val desUrl = s"/income-tax/reliefs/pensions/$nino/${desIfTaxYearConverter(taxYear)}"
    val serviceUrl: String = s"/income-tax-pensions/pension-reliefs/nino/$nino/taxYear/$taxYear"
    auditStubs()
  }

  "create or amend pension reliefs" when {

    "the user is an individual" must {
      "return a No content(204) Success response" in new Setup {

        stubPutWithoutResponseBody(desUrl, Json.toJson(fullCreateOrUpdatePensionReliefsData).toString(), NO_CONTENT)

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(requestHeaders:_*)
          .put(fullJson)) {
          result =>
            result.status mustBe NO_CONTENT

        }
      }

      "return Bad Request(400) if the body payload validation fails" in new Setup {

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(requestHeaders:_*)
          .put(Json.obj())) {
          result =>
            result.status mustBe BAD_REQUEST
        }
      }

      "return Bad Request(400) if a downstream bad request error occurs" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "INVALID_TAXABLE_ENTITY_ID", "Submission has not passed validation. Invalid parameter taxableEntityId.")).toString()

        stubPutWithResponseBody(desUrl, Json.toJson(fullCreateOrUpdatePensionReliefsData).toString(), errorResponseBody, BAD_REQUEST)

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(requestHeaders:_*)
          .put(fullJson)) {
          result =>
            result.status mustBe BAD_REQUEST
            result.body mustBe errorResponseBody
        }
      }

      "return Internal Server Error(500) if an unexpected error is returned from DES user" in new Setup {

        // e,g, 404 not found is not expected as create or update will create if not found
        val errorResponseBody: String = Json.toJson(DesErrorBodyModel.parsingError).toString()

        stubPutWithResponseBody(desUrl, Json.toJson(fullCreateOrUpdatePensionReliefsData).toString(), errorResponseBody, NOT_FOUND)

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(requestHeaders:_*)
          .put(fullJson)) {
          result =>
            result.status mustBe INTERNAL_SERVER_ERROR
            result.body mustBe errorResponseBody
        }
      }

      "return Service Unavailable(503) if a downstream service unavailable error occurs" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")).toString()

        stubPutWithResponseBody(desUrl, Json.toJson(fullCreateOrUpdatePensionReliefsData).toString(), errorResponseBody, SERVICE_UNAVAILABLE)

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(requestHeaders:_*)
          .put(fullJson)) {
          result =>
            result.status mustBe SERVICE_UNAVAILABLE
            result.body mustBe errorResponseBody
            Json.parse(result.body) mustBe Json.obj("code" -> "SERVICE_UNAVAILABLE", "reason" -> "Dependent systems are currently not responding.")
        }
      }

      "return Internal Server Error(500) if a downstream internal server error occurs" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "SERVER_ERROR", "DES is currently experiencing problems that require live service intervention.")).toString()

        stubPutWithResponseBody(desUrl, Json.toJson(fullCreateOrUpdatePensionReliefsData).toString(), errorResponseBody, INTERNAL_SERVER_ERROR)

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(requestHeaders:_*)
          .put(fullJson)) {
          result =>
            result.status mustBe INTERNAL_SERVER_ERROR
            result.body mustBe errorResponseBody
            Json.parse(result.body) mustBe Json.obj(
              "code" -> "SERVER_ERROR", "reason" -> "DES is currently experiencing problems that require live service intervention.")
        }
      }

      "return Unauthorised(401) if the user has no HMRC-MTD-IT enrolment" in new Setup {
        unauthorisedOtherEnrolment()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(requestHeaders:_*)
          .put(fullJson)) {
          result =>
            result.status mustBe UNAUTHORIZED
            result.body mustBe ""
        }
      }

      "return Unauthorised(401) if the request has no MTDITID header present" in new Setup {
        whenReady(buildClient(serviceUrl)
          .put(fullJson)) {
          result =>
            result.status mustBe UNAUTHORIZED
            result.body mustBe ""
        }
      }
    }

  }

}
