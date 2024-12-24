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
import connectors.StateBenefitsConnectorISpec.expectedResponseBody
import helpers.WiremockSpec
import models.AllStateBenefitsData
import models.error.ServiceError.DownstreamError
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import testdata.common._
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.CustomerAddedStateBenefitsDataBuilder.aCustomerAddedStateBenefitsDataJsValue
import utils.StateBenefitsDataBuilder.aStateBenefitsDataJsValue

class StateBenefitsConnectorISpec extends WiremockSpec {
  val connector: StateBenefitsConnector = app.injector.instanceOf[StateBenefitsConnector]
  val httpClient: HttpClient            = app.injector.instanceOf[HttpClient]
  implicit val hc: HeaderCarrier        = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))

  def appConfig(host: String): AppConfig =
    new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
      override val stateBenefitsBaseUrl: String = s"http://$host:$wireMockPort"
    }

  val stateBenefitsUrl = s"/income-tax-state-benefits/benefits/nino/$nino/tax-year/$taxYear"

  "getStateBenefits" should {

    "include internal headers" when {
      val headersSentToBenefits = Seq(
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      val internalHost = "localhost"
      val externalHost = "127.0.0.1"

      for ((intExtHost, intExt) <- Seq((internalHost, "Internal"), (externalHost, "External")))
        s"the host for DES is '$intExt'" in {
          val connector      = new StateBenefitsConnectorImpl(httpClient, appConfig(intExtHost))
          val expectedResult = Json.parse(expectedResponseBody.toString()).as[AllStateBenefitsData]

          stubGetWithResponseBody(stateBenefitsUrl, OK, expectedResponseBody.toString(), headersSentToBenefits)

          val result = await(connector.getStateBenefits(nino, taxYear)(hc).value)

          result mustBe Right(Some(expectedResult))
        }
    }

    "return a AllStateBenefitsData" in {
      val expectedResult = Json.parse(expectedResponseBody.toString()).as[AllStateBenefitsData]
      stubGetWithResponseBody(stateBenefitsUrl, OK, expectedResponseBody.toString())

      val result = await(connector.getStateBenefits(nino, taxYear)(hc).value).toOption.get

      result mustBe Some(expectedResult)
    }

    "return a Right None when NOT_FOUND" in {
      stubGetWithoutResponseBody(stateBenefitsUrl, NOT_FOUND)

      val result = await(connector.getStateBenefits(nino, taxYear)(hc).value)

      result mustBe Right(None)
    }

    "return a Right None when NO_CONTENT" in {
      stubGetWithoutResponseBody(stateBenefitsUrl, NO_CONTENT)

      val result = await(connector.getStateBenefits(nino, taxYear)(hc).value)

      result mustBe Right(None)
    }

    "return a SERVICE_UNAVAILABLE" in {
      val responseBody   = Json.obj("code" -> "SERVICE_UNAVAILABLE", "reason" -> "Dependent systems are currently not responding.")
      val expectedResult = DownstreamError("APIErrorBodyModel(SERVICE_UNAVAILABLE,Dependent systems are currently not responding.)")

      stubGetWithResponseBody(stateBenefitsUrl, SERVICE_UNAVAILABLE, responseBody.toString())
      val result = await(connector.getStateBenefits(nino, taxYear)(hc).value)

      result mustBe Left(expectedResult)
    }

    "return a BAD_REQUEST" in {
      val responseBody = Json.obj(
        "code"   -> "INVALID_NINO",
        "reason" -> "Nino is invalid"
      )
      val expectedResult = DownstreamError("APIErrorBodyModel(INVALID_NINO,Nino is invalid)")

      stubGetWithResponseBody(stateBenefitsUrl, BAD_REQUEST, responseBody.toString())
      val result = await(connector.getStateBenefits(nino, taxYear)(hc).value)

      result mustBe Left(expectedResult)
    }

    "return an INTERNAL_SERVER_ERROR" in {
      val responseBody = Json.obj(
        "code"   -> "SERVER_ERROR",
        "reason" -> "DES is currently experiencing problems that require live service intervention."
      )
      val expectedResult =
        DownstreamError("APIErrorBodyModel(SERVER_ERROR,DES is currently experiencing problems that require live service intervention.)")

      stubGetWithResponseBody(stateBenefitsUrl, INTERNAL_SERVER_ERROR, responseBody.toString())
      val result = await(connector.getStateBenefits(nino, taxYear)(hc).value)

      result mustBe Left(expectedResult)
    }

    "return an GATEWAY_TIMEOUT" in {

      val responseBody = Json.obj(
        "code"   -> "SERVER_ERROR",
        "reason" -> "DES is currently experiencing problems that require live service intervention."
      )
      val expectedResult =
        DownstreamError("APIErrorBodyModel(SERVER_ERROR,DES is currently experiencing problems that require live service intervention.)")

      stubGetWithResponseBody(stateBenefitsUrl, GATEWAY_TIMEOUT, responseBody.toString())
      val result = await(connector.getStateBenefits(nino, taxYear)(hc).value)

      result mustBe Left(expectedResult)
    }
  }

  // TODO LT Add more tests, refactor above to Table, change the model to case classes and sealed traits

}

object StateBenefitsConnectorISpec {

  val expectedResponseBody: JsObject = JsObject(
    Seq(
      "stateBenefits"              -> aStateBenefitsDataJsValue,
      "customerAddedStateBenefits" -> aCustomerAddedStateBenefitsDataJsValue
    ))
}
