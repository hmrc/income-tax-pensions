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
import connectors.PensionReliefsConnectorISpec.expectedResponseBody
import helpers.WiremockSpec
import models._
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.TaxYearHelper.{ifTysTaxYearConverter, taxYearConverter}

class PensionReliefsConnectorISpec extends WiremockSpec {

  lazy val connector: PensionReliefsConnector = app.injector.instanceOf[PensionReliefsConnector]
  lazy val httpClient: HttpClientV2             = app.injector.instanceOf[HttpClientV2]

  def appConfig(desIfHost: String): AppConfig =
    new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
      override val desBaseUrl: String = s"http://$desIfHost:$wireMockPort"
      override val ifBaseUrl: String  = s"http://$desIfHost:$wireMockPort"
      override val hipBaseUrl: String = s"http://$desIfHost:$wireMockPort"
    }

  val nino: String                = "123456789"
  val (nonTysTaxYear, tysTaxYear) = (2023, 2024)
  val desUrl                      = s"/income-tax/reliefs/pensions/$nino/${taxYearConverter(nonTysTaxYear)}"
  val ifTysUrl                    = s"/income-tax/reliefs/pensions/${ifTysTaxYearConverter(tysTaxYear)}/$nino"
  val hipUrl                      = s"/income-tax/v1/reliefs/pensions/$nino/${taxYearConverter(nonTysTaxYear)}"

  val minimumPensionReliefs: PensionReliefs = PensionReliefs(regularPensionContributions = Some(10.22), None, None, None, None)

  val fullPensionReliefs: PensionReliefs = PensionReliefs(
    regularPensionContributions = Some(10.22),
    oneOffPensionContributionsPaid = Some(11.33),
    retirementAnnuityPayments = Some(12.44),
    paymentToEmployersSchemeNoTaxRelief = Some(13.55),
    overseasPensionSchemeContributions = Some(14.66)
  )

  val fullCreateOrUpdatePensionReliefsData: CreateOrUpdatePensionReliefsModel = CreateOrUpdatePensionReliefsModel(fullPensionReliefs)
  val minEmploymentFinancialData: CreateOrUpdatePensionReliefsModel           = CreateOrUpdatePensionReliefsModel(minimumPensionReliefs)
  val fullCreateOrUpdatePensionReliefsJsonBody: String                        = Json.toJson(fullCreateOrUpdatePensionReliefsData).toString()
  val minEmploymentFinancialDataJsonBody: String                              = Json.toJson(minEmploymentFinancialData).toString()

  for ((taxYear, url) <- Seq((tysTaxYear, ifTysUrl), (nonTysTaxYear, hipUrl))) {
    lazy val externalHost = "127.0.0.1"
    val connector         = new PensionReliefsConnector(httpClient, appConfig(externalHost))
    s".GetPensionReliefsConnector - $taxYear" should {
      "include internal headers" when {

        val headersSentToDes = Seq(
          new HttpHeader(HeaderNames.authorisation, "Bearer secret"),
          new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
        )

        lazy val internalHost = "localhost"

        "the host for DES is 'Internal'" in {
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
          val connector                  = new PensionReliefsConnector(httpClient, appConfig(internalHost))
          val expectedResult             = Json.parse(expectedResponseBody).as[GetPensionReliefsModel]

          stubGetWithResponseBody(url, OK, expectedResponseBody, headersSentToDes)

          val result = await(connector.getPensionReliefs(nino, taxYear)(hc))

          result mustBe Right(Some(expectedResult))
        }

        "the host for DES is 'External'" in {
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
          val expectedResult             = Json.parse(expectedResponseBody).as[GetPensionReliefsModel]

          stubGetWithResponseBody(url, OK, expectedResponseBody, headersSentToDes)

          val result = await(connector.getPensionReliefs(nino, taxYear)(hc))

          result mustBe Right(Some(expectedResult))
        }
      }

      "return a GetPensionReliefsModel when nino and taxYear are present" in {
        val expectedResult = Json.parse(expectedResponseBody).as[GetPensionReliefsModel]
        stubGetWithResponseBody(url, OK, expectedResponseBody)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result                     = await(connector.getPensionReliefs(nino, taxYear)(hc)).toOption.get

        result mustBe Some(expectedResult)
      }

      "return a Parsing error INTERNAL_SERVER_ERROR response" in {
        lazy val invalidJson = Json.obj("submittedOn" -> true)

        val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

        stubGetWithResponseBody(url, OK, invalidJson.toString())
        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result                     = await(connector.getPensionReliefs(nino, taxYear)(hc))

        result mustBe Left(expectedResult)
      }

      "return a SERVICE_UNAVAILABLE" in {
        val responseBody = Json.obj("code" -> "SERVICE_UNAVAILABLE", "reason" -> "Dependent systems are currently not responding.")
        val expectedResult =
          DesErrorModel(SERVICE_UNAVAILABLE, DesErrorBodyModel("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding."))

        stubGetWithResponseBody(url, SERVICE_UNAVAILABLE, responseBody.toString())
        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result                     = await(connector.getPensionReliefs(nino, taxYear)(hc))

        result mustBe Left(expectedResult)
      }

      "return a BAD_REQUEST" in {
        val responseBody = Json.obj(
          "code"   -> "INVALID_NINO",
          "reason" -> "Nino is invalid"
        )
        val expectedResult = DesErrorModel(BAD_REQUEST, DesErrorBodyModel("INVALID_NINO", "Nino is invalid"))

        stubGetWithResponseBody(url, BAD_REQUEST, responseBody.toString())
        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result                     = await(connector.getPensionReliefs(nino, taxYear)(hc))

        result mustBe Left(expectedResult)
      }

      "return a NOT_FOUND" in {

        stubGetWithResponseBody(url, NOT_FOUND, "")
        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result                     = await(connector.getPensionReliefs(nino, taxYear)(hc))

        result mustBe Right(None)
      }

      "return an INTERNAL_SERVER_ERROR" in {
        val responseBody = Json.obj(
          "code"   -> "SERVER_ERROR",
          "reason" -> "DES is currently experiencing problems that require live service intervention."
        )
        val expectedResult = DesErrorModel(
          INTERNAL_SERVER_ERROR,
          DesErrorBodyModel("SERVER_ERROR", "DES is currently experiencing problems that require live service intervention."))

        stubGetWithResponseBody(url, INTERNAL_SERVER_ERROR, responseBody.toString())
        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result                     = await(connector.getPensionReliefs(nino, taxYear)(hc))

        result mustBe Left(expectedResult)
      }

      "return a INTERNAL_SERVER_ERROR  when DES throws an unexpected result" in {
        val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

        stubGetWithoutResponseBody(url, NO_CONTENT)
        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result                     = await(connector.getPensionReliefs(nino, taxYear)(hc))

        result mustBe Left(expectedResult)
      }
    }
  }
  for ((taxYear, desIfUrl) <- Seq((nonTysTaxYear, desUrl), (tysTaxYear, ifTysUrl))) {

    s".deletePensionReliefs - $taxYear" should {

      "include internal headers" when {
        val headersSentToDes = Seq(
          new HttpHeader(HeaderNames.authorisation, "Bearer secret"),
          new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
        )

        val internalHost = "localhost"
        val externalHost = "127.0.0.1"

        "the host for DES is 'Internal'" in {
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
          val connector                  = new PensionReliefsConnector(httpClient, appConfig(internalHost))

          stubDeleteWithoutResponseBody(desIfUrl, NO_CONTENT, headersSentToDes)

          val result = await(connector.deletePensionReliefs(nino, taxYear)(hc))

          result mustBe Right(())
        }

        "the host for DES is 'External'" in {
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
          val connector                  = new PensionReliefsConnector(httpClient, appConfig(externalHost))

          stubDeleteWithoutResponseBody(desIfUrl, NO_CONTENT, headersSentToDes)

          val result = await(connector.deletePensionReliefs(nino, taxYear)(hc))

          result mustBe Right(())
        }
      }

      "handle error" when {

        val desErrorBodyModel = DesErrorBodyModel("DES_CODE", "DES_REASON")

        Seq(INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE, BAD_REQUEST).foreach { status =>
          s"Des returns $status" in {
            val desError                   = DesErrorModel(status, desErrorBodyModel)
            implicit val hc: HeaderCarrier = HeaderCarrier()

            stubDeleteWithResponseBody(desIfUrl, status, desError.toJson.toString())

            val result = await(connector.deletePensionReliefs(nino, taxYear)(hc))

            result mustBe Left(desError)
          }
        }

        "DES returns an unexpected error code - 502 BadGateway" in {
          val desError                   = DesErrorModel(INTERNAL_SERVER_ERROR, desErrorBodyModel)
          implicit val hc: HeaderCarrier = HeaderCarrier()

          stubDeleteWithResponseBody(desIfUrl, BAD_GATEWAY, desError.toJson.toString())

          val result = await(connector.deletePensionReliefs(nino, taxYear)(hc))

          result mustBe Left(desError)
        }

      }

    }

    s".createOrAmendPensionReliefs - $taxYear" should {

      "include internal headers" when {
        val headersSentToDes = Seq(
          new HttpHeader(HeaderNames.authorisation, "Bearer secret"),
          new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
        )

        val internalHost = "localhost"
        val externalHost = "127.0.0.1"

        "the host for DES is 'Internal'" in {
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
          val connector                  = new PensionReliefsConnector(httpClient, appConfig(internalHost))

          stubPutWithoutResponseBody(desIfUrl, fullCreateOrUpdatePensionReliefsJsonBody, NO_CONTENT, headersSentToDes)

          val result = await(connector.createOrAmendPensionReliefs(nino, taxYear, fullCreateOrUpdatePensionReliefsData)(hc))

          result mustBe Right(())
        }

        "the host for DES is 'External'" in {
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
          val connector                  = new PensionReliefsConnector(httpClient, appConfig(externalHost))

          stubPutWithoutResponseBody(desIfUrl, fullCreateOrUpdatePensionReliefsJsonBody, NO_CONTENT, headersSentToDes)

          val result = await(connector.createOrAmendPensionReliefs(nino, taxYear, fullCreateOrUpdatePensionReliefsData)(hc))

          result mustBe Right(())
        }
      }

      "return a Right(())" when {
        "request body is a full valid pension reliefs model" in {

          stubPutWithoutResponseBody(desIfUrl, fullCreateOrUpdatePensionReliefsJsonBody, NO_CONTENT)

          implicit val hc: HeaderCarrier = HeaderCarrier()
          val result                     = await(connector.createOrAmendPensionReliefs(nino, taxYear, fullCreateOrUpdatePensionReliefsData)(hc))

          result mustBe Right(())
        }

        "request body is a minimum valid pension reliefs model" in {

          stubPutWithoutResponseBody(desIfUrl, minEmploymentFinancialDataJsonBody, NO_CONTENT)

          implicit val hc: HeaderCarrier = HeaderCarrier()
          val result                     = await(connector.createOrAmendPensionReliefs(nino, taxYear, minEmploymentFinancialData)(hc))

          result mustBe Right(())
        }
      }

      "return Left(error)" when {

        Seq(BAD_REQUEST, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { httpErrorStatus =>
          s"DES returns $httpErrorStatus response that has a parsable error body" in {
            val responseBody = Json.obj(
              "code"   -> "SOME_DES_ERROR_CODE",
              "reason" -> "SOME_DES_ERROR_REASON"
            )
            val expectedResult = DesErrorModel(httpErrorStatus, DesErrorBodyModel("SOME_DES_ERROR_CODE", "SOME_DES_ERROR_REASON"))

            stubPutWithResponseBody(desIfUrl, fullCreateOrUpdatePensionReliefsJsonBody, responseBody.toString(), httpErrorStatus)
            implicit val hc: HeaderCarrier = HeaderCarrier()
            val result                     = await(connector.createOrAmendPensionReliefs(nino, taxYear, fullCreateOrUpdatePensionReliefsData)(hc))

            result mustBe Left(expectedResult)
          }

          s"DES returns $httpErrorStatus response that does not have a parsable error body" in {
            val expectedResult = DesErrorModel(httpErrorStatus, DesErrorBodyModel.parsingError)

            stubPutWithResponseBody(desIfUrl, fullCreateOrUpdatePensionReliefsJsonBody, "UNEXPECTED RESPONSE BODY", httpErrorStatus)

            implicit val hc: HeaderCarrier = HeaderCarrier()
            val result                     = await(connector.createOrAmendPensionReliefs(nino, taxYear, fullCreateOrUpdatePensionReliefsData)(hc))

            result mustBe Left(expectedResult)
          }

        }

        "DES returns an unexpected http response that is parsable" in {

          val responseBody = Json.obj(
            "code"   -> "BAD_GATEWAY",
            "reason" -> "bad gateway"
          )
          val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("BAD_GATEWAY", "bad gateway"))

          stubPutWithResponseBody(desIfUrl, fullCreateOrUpdatePensionReliefsJsonBody, responseBody.toString(), BAD_GATEWAY)
          implicit val hc: HeaderCarrier = HeaderCarrier()
          val result                     = await(connector.createOrAmendPensionReliefs(nino, taxYear, fullCreateOrUpdatePensionReliefsData)(hc))

          result mustBe Left(expectedResult)
        }

        "DES returns an unexpected http response that is not parsable" in {
          val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

          stubPutWithResponseBody(desIfUrl, fullCreateOrUpdatePensionReliefsJsonBody, "Bad Gateway", BAD_GATEWAY)
          implicit val hc: HeaderCarrier = HeaderCarrier()
          val result                     = await(connector.createOrAmendPensionReliefs(nino, taxYear, fullCreateOrUpdatePensionReliefsData)(hc))

          result mustBe Left(expectedResult)
        }
      }
    }
  }
}

object PensionReliefsConnectorISpec {
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
