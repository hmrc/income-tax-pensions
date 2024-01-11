/*
 * Copyright 2024 HM Revenue & Customs
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
import connectors.PensionIncomeConnectorISpec.expectedResponseBody
import connectors.httpParsers.GetPensionIncomeHttpParser.GetPensionIncomeResponse
import helpers.WiremockSpec
import models._
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.TaxYearHelper.{desIfTaxYearConverter, ifTysTaxYearConverter}

class PensionIncomeConnectorISpec extends WiremockSpec {

  lazy val connector: PensionIncomeConnector = app.injector.instanceOf[PensionIncomeConnector]
  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]


  def appConfig(desIfHost: String): AppConfig = new BackendAppConfig(
    app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override val desBaseUrl: String = s"http://$desIfHost:$wireMockPort"
    override val ifBaseUrl: String = s"http://$desIfHost:$wireMockPort"
  }

  val nino: String = "123456789"
  val (nonTysTaxYear, tysTaxYear) = (2023, 2024)
  val desUrl = s"/income-tax/income/pensions/$nino/${desIfTaxYearConverter(nonTysTaxYear)}"
  val ifTysUrl = s"/income-tax/income/pensions/${ifTysTaxYearConverter(tysTaxYear)}/$nino"

  val fullForeignPensionModel = Seq(
    ForeignPension(
      countryCode = "FRA",
      taxableAmount = 1999.99,
      amountBeforeTax = Some(1999.99),
      taxTakenOff = Some(1999.99),
      specialWithholdingTax = Some(1999.99),
      foreignTaxCreditRelief = Some(false)
    )
  )

  val minForeignPensionModel = Seq(
    ForeignPension(
      countryCode = "FRA",
      taxableAmount = 1999.99,
      amountBeforeTax = None,
      taxTakenOff = None,
      specialWithholdingTax = None,
      foreignTaxCreditRelief = None
    )
  )

  val fullOverseasPensionContributionModel = Seq(
    OverseasPensionContribution(
      customerReference = Some("PENSIONINCOME245"),
      exemptEmployersPensionContribs = 1999.99,
      migrantMemReliefQopsRefNo = Some("QOPS000000"),
      dblTaxationRelief = Some(1999.99),
      dblTaxationCountry = Some("FRA"),
      dblTaxationArticle = Some("AB3211-1"),
      dblTaxationTreaty = Some("Munich"),
      sf74Reference = Some("SF74-123456")
    )
  )
  val minOverseasPensionContributionModel = Seq(
    OverseasPensionContribution(
      customerReference = None,
      exemptEmployersPensionContribs = 1999.99,
      migrantMemReliefQopsRefNo = None,
      dblTaxationRelief = None,
      dblTaxationCountry = None,
      dblTaxationArticle = None,
      dblTaxationTreaty = None,
      sf74Reference = None
    )
  )

  val fullPensionIncomeModel: GetPensionIncomeModel =
    GetPensionIncomeModel(
      submittedOn = "2022-07-28T07:59:39.041Z",
      deletedOn = Some("2022-07-28T07:59:39.041Z"),
      foreignPension = Some(fullForeignPensionModel),
      overseasPensionContribution = Some(fullOverseasPensionContributionModel)
    )

  val fullCreateOrUpdatePensionIncomeData: CreateUpdatePensionIncomeModel =
    CreateUpdatePensionIncomeModel(Some(fullForeignPensionModel), Some(fullOverseasPensionContributionModel))
  val fullCreateOrUpdatePensionIncomeJsonBody: String = Json.toJson(fullCreateOrUpdatePensionIncomeData).toString()

  val minCreateOrUpdatePensionIncomeData: CreateUpdatePensionIncomeModel =
    CreateUpdatePensionIncomeModel(Some(minForeignPensionModel), Some(minOverseasPensionContributionModel))
  val minCreateOrUpdatePensionIncomeJsonBody: String = Json.toJson(minCreateOrUpdatePensionIncomeData).toString()

  implicit val hc: HeaderCarrier = HeaderCarrier()
  val headersSentToDes = Seq(
    new HttpHeader(HeaderNames.authorisation, "Bearer secret"),
    new HttpHeader(HeaderNames.xSessionId, "sessionIdValue")
  )

  for ((taxYear, desIfUrl) <- Seq((nonTysTaxYear, desUrl), (tysTaxYear, ifTysUrl))) {
    s".GetPensionIncomeConnector - $taxYear" should {
      "include internal headers" when {

        lazy val internalHost = "localhost"
        lazy val externalHost = "127.0.0.1"

        "the host for DES-IF is 'Internal'" in {
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
          val connector = new PensionIncomeConnector(httpClient, appConfig(internalHost))
          val expectedResult = Json.parse(expectedResponseBody).as[GetPensionIncomeModel]

          stubGetWithResponseBody(desIfUrl, OK, expectedResponseBody, headersSentToDes)
          auditStubs()

          val result = await(connector.getPensionIncome(nino, taxYear)(hc))

          result mustBe Right(Some(expectedResult))
        }

        "the host for DES-IF is 'External'" in {
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
          val connector = new PensionIncomeConnector(httpClient, appConfig(externalHost))
          val expectedResult = Json.parse(expectedResponseBody).as[GetPensionIncomeModel]

          stubGetWithResponseBody(desIfUrl, OK, expectedResponseBody, headersSentToDes)
          auditStubs()

          val result = await(connector.getPensionIncome(nino, taxYear)(hc))

          result mustBe Right(Some(expectedResult))
        }
      }
      "return a GetPensionIncomeModel when nino and taxYear are present" in {
        val expectedResult = Json.parse(expectedResponseBody).as[GetPensionIncomeModel]
        stubGetWithResponseBody(desIfUrl, OK, expectedResponseBody)
        auditStubs()

        implicit val hc: HeaderCarrier = HeaderCarrier()
        val result = await(connector.getPensionIncome(nino, taxYear)(hc)).toOption.get

        result mustBe Some(expectedResult)
      }

      "return a NOT_FOUND" in {
        stubGetWithResponseBody(desIfUrl, NOT_FOUND, "")
        implicit val hc: HeaderCarrier = HeaderCarrier()
        auditStubs()

        val result = await(connector.getPensionIncome(nino, taxYear)(hc))
        result mustBe Right(None)
      }


      "handle error" when {
        val desErrorBodyModel = DesErrorBodyModel("DES-IF_CODE", "DES-IF_REASON")

        def runErrorTest(status: Int, desError: DesErrorModel): GetPensionIncomeResponse = {
          stubGetWithResponseBody(desIfUrl, status, desError.toJson.toString())
          auditStubs()

          val result = await(connector.getPensionIncome(nino, taxYear)(hc))
          result
        }

        Seq(INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE, BAD_REQUEST).foreach { status =>
          s"Des returns $status" in {
            val desError: DesErrorModel = DesErrorModel(status, desErrorBodyModel)
            val result = runErrorTest(status, desError)
            auditStubs()

            result mustBe Left(desError)
          }
        }

        "return a INTERNAL_SERVER_ERROR  when DES-IF throws an unexpected result" in {
          val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

          stubGetWithoutResponseBody(desIfUrl, NO_CONTENT)
          auditStubs()

          val result = await(connector.getPensionIncome(nino, taxYear)(hc))
          result mustBe Left(expectedResult)
        }

        "return a Parsing error INTERNAL_SERVER_ERROR response" in {
          lazy val invalidJson = Json.obj("submittedOn" -> true)

          val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

          stubGetWithResponseBody(desIfUrl, OK, invalidJson.toString())
          auditStubs()

          val result = await(connector.getPensionIncome(nino, taxYear)(hc))
          result mustBe Left(expectedResult)
        }
      }
    }

    s".deletePensionIncome- $taxYear " should {

      "include internal headers" when {

        val internalHost = "localhost"
        val externalHost = "127.0.0.1"

        "the host for DES-IF is 'Internal'" in {
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
          val connector = new PensionIncomeConnector(httpClient, appConfig(internalHost))

          stubDeleteWithoutResponseBody(desIfUrl, NO_CONTENT, headersSentToDes)
          auditStubs()

          val result = await(connector.deletePensionIncome(nino, taxYear)(hc))
          result mustBe Right(())
        }

        "the host for DES-IF is 'External'" in {
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
          val connector = new PensionIncomeConnector(httpClient, appConfig(externalHost))

          stubDeleteWithoutResponseBody(desIfUrl, NO_CONTENT, headersSentToDes)
          auditStubs()

          val result = await(connector.deletePensionIncome(nino, taxYear)(hc))
          result mustBe Right(())
        }
      }


      "handle error" when {

        val desErrorBodyModel = DesErrorBodyModel("DES-IF_CODE", "DES-IF_REASON")

        Seq(INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE, BAD_REQUEST, NOT_FOUND).foreach { status =>

          s"Des returns $status" in {
            val desError = DesErrorModel(status, desErrorBodyModel)
            implicit val hc: HeaderCarrier = HeaderCarrier()

            stubDeleteWithResponseBody(desIfUrl, status, desError.toJson.toString())
            auditStubs()

            val result = await(connector.deletePensionIncome(nino, taxYear)(hc))
            result mustBe Left(desError)
          }
        }

        "DES-IF returns an unexpected error code - 502 BadGateway" in {
          val desError = DesErrorModel(INTERNAL_SERVER_ERROR, desErrorBodyModel)
          implicit val hc: HeaderCarrier = HeaderCarrier()

          stubDeleteWithResponseBody(desIfUrl, BAD_GATEWAY, desError.toJson.toString())
          auditStubs()

          val result = await(connector.deletePensionIncome(nino, taxYear)(hc))
          result mustBe Left(desError)
        }
      }
    }

    s".createOrAmendPensionIncome - $taxYear" should {

      "include internal headers" when {
        val internalHost = "localhost"
        val externalHost = "127.0.0.1"

        "the host for DES-IF is 'Internal'" in {
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
          val connector = new PensionIncomeConnector(httpClient, appConfig(internalHost))

          stubPutWithoutResponseBody(desIfUrl, fullCreateOrUpdatePensionIncomeJsonBody, NO_CONTENT, headersSentToDes)
          auditStubs()


          val result = await(connector.createOrAmendPensionIncome(nino, taxYear, fullCreateOrUpdatePensionIncomeData)(hc))

          result mustBe Right(())
        }

        "the host for DES-IF is 'External'" in {
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("sessionIdValue")))
          val connector = new PensionIncomeConnector(httpClient, appConfig(externalHost))

          stubPutWithoutResponseBody(desIfUrl, fullCreateOrUpdatePensionIncomeJsonBody, NO_CONTENT, headersSentToDes)
          auditStubs()

          val result = await(connector.createOrAmendPensionIncome(nino, taxYear, fullCreateOrUpdatePensionIncomeData)(hc))

          result mustBe Right(())
        }
      }

      "return a the expected object" when {
        "request body is a full valid pension income model" in {

          stubPutWithoutResponseBody(desIfUrl, fullCreateOrUpdatePensionIncomeJsonBody, NO_CONTENT)
          auditStubs()


          implicit val hc: HeaderCarrier = HeaderCarrier()
          val result = await(connector.createOrAmendPensionIncome(nino, taxYear, fullCreateOrUpdatePensionIncomeData)(hc))

          result mustBe Right(())
        }

        "request body is a minimum valid pension income model" in {

          stubPutWithoutResponseBody(desIfUrl, minCreateOrUpdatePensionIncomeJsonBody, NO_CONTENT)
          auditStubs()

          implicit val hc: HeaderCarrier = HeaderCarrier()
          val result = await(connector.createOrAmendPensionIncome(nino, taxYear, minCreateOrUpdatePensionIncomeData)(hc))

          result mustBe Right(())
        }
      }

      "return expected error" when {

        Seq(BAD_REQUEST, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE).foreach { httpErrorStatus =>

          s"DES-IF returns $httpErrorStatus response that has a parsable error body" in {
            val responseBody = Json.obj(
              "code" -> "SOME_DES-IF_ERROR_CODE",
              "reason" -> "SOME_DES-IF_ERROR_REASON"
            )
            val expectedResult = DesErrorModel(httpErrorStatus, DesErrorBodyModel("SOME_DES-IF_ERROR_CODE", "SOME_DES-IF_ERROR_REASON"))

            stubPutWithResponseBody(desIfUrl, fullCreateOrUpdatePensionIncomeJsonBody, responseBody.toString(), httpErrorStatus)
            auditStubs()

            val result = await(connector.createOrAmendPensionIncome(nino, taxYear, fullCreateOrUpdatePensionIncomeData)(hc))

            result mustBe Left(expectedResult)
          }

          s"DES-IF returns $httpErrorStatus response that does not have a parsable error body" in {
            val expectedResult = DesErrorModel(httpErrorStatus, DesErrorBodyModel.parsingError)

            stubPutWithResponseBody(desIfUrl, fullCreateOrUpdatePensionIncomeJsonBody,
              "UNEXPECTED RESPONSE BODY", httpErrorStatus)
            auditStubs()


            implicit val hc: HeaderCarrier = HeaderCarrier()
            val result = await(connector.createOrAmendPensionIncome(nino, taxYear, fullCreateOrUpdatePensionIncomeData)(hc))

            result mustBe Left(expectedResult)
          }

        }

        "DES-IF returns an unexpected http response that is parsable" in {

          val responseBody = Json.obj(
            "code" -> "BAD_GATEWAY",
            "reason" -> "bad gateway"
          )
          val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel("BAD_GATEWAY", "bad gateway"))

          stubPutWithResponseBody(desIfUrl, fullCreateOrUpdatePensionIncomeJsonBody, responseBody.toString(), BAD_GATEWAY)
          auditStubs()

          val result = await(connector.createOrAmendPensionIncome(nino, taxYear, fullCreateOrUpdatePensionIncomeData)(hc))
          result mustBe Left(expectedResult)
        }

        "DES-IF returns an unexpected http response that is not parsable" in {
          val expectedResult = DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError)

          stubPutWithResponseBody(desIfUrl, fullCreateOrUpdatePensionIncomeJsonBody, "Bad Gateway", BAD_GATEWAY)
          auditStubs()

          val result = await(connector.createOrAmendPensionIncome(nino, taxYear, fullCreateOrUpdatePensionIncomeData)(hc))
          result mustBe Left(expectedResult)
        }
      }
    }
  }
}

object PensionIncomeConnectorISpec {
  val expectedResponseBody: String =
    """
      | {
      |    "submittedOn": "2022-07-28T07:59:39.041Z",
      |    "deletedOn": "2022-07-28T07:59:39.041Z",
      |    "foreignPension": [
      |      {
      |        "countryCode": "FRA",
      |        "amountBeforeTax": 1999.99,
      |        "taxTakenOff": 1999.99,
      |        "specialWithholdingTax": 1999.99,
      |        "foreignTaxCreditRelief": false,
      |        "taxableAmount": 1999.99
      |      }
      |    ],
      |    "overseasPensionContribution": [
      |      {
      |        "customerReference": "PENSIONINCOME245",
      |        "exemptEmployersPensionContribs": 1999.99,
      |        "migrantMemReliefQopsRefNo": "QOPS000000",
      |        "dblTaxationRelief": 1999.99,
      |        "dblTaxationCountry": "FRA",
      |        "dblTaxationArticle": "AB3211-1",
      |        "dblTaxationTreaty": "Munich",
      |        "sf74Reference": "SF74-123456"
      |      }
      |    ]
      |  }
      |""".stripMargin
}
