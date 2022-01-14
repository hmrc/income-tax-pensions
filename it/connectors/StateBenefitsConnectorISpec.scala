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
import models.{DesErrorBodyModel, DesErrorModel}
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.DESTaxYearHelper.desTaxYearConverter

class StateBenefitsConnectorISpec extends WiremockSpec {

  lazy val connector: StateBenefitsConnector = app.injector.instanceOf[StateBenefitsConnector]
  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  def appConfig(desHost: String): AppConfig = new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override val desBaseUrl: String = s"http://$desHost:$wireMockPort"
  }

  val nino: String = "AA123123A"
  val taxYear: Int = 2021
  val benefitId: String = "a111111a-abcd-111a-123a-11a1a111a1"

  ".deleteOverrideStateBenefit" should {

    val deleteOverrideUrl: String = s"/income-tax/income/state-benefits/$nino/${desTaxYearConverter(taxYear)}/$benefitId"

    "include internal headers" when {
      val headersSentToDes = Seq(
        new HttpHeader(HeaderNames.authorisation, "Bearer secret"),
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      val internalHost = "localhost"
      val externalHost = "127.0.0.1"

      "the host for DES is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new StateBenefitsConnector(httpClient, appConfig(internalHost))

        stubDeleteWithoutResponseBody(deleteOverrideUrl,
          NO_CONTENT, headersSentToDes)

        val result = await(connector.deleteOverrideStateBenefit(nino, taxYear, benefitId)(hc))

        result mustBe Right(())
      }

      "the host for DES is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new StateBenefitsConnector(httpClient, appConfig(externalHost))

        stubDeleteWithoutResponseBody(deleteOverrideUrl,
          NO_CONTENT, headersSentToDes)

        val result = await(connector.deleteOverrideStateBenefit(nino, taxYear, benefitId)(hc))

        result mustBe Right(())
      }
    }

    "return a Right on success" in {
      stubDeleteWithoutResponseBody(deleteOverrideUrl, NO_CONTENT)

      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.deleteOverrideStateBenefit(nino, taxYear, benefitId)(hc))

      result mustBe Right(())
    }

    "handle a Left error" when {

      val desErrorBodyModel = DesErrorBodyModel("DES_CODE", "DES_REASON")

      Seq(INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE, NOT_FOUND, BAD_REQUEST).foreach { errorStatus =>

        s"DES returns expected error $errorStatus" in {
          val desError = DesErrorModel(errorStatus, desErrorBodyModel)
          implicit val hc: HeaderCarrier = HeaderCarrier()

          stubDeleteWithResponseBody(deleteOverrideUrl, errorStatus, desError.toJson.toString())

          val result = await(connector.deleteOverrideStateBenefit(nino, taxYear, benefitId)(hc))

          result mustBe Left(desError)
        }
      }

      "DES returns a non parsable response" in {
        val errorResponseBody = "a non parsable body"
        val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

        stubDeleteWithResponseBody(deleteOverrideUrl, INTERNAL_SERVER_ERROR, errorResponseBody)
        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.deleteOverrideStateBenefit(nino, taxYear, benefitId)(hc))

        result mustBe Left(expectedResult)
      }

      "DES returns an unexpected http response that is parsable" in {


        val errorResponseBody = Json.obj(
          "code" -> "SERVER_ERROR",
          "reason" -> "DES is currently experiencing problems that require live service intervention."
        )

        val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel(
          "SERVER_ERROR", "DES is currently experiencing problems that require live service intervention."))

        stubDeleteWithResponseBody(deleteOverrideUrl, BAD_GATEWAY, errorResponseBody.toString())
        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.deleteOverrideStateBenefit(nino, taxYear, benefitId)(hc))

        result mustBe Left(expectedResult)

      }
    }
  }
}
