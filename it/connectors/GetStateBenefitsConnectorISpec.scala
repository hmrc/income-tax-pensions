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

package connectors

import com.github.tomakehurst.wiremock.http.HttpHeader
import config.{AppConfig, BackendAppConfig}
import helpers.WiremockSpec
import models.{DesErrorBodyModel, DesErrorModel, GetStateBenefitsModel}
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import connectors.GetStateBenefitsConnectorISpec.expectedResponseBody

class GetStateBenefitsConnectorISpec extends WiremockSpec {

  lazy val connector: GetStateBenefitsConnector = app.injector.instanceOf[GetStateBenefitsConnector]

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  def appConfig(benefitsHost: String): AppConfig = new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override val benefitsBaseUrl: String = s"http://$benefitsHost:$wireMockPort"
  }

  val nino: String = "123456789"
  val taxYear: Int = 2021
  val benefitsUrl: String = s"/income-tax-benefits/state-benefits/nino/$nino/taxYear/$taxYear"

  ".getStateBenefits" should {

    "include internal headers" when {
      val headersSentToBenefits = Seq(
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      val internalHost = "localhost"
      val externalHost = "127.0.0.1"

      "the host for DES is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new GetStateBenefitsConnector(httpClient, appConfig(internalHost))
        val expectedResult = Json.parse(expectedResponseBody).as[GetStateBenefitsModel]

        stubGetWithResponseBody(benefitsUrl,
          OK, expectedResponseBody, headersSentToBenefits)

        val result = await(connector.getStateBenefits(nino, taxYear)(hc))

        result mustBe Right(Some(expectedResult))
      }

      "the host for DES is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new GetStateBenefitsConnector(httpClient, appConfig(externalHost))
        val expectedResult = Json.parse(expectedResponseBody).as[GetStateBenefitsModel]

        stubGetWithResponseBody(benefitsUrl,
          OK, expectedResponseBody, headersSentToBenefits)

        val result = await(connector.getStateBenefits(nino, taxYear)(hc))

        result mustBe Right(Some(expectedResult))
      }
    }

    "return a GetStateBenefitsModel" when {

      "nino and tax year are present" in {
        val expectedResult = Json.parse(expectedResponseBody).as[GetStateBenefitsModel]

        stubGetWithResponseBody(benefitsUrl, OK, expectedResponseBody)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.getStateBenefits(nino, taxYear)(hc)).right.get

        result mustBe Some(expectedResult)
      }
    }

    "return a Right None when NOT_FOUND" in {
      stubGetWithResponseBody(benefitsUrl, NOT_FOUND, "")
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getStateBenefits(nino, taxYear)(hc))

      result mustBe Right(None)
    }

    "return a Parsing error INTERNAL_SERVER_ERROR response" in {
      val invalidJson = Json.obj("stateBenefits" -> Some(true))

      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

      stubGetWithResponseBody(benefitsUrl, OK, invalidJson.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getStateBenefits(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a SERVICE_UNAVAILABLE" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Dependent systems are currently not responding.")
      val expectedResult = DesErrorModel(SERVICE_UNAVAILABLE, DesErrorBodyModel.serviceUnavailable)

      stubGetWithResponseBody(benefitsUrl
        , SERVICE_UNAVAILABLE, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getStateBenefits(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a BAD_REQUEST" in {
      val responseBody = Json.obj(
        "code" -> "INVALID_NINO",
        "reason" -> "Nino is invalid"
      )
      val expectedResult = DesErrorModel(BAD_REQUEST, DesErrorBodyModel("INVALID_NINO", "Nino is invalid"))

      stubGetWithResponseBody(benefitsUrl
        , BAD_REQUEST, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getStateBenefits(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return an INTERNAL_SERVER_ERROR" in {
      val responseBody = Json.obj(
        "code" -> "SERVER_ERROR",
        "reason" -> "DES is currently experiencing problems that require live service intervention."
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.serverError)

      stubGetWithResponseBody(benefitsUrl
        , INTERNAL_SERVER_ERROR, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getStateBenefits(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a INTERNAL_SERVER_ERROR  when DES throws an unexpected result" in {
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

      stubGetWithoutResponseBody(benefitsUrl
        , NO_CONTENT)
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getStateBenefits(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }
  }

}

object GetStateBenefitsConnectorISpec {
  val expectedResponseBody: String = {
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
      |
      |""".stripMargin
  }
}
