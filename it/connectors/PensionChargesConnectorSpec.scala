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
import connectors.PensionChargesConnectorSpec.expectedResponseBody
import helpers.WiremockSpec
import models.{DesErrorBodyModel, DesErrorModel, GetPensionChargesRequestModel}
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.DESTaxYearHelper.desTaxYearConverter

class PensionChargesConnectorSpec extends WiremockSpec {

  lazy val connector: PensionChargesConnector = app.injector.instanceOf[PensionChargesConnector]

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  def appConfig(desHost: String): AppConfig = new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override val desBaseUrl: String = s"http://$desHost:$wireMockPort"
  }

  val nino: String = "123456789"
  val taxYear: Int = 1999
  val desUrl: String = s"/income-tax/charges/pensions/$nino/${desTaxYearConverter(taxYear)}"

  ".getPensionCharges" should {

    "include internal headers" when {
      val headersSentToDes = Seq(
        new HttpHeader(HeaderNames.authorisation, "Bearer secret"),
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      val internalHost = "localhost"
      val externalHost = "127.0.0.1"

      "the host for DES is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new PensionChargesConnector(httpClient, appConfig(internalHost))
        val expectedResult = Json.parse(expectedResponseBody).as[GetPensionChargesRequestModel]

        stubGetWithResponseBody(desUrl,
          OK, expectedResponseBody, headersSentToDes)

        val result = await(connector.getPensionCharges(nino, taxYear)(hc))

        result mustBe Right(expectedResult)
      }

      "the host for DES is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new PensionChargesConnector(httpClient, appConfig(externalHost))
        val expectedResult = Json.parse(expectedResponseBody).as[GetPensionChargesRequestModel]

        stubGetWithResponseBody(desUrl,
          OK, expectedResponseBody, headersSentToDes)

        val result = await(connector.getPensionCharges(nino, taxYear)(hc))

        result mustBe Right(expectedResult)
      }
    }

    "return a GetPensionChargesRequestModel" when {
      "nino and taxYear are present" in {
        val expectedResult = Json.parse(expectedResponseBody).as[GetPensionChargesRequestModel]
        stubGetWithResponseBody(desUrl, OK, expectedResponseBody)

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.getPensionCharges(nino, taxYear)(hc)).right.get

        result mustBe expectedResult
      }

    }

    "return a Parsing error INTERNAL_SERVER_ERROR response" in {
      val invalidJson = Json.obj(
        "source" -> true
      )

      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

      stubGetWithResponseBody(desUrl, OK, invalidJson.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getPensionCharges(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a NO_CONTENT" in {
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

      stubGetWithResponseBody(desUrl, NO_CONTENT, "{}")
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getPensionCharges(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a general Bad Request" in {
      val responseBody = Json.obj(
        "code" -> "INVALID_TAX_YEAR",
        "reason" -> "Submission has not passed validation. Invalid parameter taxYear."
      )
      val expectedResult = DesErrorModel(BAD_REQUEST,
        DesErrorBodyModel("INVALID_TAX_YEAR", "Submission has not passed validation. Invalid parameter taxYear."))

      stubGetWithResponseBody(desUrl, BAD_REQUEST, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getPensionCharges(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Not found" in {
      val responseBody = Json.obj(
        "code" -> "NO_DATA_FOUND",
        "reason" -> "The remote endpoint has indicated that no data can be found."
      )
      val expectedResult = DesErrorModel(NOT_FOUND,
        DesErrorBodyModel("NO_DATA_FOUND", "The remote endpoint has indicated that no data can be found."))

      stubGetWithResponseBody(desUrl, NOT_FOUND, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getPensionCharges(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal server error" in {
      val responseBody = Json.obj(
        "code" -> "SERVER_ERROR",
        "reason" -> "DES is currently experiencing problems that require live service intervention."
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("SERVER_ERROR",
        "DES is currently experiencing problems that require live service intervention."))

      stubGetWithResponseBody(desUrl, INTERNAL_SERVER_ERROR, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getPensionCharges(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return a Service Unavailable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Dependent systems are currently not responding."
      )
      val expectedResult = DesErrorModel(SERVICE_UNAVAILABLE, DesErrorBodyModel("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding."))

      stubGetWithResponseBody(s"/income-tax/charges/pensions/$nino/${desTaxYearConverter(taxYear)}", SERVICE_UNAVAILABLE, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getPensionCharges(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when DES throws an unexpected result" in {
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

      stubGetWithoutResponseBody(desUrl, NO_CONTENT)
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getPensionCharges(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when DES throws an unexpected result that is parsable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE",
        "reason" -> "Dependent systems are currently not responding."
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR,
        DesErrorBodyModel("SERVICE_UNAVAILABLE", "Dependent systems are currently not responding."))

      stubGetWithResponseBody(desUrl, CONFLICT, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getPensionCharges(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }

    "return an Internal Server Error when DES throws an unexpected result that isn't parsable" in {
      val responseBody = Json.obj(
        "code" -> "SERVICE_UNAVAILABLE"
      )
      val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

      stubGetWithResponseBody(desUrl, CONFLICT, responseBody.toString())
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val result = await(connector.getPensionCharges(nino, taxYear)(hc))

      result mustBe Left(expectedResult)
    }
  }

  ".deletePensionCharges " should {

    "include internal headers" when {
      val headersSentToDes = Seq(
        new HttpHeader(HeaderNames.authorisation, "Bearer secret"),
        new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
      )

      val internalHost = "localhost"
      val externalHost = "127.0.0.1"

      "the host for DES is 'Internal'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new PensionChargesConnector(httpClient, appConfig(internalHost))

        stubDeleteWithoutResponseBody(desUrl, NO_CONTENT, headersSentToDes)

        val result = await(connector.deletePensionCharges(nino, taxYear)(hc))

        result mustBe Right(())
      }

      "the host for DES is 'External'" in {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
        val connector = new PensionChargesConnector(httpClient, appConfig(externalHost))

        stubDeleteWithoutResponseBody(desUrl, NO_CONTENT, headersSentToDes)

        val result = await(connector.deletePensionCharges(nino, taxYear)(hc))

        result mustBe Right(())
      }
    }


    "handle error" when {

      val desErrorBodyModel = DesErrorBodyModel("DES_CODE", "DES_REASON")

      Seq(INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE, NOT_FOUND, BAD_REQUEST).foreach { status =>

        s"Des returns $status" in {
          val desError = DesErrorModel(status, desErrorBodyModel)
          implicit val hc: HeaderCarrier = HeaderCarrier()

          stubDeleteWithResponseBody(desUrl, status, desError.toJson.toString())

          val result = await(connector.deletePensionCharges(nino, taxYear)(hc))

          result mustBe Left(desError)
        }
      }

      "DES returns an unexpected error code - 502 BadGateway" in {
        val desError = DesErrorModel(INTERNAL_SERVER_ERROR, desErrorBodyModel)
        implicit val hc: HeaderCarrier = HeaderCarrier()

        stubDeleteWithResponseBody(desUrl, BAD_GATEWAY, desError.toJson.toString())

        val result = await(connector.deletePensionCharges(nino, taxYear)(hc))

        result mustBe Left(desError)
      }

    }

  }

}

object PensionChargesConnectorSpec {
  val expectedResponseBody: String =
    """
      {
      | "submittedOn": "2020-07-27T17:00:19Z",
      |	"pensionSavingsTaxCharges": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
      |		],
      |		"lumpSumBenefitTakenInExcessOfLifetimeAllowance": {
      |			"amount": 123.45,
      |			"taxPaid": 12.45
      |		},
      |		"benefitInExcessOfLifetimeAllowance": {
      |			"amount": 123.45,
      |			"taxPaid": 12.34
      |		},
      |		"isAnnualAllowanceReduced": true,
      |		"taperedAnnualAllowance": true,
      |		"moneyPurchasedAllowance": false
      |	},
      |	"pensionSchemeOverseasTransfers": {
      |		"overseasSchemeProvider": [{
      |			"providerName": "Overseas Pensions Plc",
      |			"providerAddress": "111 Some Street, Some Town, Some Place",
      |			"providerCountryCode": "ESP",
      |			"qualifyingRecognisedOverseasPensionScheme": [
      |				"Q123456"
      |			]
      |		}],
      |		"transferCharge": 123.45,
      |		"transferChargeTaxPaid": 0
      |	},
      |	"pensionSchemeUnauthorisedPayments": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
      |		],
      |		"surcharge": {
      |			"amount": 123.45,
      |			"foreignTaxPaid": 123.45
      |		},
      |		"noSurcharge": {
      |			"amount": 123.45,
      |			"foreignTaxPaid": 123.45
      |		}
      |	},
      |	"pensionContributions": {
      |		"pensionSchemeTaxReference": [
      |			"00123456RA"
      |		],
      |		"inExcessOfTheAnnualAllowance": 123.45,
      |		"annualAllowanceTaxPaid": 123.45
      |	},
      |	"overseasPensionContributions": {
      |		"overseasSchemeProvider": [{
      |			"providerName": "Overseas Pensions Plc",
      |			"providerAddress": "112 Some Street, Some Town, Some Place",
      |			"providerCountryCode": "ESP",
      |			"pensionSchemeTaxReference": [
      |				"00123456RA"
      |			]
      |		}],
      |		"shortServiceRefund": 123.45,
      |		"shortServiceRefundTaxPaid": 0
      |	}
      |}""".stripMargin
}
