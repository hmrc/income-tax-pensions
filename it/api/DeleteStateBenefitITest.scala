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
import models.{DesErrorBodyModel, DesErrorModel}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import utils.DESTaxYearHelper.desTaxYearConverter

class DeleteStateBenefitITest extends WiremockSpec with ScalaFutures {

  trait Setup {
    val timeSpan: Long = 5
    implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(timeSpan, Seconds))
    val nino: String = "AA123123A"
    val taxYear: Int = 2021
    val benefitId: String = "a111111a-abcd-111a-123a-11a1a111a1"
    val mtditidHeader: (String, String) = ("mtditid", "555555555")
    val requestHeaders: Seq[HttpHeader] = Seq(new HttpHeader("mtditid", "555555555"))
    val desUrl: String = s"/income-tax/income/state-benefits/$nino/${desTaxYearConverter(taxYear)}/custom/$benefitId"
    val serviceUrl: String = s"/income-tax-pensions/state-benefits/nino/$nino/taxYear/$taxYear/benefitId/$benefitId"
    auditStubs()
  }

  "delete state benefit" should {

    "return a 204 No Content response on success" in new Setup {
      stubDeleteWithoutResponseBody(desUrl, NO_CONTENT)
      authorised()

      whenReady(buildClient(serviceUrl)
        .withHttpHeaders(mtditidHeader)
        .delete) {
        result =>
          result.status mustBe NO_CONTENT
      }
    }

    "return a 400 if a downstream Bad Request error occurs" in new Setup {
      val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
        "INVALID_BENEFIT_ID", "Submission has not passed validation. Invalid parameter benefitId."
      )).toString()

      val expectedResult: JsValue = DesErrorModel(BAD_REQUEST, DesErrorBodyModel.invalidBenefitId).toJson

      stubDeleteWithResponseBody(desUrl, BAD_REQUEST, errorResponseBody)
      authorised()

      whenReady(buildClient(serviceUrl)
        .withHttpHeaders(mtditidHeader)
        .delete()) {
        result =>
          result.status mustBe BAD_REQUEST
          Json.parse(result.body) mustBe expectedResult
      }
    }

    "return a 404 if no data is found to delete" in new Setup {
      val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
        "NO_DATA_FOUND", "The remote endpoint has indicated that the requested resource could not be found."
      )).toString()

      stubDeleteWithResponseBody(desUrl, NOT_FOUND, errorResponseBody)
      authorised()

      whenReady(buildClient(serviceUrl)
        .withHttpHeaders(mtditidHeader)
        .delete()) {
        result =>
          result.status mustBe NOT_FOUND
          Json.parse(result.body) mustBe Json.obj(
            "code" -> "NO_DATA_FOUND", "reason" -> "The remote endpoint has indicated that the requested resource could not be found.")
      }
    }

    "return 403 Forbidden if the state benefit cannot be deleted" in new Setup {
      val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
        "DELETE_FORBIDDEN", "The remote endpoint has indicated that HMRC held State Benefit cannot be deleted."
      )).toString()

      stubDeleteWithResponseBody(desUrl, FORBIDDEN, errorResponseBody)
      authorised()

      whenReady(buildClient(serviceUrl)
        .withHttpHeaders(mtditidHeader)
        .delete()) {
        result =>
          result.status mustBe FORBIDDEN
          Json.parse(result.body) mustBe Json.obj(
            "code" -> "DELETE_FORBIDDEN", "reason" -> "The remote endpoint has indicated that HMRC held State Benefit cannot be deleted.")
      }
    }

    "return 500 if a downstream error occurs" in new Setup {

      val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
        "SERVER_ERROR", "DES is currently experiencing problems that require live service intervention.")).toString()

      stubDeleteWithResponseBody(desUrl, INTERNAL_SERVER_ERROR, errorResponseBody)

      authorised()

      whenReady(buildClient(serviceUrl)
        .withHttpHeaders(mtditidHeader)
        .delete()) {
        result =>
          result.status mustBe INTERNAL_SERVER_ERROR
          Json.parse(result.body) mustBe Json.obj(
            "code" -> "SERVER_ERROR", "reason" -> "DES is currently experiencing problems that require live service intervention.")
      }
    }

    "return 503 if a downstream error occurs" in new Setup {

      val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
        "SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")).toString()

      stubDeleteWithResponseBody(desUrl, SERVICE_UNAVAILABLE, errorResponseBody)

      authorised()

      whenReady(buildClient(serviceUrl)
        .withHttpHeaders(mtditidHeader)
        .delete()) {
        result =>
          result.status mustBe SERVICE_UNAVAILABLE
          Json.parse(result.body) mustBe Json.obj("code" -> "SERVICE_UNAVAILABLE", "reason" -> "Dependent systems are currently not responding.")
      }
    }

    "return 401 if user is not authorised" when {

      "user has no HMRC-MTD-IT enrolment" in new Setup {
        unauthorisedOtherEnrolment()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .delete()) {
          result =>
            result.status mustBe UNAUTHORIZED
            result.body mustBe ""
        }
      }

      "the request has no MTDITID header present" in new Setup {
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
