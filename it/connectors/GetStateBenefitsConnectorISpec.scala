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

package connectors

import com.github.tomakehurst.wiremock.http.HttpHeader
import config.{AppConfig, BackendAppConfig}
import helpers.WiremockSpec
import models.{AllStateBenefitsData, DesErrorBodyModel, DesErrorModel}
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import connectors.GetStateBenefitsConnectorISpec.expectedResponseBody
import utils.CustomerAddedStateBenefitsDataBuilder.aCustomerAddedStateBenefitsDataJsValue
import utils.StateBenefitsDataBuilder.aStateBenefitsDataJsValue

class GetStateBenefitsConnectorISpec extends WiremockSpec {

  lazy val connector: GetStateBenefitsConnector = app.injector.instanceOf[GetStateBenefitsConnector]

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  def appConfig(benefitsHost: String): AppConfig = new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override val stateBenefitsBaseUrl: String = s"http://$benefitsHost:$wireMockPort"
  }

  val nino: String = "123456789"
  val taxYear: Int = 2021
  val stateBenefitsUrl: String = s"/income-tax-state-benefits/benefits/nino/$nino/tax-year/$taxYear"

  ".getStateBenefits" should {

    "include internal headers" when {
      val headersSentToBenefits = Seq(
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      val internalHost = "localhost"
      val externalHost = "127.0.0.1"

      for ((intExtHost, intExt) <- Seq((internalHost, "Internal"), (externalHost, "External"))) {
        s"the host for DES is '$intExt'" in {
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
          val connector = new GetStateBenefitsConnector(httpClient, appConfig(intExtHost))
          val expectedResult = Json.parse(expectedResponseBody.toString()).as[AllStateBenefitsData]

          stubGetWithResponseBody(stateBenefitsUrl,
            OK, expectedResponseBody.toString(), headersSentToBenefits)

          val result = await(connector.getStateBenefits(nino, taxYear)(hc))

          result mustBe Right(Some(expectedResult))
        }
      }
    }

    "return a GetStateBenefitsModel" when {

      "nino and tax year are present" in {
        val expectedResult = Json.parse(expectedResponseBody.toString()).as[AllStateBenefitsData]

        stubGetWithResponseBody(stateBenefitsUrl, OK, expectedResponseBody.toString())

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.getStateBenefits(nino, taxYear)(hc)).toOption.get

        result mustBe Some(expectedResult)
      }
    }

    "return a Right None when NOT_FOUND" in {
      stubGetWithResponseBody(stateBenefitsUrl, NOT_FOUND, "")
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getStateBenefits(nino, taxYear)(hc))

      result mustBe Right(None)
    }

    "return a Right None when NO_CONTENT" in {
//      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

      stubGetWithoutResponseBody(stateBenefitsUrl
        , NO_CONTENT)
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getStateBenefits(nino, taxYear)(hc))

      result mustBe Right(None)
    }

    "return a SERVICE_UNAVAILABLE" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Dependent systems are currently not responding.")
      val expectedResult = DesErrorModel(SERVICE_UNAVAILABLE, DesErrorBodyModel.serviceUnavailable)

      stubGetWithResponseBody(stateBenefitsUrl
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

      stubGetWithResponseBody(stateBenefitsUrl
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

      stubGetWithResponseBody(stateBenefitsUrl
        , INTERNAL_SERVER_ERROR, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getStateBenefits(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return an GATEWAY_TIMEOUT" in {

      val responseBody = Json.obj(
        "code" -> "SERVER_ERROR",
        "reason" -> "DES is currently experiencing problems that require live service intervention."
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.serverError)

      stubGetWithResponseBody(stateBenefitsUrl
        , GATEWAY_TIMEOUT, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getStateBenefits(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }
  }

}

object GetStateBenefitsConnectorISpec {

  val expectedResponseBody: JsObject = JsObject(Seq(
    "stateBenefits" -> aStateBenefitsDataJsValue,
    "customerAddedStateBenefits" -> aCustomerAddedStateBenefitsDataJsValue
  ))
}
