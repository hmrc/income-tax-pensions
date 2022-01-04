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
import connectors.GetPensionReliefsConnectorISpec.expectedResponseBody
import helpers.WiremockSpec
import models.{DesErrorBodyModel, DesErrorModel, GetPensionReliefsModel}
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.DESTaxYearHelper.desTaxYearConverter

class GetPensionReliefsConnectorISpec extends WiremockSpec {

  lazy val connector: GetPensionReliefsConnector = app.injector.instanceOf[GetPensionReliefsConnector]
  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]


  def appConfig(desHost: String): AppConfig = new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override val desBaseUrl: String = s"http://$desHost:$wireMockPort"
  }

  val nino: String = "123456789"
  val taxYear: Int = 2021
  val desUrl = s"/income-tax/reliefs/pensions/$nino/${desTaxYearConverter(taxYear)}"


  ".GetPensionReliefsConnector" should {
    "include internal headers" when {

      val headersSentToDes = Seq(
        new HttpHeader(HeaderNames.authorisation, "Bearer secret"),
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      lazy val internalHost = "localhost"
      lazy val externalHost = "127.0.0.1"

      "the host for DES is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new GetPensionReliefsConnector(httpClient, appConfig(internalHost))
        val expectedResult = Json.parse(expectedResponseBody).as[GetPensionReliefsModel]

        stubGetWithResponseBody(desUrl, OK, expectedResponseBody, headersSentToDes)

        val result = await(connector.getPensionReliefs(nino, taxYear)(hc))

        result mustBe Right(expectedResult)
      }

      "the host for DES is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new GetPensionReliefsConnector(httpClient, appConfig(externalHost))
        val expectedResult = Json.parse(expectedResponseBody).as[GetPensionReliefsModel]

        stubGetWithResponseBody(desUrl, OK, expectedResponseBody, headersSentToDes)

        val result = await(connector.getPensionReliefs(nino, taxYear)(hc))

        result mustBe Right(expectedResult)
      }
    }

    "return a GetPensionReliefsModel when nino and taxYear are present" in {
      val expectedResult = Json.parse(expectedResponseBody).as[GetPensionReliefsModel]
      stubGetWithResponseBody(desUrl, OK, expectedResponseBody)

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getPensionReliefs(nino, taxYear)(hc)).right.get

      result mustBe expectedResult
    }

    "return a Parsing error INTERNAL_SERVER_ERROR response" in {
      lazy val invalidJson = Json.obj("submittedOn" -> true)

      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

      stubGetWithResponseBody(desUrl, OK, invalidJson.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getPensionReliefs(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a SERVICE_UNAVAILABLE" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Dependent systems are currently not responding.")
      val expectedResult = DesErrorModel(SERVICE_UNAVAILABLE, DesErrorBodyModel("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding."))

      stubGetWithResponseBody(desUrl, SERVICE_UNAVAILABLE, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getPensionReliefs(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a BAD_REQUEST" in {
      val responseBody = Json.obj(
        "code" -> "INVALID_NINO",
        "reason" -> "Nino is invalid"
      )
      val expectedResult = DesErrorModel(BAD_REQUEST, DesErrorBodyModel("INVALID_NINO", "Nino is invalid"))

      stubGetWithResponseBody(desUrl, BAD_REQUEST, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getPensionReliefs(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a NOT_FOUND" in {
      val responseBody = Json.obj(
        "code" -> "NOT_FOUND",
        "reason" -> "The remote endpoint has indicated that no data can be found."
      )
      val expectedResult = DesErrorModel(NOT_FOUND, DesErrorBodyModel("NOT_FOUND", "The remote endpoint has indicated that no data can be found."))

      stubGetWithResponseBody(desUrl, NOT_FOUND, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getPensionReliefs(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return an INTERNAL_SERVER_ERROR" in {
      val responseBody = Json.obj(
        "code" -> "SERVER_ERROR",
        "reason" -> "DES is currently experiencing problems that require live service intervention."
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR,
        DesErrorBodyModel("SERVER_ERROR", "DES is currently experiencing problems that require live service intervention."))

      stubGetWithResponseBody(desUrl, INTERNAL_SERVER_ERROR, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getPensionReliefs(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a INTERNAL_SERVER_ERROR  when DES throws an unexpected result" in {
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

      stubGetWithoutResponseBody(desUrl, NO_CONTENT)
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getPensionReliefs(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }
  }
}

object GetPensionReliefsConnectorISpec {
  val expectedResponseBody: String =
    """
      |{
      |  "submittedOn": "2020-01-04T05:01:01Z",
      |  "deletedOn": "2020-01-04T05:01:01Z",
      |  "pensionReliefs": {
      |    "regularPensionContributions": 0,
      |    "oneOffPensionContributionsPaid": 0,
      |    "retirementAnnuityPayments": 0,
      |    "paymentToEmployersSchemeNoTaxRelief": 0,
      |    "overseasPensionSchemeContributions": 0
      |  }
      |}
      |""".stripMargin
}
