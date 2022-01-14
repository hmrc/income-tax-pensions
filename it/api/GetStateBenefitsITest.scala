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

class GetStateBenefitsITest extends WiremockSpec with ScalaFutures {

  trait Setup {
    val timeSpan: Long = 5
    implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(timeSpan, Seconds))
    val nino: String = "AA123123A"
    val taxYear: Int = 2021
    val benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c934"
    val mtditidHeader: (String, String) = ("mtditid", "555555555")
    val requestHeaders: Seq[HttpHeader] = Seq(new HttpHeader("mtditid", "555555555"))
    val desUrl: String = s"/income-tax/income/state-benefits/$nino/${desTaxYearConverter(taxYear)}\\?benefitId=$benefitId"
    val serviceUrl: String = s"/income-tax-pensions/state-benefits/nino/$nino/taxYear/$taxYear?benefitId=$benefitId"
    auditStubs()
  }

  val GetStateBenefitsDesResponseBody: String =
    """
      |{
      |   "stateBenefits":{
      |      "incapacityBenefit":[
      |         {
      |            "dateIgnored":"2019-04-11T16:22:00Z",
      |            "submittedOn":"2020-09-11T17:23:00Z",
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |            "startDate":"2019-11-13",
      |            "endDate":"2020-08-23",
      |            "amount":1212.34,
      |            "taxPaid":22323.23
      |         }
      |      ],
      |      "statePension":{
      |         "dateIgnored":"2018-09-09T19:23:00Z",
      |         "submittedOn":"2020-08-07T12:23:00Z",
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c935",
      |         "startDate":"2018-06-03",
      |         "endDate":"2020-09-13",
      |         "amount":42323.23,
      |         "taxPaid":2323.44
      |      },
      |      "statePensionLumpSum":{
      |         "dateIgnored":"2019-07-08T05:23:00Z",
      |         "submittedOn":"2020-03-13T19:23:00Z",
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c936",
      |         "startDate":"2019-04-23",
      |         "endDate":"2020-08-13",
      |         "amount":45454.23,
      |         "taxPaid":45432.56
      |      },
      |      "employmentSupportAllowance":[
      |         {
      |            "dateIgnored":"2019-09-28T10:23:00Z",
      |            "submittedOn":"2020-11-13T19:23:00Z",
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c937",
      |            "startDate":"2019-09-23",
      |            "endDate":"2020-08-23",
      |            "amount":44545.43,
      |            "taxPaid":35343.23
      |         }
      |      ],
      |      "jobSeekersAllowance":[
      |         {
      |            "dateIgnored":"2019-08-18T13:23:00Z",
      |            "submittedOn":"2020-07-10T18:23:00Z",
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c938",
      |            "startDate":"2019-09-19",
      |            "endDate":"2020-09-23",
      |            "amount":33223.12,
      |            "taxPaid":44224.56
      |         }
      |      ],
      |      "bereavementAllowance":{
      |         "dateIgnored":"2020-08-10T12:23:00Z",
      |         "submittedOn":"2020-09-19T19:23:00Z",
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c939",
      |         "startDate":"2019-05-22",
      |         "endDate":"2020-09-26",
      |         "amount":56534.23,
      |         "taxPaid":34343.57
      |      },
      |      "otherStateBenefits":{
      |         "dateIgnored":"2020-01-11T15:23:00Z",
      |         "submittedOn":"2020-09-13T15:23:00Z",
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c940",
      |         "startDate":"2018-09-03",
      |         "endDate":"2020-06-03",
      |         "amount":56532.45,
      |         "taxPaid":5656.89
      |      }
      |   },
      |   "customerAddedStateBenefits":{
      |      "incapacityBenefit":[
      |         {
      |            "submittedOn":"2020-11-17T19:23:00Z",
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c941",
      |            "startDate":"2018-07-17",
      |            "endDate":"2020-09-23",
      |            "amount":45646.78,
      |            "taxPaid":4544.34
      |         }
      |      ],
      |      "statePension":{
      |         "submittedOn":"2020-06-11T10:23:00Z",
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c943",
      |         "startDate":"2018-04-03",
      |         "endDate":"2020-09-13",
      |         "amount":45642.45,
      |         "taxPaid":6764.34
      |      },
      |      "statePensionLumpSum":{
      |         "submittedOn":"2020-06-13T05:29:00Z",
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c956",
      |         "startDate":"2019-09-23",
      |         "endDate":"2020-09-26",
      |         "amount":34322.34,
      |         "taxPaid":4564.45
      |      },
      |      "employmentSupportAllowance":[
      |         {
      |            "submittedOn":"2020-02-10T11:20:00Z",
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c988",
      |            "startDate":"2019-09-11",
      |            "endDate":"2020-06-13",
      |            "amount":45424.23,
      |            "taxPaid":23232.34
      |         }
      |      ],
      |      "jobSeekersAllowance":[
      |         {
      |            "submittedOn":"2020-05-13T14:23:00Z",
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c990",
      |            "startDate":"2019-07-10",
      |            "endDate":"2020-05-11",
      |            "amount":34343.78,
      |            "taxPaid":3433.56
      |         }
      |      ],
      |      "bereavementAllowance":{
      |         "submittedOn":"2020-02-13T11:23:00Z",
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c997",
      |         "startDate":"2018-08-12",
      |         "endDate":"2020-07-13",
      |         "amount":45423.45,
      |         "taxPaid":4543.64
      |      },
      |      "otherStateBenefits":{
      |         "submittedOn":"2020-09-12T12:23:00Z",
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c957",
      |         "startDate":"2018-01-13",
      |         "endDate":"2020-08-13",
      |         "amount":63333.33,
      |         "taxPaid":4644.45
      |      }
      |   }
      |}
      |""".stripMargin


  "get state benefits" when {

    "the user is an individual" must {
      "return 200 and the state benefits for a user" in new Setup {

        stubGetWithResponseBody(
          url = desUrl,
          status = OK,
          response = GetStateBenefitsDesResponseBody
        )

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .get) {
          result =>
            result.status mustBe OK
            Json.parse(result.body) mustBe Json.parse(GetStateBenefitsDesResponseBody)
        }
      }

      "return 400 if a there is an invalid tax year" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "INVALID_TAX_YEAR", "Submission has not passed validation. Invalid parameter taxYear.")).toString()
        stubGetWithResponseBody(
          url = desUrl,
          status = BAD_REQUEST,
          response = errorResponseBody
        )

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
        stubGetWithResponseBody(
          url = desUrl,
          status = BAD_REQUEST,
          response = errorResponseBody
        )

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

      "return 400 if a there is an invalid header correlation id" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "INVALID_CORRELATIONID", "Submission has not passed validation. Invalid Header parameter CorrelationId.")).toString()
        stubGetWithResponseBody(
          url = desUrl,
          status = BAD_REQUEST,
          response = errorResponseBody
        )

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .get) {
          result =>
            result.status mustBe BAD_REQUEST
            Json.parse(result.body) mustBe Json.obj(
              "code" -> "INVALID_CORRELATIONID", "reason" -> "Submission has not passed validation. Invalid Header parameter CorrelationId.")
        }
      }

      "return 400 if a there is an invalid benefit id" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "INVALID_BENEFIT_ID", "Submission has not passed validation. Invalid parameter benefitId.")).toString()
        stubGetWithResponseBody(
          url = desUrl,
          status = BAD_REQUEST,
          response = errorResponseBody
        )

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .get) {
          result =>
            result.status mustBe BAD_REQUEST
            Json.parse(result.body) mustBe Json.obj(
              "code" -> "INVALID_BENEFIT_ID", "reason" -> "Submission has not passed validation. Invalid parameter benefitId.")
        }
      }

      "return 404 if a user has no recorded state benefits" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "NO_DATA_FOUND", "The remote endpoint has indicated that no data can be found.")).toString()

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

      "return 422 if a the date range is invalid" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "INVALID_DATE_RANGE", "The remote endpoint has indicated that tax year requested exceeds CY-4.")).toString()

        stubGetWithResponseBody(
          url = desUrl,
          status = UNPROCESSABLE_ENTITY,
          response = errorResponseBody
        )

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .get) {
          result =>
            result.status mustBe UNPROCESSABLE_ENTITY
            result.body mustBe errorResponseBody
        }
      }

      "return 422 if the tax year is not supported" in new Setup {

        val errorResponseBody: String = Json.toJson(DesErrorBodyModel(
          "TAX_YEAR_NOT_SUPPORTED", "The remote endpoint has indicated that requested tax year is    not supported.")).toString()

        stubGetWithResponseBody(
          url = desUrl,
          status = UNPROCESSABLE_ENTITY,
          response = errorResponseBody
        )

        authorised()

        whenReady(buildClient(serviceUrl)
          .withHttpHeaders(mtditidHeader)
          .get) {
          result =>
            result.status mustBe UNPROCESSABLE_ENTITY
            result.body mustBe errorResponseBody
        }
      }

    }

  }

}
