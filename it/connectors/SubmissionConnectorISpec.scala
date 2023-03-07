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
import models._
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class SubmissionConnectorISpec extends WiremockSpec {

  lazy val connector: SubmissionConnector = app.injector.instanceOf[SubmissionConnector]

  lazy val httpClient: HttpClient = app.injector.instanceOf[HttpClient]

  def appConfig(desHost: String): AppConfig = new BackendAppConfig(app.injector.instanceOf[Configuration], app.injector.instanceOf[ServicesConfig]) {
    override val submissionBaseUrl: String = s"http://$desHost:$wireMockPort"
  }

  val nino: String = "123456789"
  val taxYear: Int = 1999
  val taxYearEOY: Int = 2000
  val desUrl: String = s"/income-tax-submission-service/income-tax/nino/$nino/sources/session\\?taxYear=$taxYear"
  val mtditid = "1234567"


  ".refreshStateBenefits" should {


    implicit val hc: HeaderCarrier = HeaderCarrier()
    val headersSentToDes = Seq(
      new HttpHeader("mtditid", mtditid),
    )


    "succeed when correct parameters are passed" in {
      val jsValue = Json.toJson(RefreshIncomeSourceRequest("pensions"))

      stubPutWithoutResponseBody(desUrl, jsValue.toString(), NO_CONTENT, headersSentToDes)

      await(connector.refreshPensionsResponse(nino, mtditid, taxYear)(hc)) mustBe Right(())
    }

    "return a Left error" when {

      Seq(INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE, BAD_REQUEST).foreach { errorStatus =>

        val desResponseBody = Json.obj(
          "code" -> "SOME_DES_ERROR_CODE",
          "reason" -> "SOME_DES_ERROR_REASON"
        ).toString

        val jsValue = Json.toJson(RefreshIncomeSourceRequest("pensions"))


        s"API returns $errorStatus" in {
          val expectedResult = APIErrorModel(errorStatus, APIErrorBodyModel("SOME_DES_ERROR_CODE", "SOME_DES_ERROR_REASON"))


          stubPutWithResponseBody(desUrl, jsValue.toString(), desResponseBody, errorStatus)

          val result = await(connector.refreshPensionsResponse(nino, mtditid, taxYear)(hc))

          result mustBe Left(expectedResult)
        }

        s"API returns $errorStatus response that does not have a parsable error body" in {
          val expectedResult = APIErrorModel(errorStatus, APIErrorBodyModel("PARSING_ERROR", "Error parsing response from API"))

          stubPutWithResponseBody(desUrl, jsValue.toString(), "UNEXPECTED RESPONSE BODY", errorStatus)

          val result = await(connector.refreshPensionsResponse(nino, mtditid, taxYear)(hc))

          result mustBe Left(expectedResult)
        }
      }
    }
  }
}