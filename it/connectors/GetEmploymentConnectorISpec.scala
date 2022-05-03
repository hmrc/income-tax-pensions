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
import connectors.GetEmploymentConnectorISpec.expectedResponseBody
import helpers.WiremockSpec
import models.{DesErrorBodyModel, DesErrorModel, GetEmploymentPensionsModel}
import play.api.Configuration
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, NO_CONTENT, OK, SERVICE_UNAVAILABLE}
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class GetEmploymentConnectorISpec extends WiremockSpec {

  lazy val connector: EmploymentConnector = app.injector.instanceOf[EmploymentConnector]

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  def appConfig(employmentHost: String): AppConfig = new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override val employmentBaseUrl: String = s"http://$employmentHost:$wireMockPort"
  }

    val nino: String = "123456789"
    val taxYear: Int = 2021
    val employmentsUrl: String = s"/income-tax-employment/income-tax/nino/$nino/sources"

  ".getEmploymentPensions" should {

    "include internal headers" when {
      val headersSentToBenefits = Seq(
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      val internalHost = "localhost"
      val externalHost = "127.0.0.1"

      "the host is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new EmploymentConnector(httpClient, appConfig(internalHost))
        val expectedResult = Json.parse(expectedResponseBody).as[GetEmploymentPensionsModel]

        stubGetWithResponseBody(employmentsUrl,
          OK, expectedResponseBody, headersSentToBenefits)

        val result = await(connector.getEmploymentPensions(nino, taxYear)(hc))

        result mustBe Right(Some(expectedResult))
      }

      "the host for DES is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new EmploymentConnector(httpClient, appConfig(externalHost))
        val expectedResult = Json.parse(expectedResponseBody).as[GetEmploymentPensionsModel]

        stubGetWithResponseBody(employmentsUrl,
          OK, expectedResponseBody, headersSentToBenefits)

        val result = await(connector.getEmploymentPensions(nino, taxYear)(hc))

        result mustBe Right(Some(expectedResult))
      }
    }

    "return a GetEmploymentPensionsModel" when {

      "nino and tax year are present" in {
        val expectedResult = Json.parse(expectedResponseBody).as[GetEmploymentPensionsModel]

        stubGetWithResponseBody(employmentsUrl, OK, expectedResponseBody)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.getEmploymentPensions(nino, taxYear)(hc)).right.get

        result mustBe Some(expectedResult)
      }
    }

    "return a Right None when NOT_FOUND" in {
      stubGetWithResponseBody(employmentsUrl, NOT_FOUND, "")
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentPensions(nino, taxYear)(hc))

      result mustBe Right(None)
    }

    "return a Parsing error INTERNAL_SERVER_ERROR response" in {
      val invalidJson = Json.obj("stateBenefits" -> Some(true))

      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

      stubGetWithResponseBody(employmentsUrl, OK, invalidJson.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentPensions(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a SERVICE_UNAVAILABLE" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Dependent systems are currently not responding.")
      val expectedResult = DesErrorModel(SERVICE_UNAVAILABLE, DesErrorBodyModel.serviceUnavailable)

      stubGetWithResponseBody(employmentsUrl
        , SERVICE_UNAVAILABLE, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentPensions(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a BAD_REQUEST" in {
      val responseBody = Json.obj(
        "code" -> "INVALID_NINO",
        "reason" -> "Nino is invalid"
      )
      val expectedResult = DesErrorModel(BAD_REQUEST, DesErrorBodyModel("INVALID_NINO", "Nino is invalid"))

      stubGetWithResponseBody(employmentsUrl
        , BAD_REQUEST, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentPensions(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return an INTERNAL_SERVER_ERROR" in {
      val responseBody = Json.obj(
        "code" -> "SERVER_ERROR",
        "reason" -> "DES is currently experiencing problems that require live service intervention."
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.serverError)

      stubGetWithResponseBody(employmentsUrl
        , INTERNAL_SERVER_ERROR, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentPensions(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a INTERNAL_SERVER_ERROR  when DES throws an unexpected result" in {
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

      stubGetWithoutResponseBody(employmentsUrl
        , NO_CONTENT)
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getEmploymentPensions(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }
  }

}

object GetEmploymentConnectorISpec {
  val expectedResponseBody: String = {
    """
      |{
      |  "hmrcEmploymentData":[
      |    {
      |      "employmentId":"1234567890",
      |      "pensionSchemeName":"HMRC pensions scheme",
      |      "pensionSchemeRef":"Some HMRC ref",
      |      "amount":"127000",
      |      "taxAmount":"450",
      |      "occPen":true
      |    },
      |    {
      |      "employmentId":"1234567890",
      |      "pensionSchemeName":"Extra pensions scheme",
      |      "pensionSchemeRef":"Some HMRC ref",
      |      "amount":"127000",
      |      "taxAmount":"450"
      |    }
      |  ],
      |  "customerEmploymentData":[
      |    {
      |      "employmentId":"1234567890",
      |      "pensionSchemeName":"Customer pension scheme",
      |      "pensionSchemeRef":"Some customer ref",
      |      "amount":"129000",
      |      "taxAmount":"470"
      |    }
      |  ],
      |  "customerExpenses": {
      |      "submittedOn":"someDate"
      |  }
      |}
      |""".stripMargin
  }
}
