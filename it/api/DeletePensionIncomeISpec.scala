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

import helpers.WiremockSpec
import models.DesErrorBodyModel
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import play.api.http.Status._
import play.api.libs.json.Json
import utils.TaxYearHelper.desIfTaxYearConverter

class DeletePensionIncomeISpec extends WiremockSpec with ScalaFutures{

  trait Setup {
    val timeSpan: Long = 5
    implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(timeSpan, Seconds))
    val nino: String = "AA123123A"
    val taxYear: Int = 2021
    val mtditidHeader: (String, String) = ("mtditid", "555555555")
    val mtdBearerToken : (String, String) = ("Authorization", "Bearer:XYZ")
    val requestHeaders: Seq[(String, String)] = Seq(mtditidHeader, mtdBearerToken)
    val iFUrl: String = s"/income-tax/income/pensions/$nino/${desIfTaxYearConverter(taxYear)}"
    val serviceUrl: String = s"/income-tax-pensions/pension-income/nino/$nino/taxYear/$taxYear"
    auditStubs()
  }

  "delete pension income" when {

    "the user is an individual" must {
      "return a No content Success response" in new Setup {

        stubDeleteWithoutResponseBody(iFUrl, NO_CONTENT)

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(requestHeaders:_*)
          .delete()) {
          result =>
            result.status mustBe NO_CONTENT

        }
      }

      "return 400 if a there is an invalid tax year" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "INVALID_TAX_YEAR", "Submission has not passed validation. Invalid parameter taxYear.")).toString()

        stubDeleteWithResponseBody(iFUrl, BAD_REQUEST, errorResponseBody)

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(requestHeaders:_*)
          .delete()) {
          result =>
            result.status mustBe BAD_REQUEST
            Json.parse(result.body) mustBe Json.obj(
              "code" -> "INVALID_TAX_YEAR", "reason" -> "Submission has not passed validation. Invalid parameter taxYear.")
        }
      }

      "return 400 if a there is an invalid taxable entity id (nino)" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "INVALID_TAXABLE_ENTITY_ID", "Submission has not passed validation. Invalid parameter taxableEntityId.")).toString()

        stubDeleteWithResponseBody(iFUrl, BAD_REQUEST, errorResponseBody)

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(requestHeaders:_*)
          .delete()) {
          result =>
            result.status mustBe BAD_REQUEST
            Json.parse(result.body) mustBe Json.obj(
              "code" -> "INVALID_TAXABLE_ENTITY_ID", "reason" -> "Submission has not passed validation. Invalid parameter taxableEntityId.")
        }
      }

      "return 404 if a user has no recorded pension income to delete" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "NO_DATA_FOUND", "The remote endpoint has indicated that the requested resource could not be found.")).toString()

        stubDeleteWithResponseBody(iFUrl, NOT_FOUND, errorResponseBody)

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(requestHeaders:_*)
          .delete()) {
          result =>
            result.status mustBe NOT_FOUND
            result.body mustBe errorResponseBody
        }
      }

      "return 503 if a downstream error occurs" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")).toString()

        stubDeleteWithResponseBody(iFUrl, SERVICE_UNAVAILABLE, errorResponseBody)

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(requestHeaders:_*)
          .delete()) {
          result =>
            result.status mustBe SERVICE_UNAVAILABLE
            Json.parse(result.body) mustBe Json.obj("code" -> "SERVICE_UNAVAILABLE", "reason" -> "Dependent systems are currently not responding.")
        }
      }

      "return 500 if a downstream error occurs" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "SERVER_ERROR", "DES is currently experiencing problems that require live service intervention.")).toString()

        stubDeleteWithResponseBody(iFUrl, INTERNAL_SERVER_ERROR, errorResponseBody)

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(requestHeaders:_*)
          .delete()) {
          result =>
            result.status mustBe INTERNAL_SERVER_ERROR
            Json.parse(result.body) mustBe Json.obj(
              "code" -> "SERVER_ERROR", "reason" -> "DES is currently experiencing problems that require live service intervention.")
        }
      }

      "return 401 if the user has no HMRC-MTD-IT enrolment" in new Setup {
        unauthorisedOtherEnrolment()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(requestHeaders:_*)
          .delete()) {
          result =>
            result.status mustBe UNAUTHORIZED
            result.body mustBe ""
        }
      }

      "return 401 if the request has no MTDITID header present" in new Setup {
        whenReady(buildClient(serviceUrl)
          .delete()) {
          result =>
            result.status mustBe UNAUTHORIZED
            result.body mustBe ""
        }
      }
    }

  }


}
