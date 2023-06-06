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

package api

import api.PensionsChargesTestData.{GetPensionChargesDesResponseBody, createUpdatePensionChargesPayload, createUpdatePensionChargesRequest}
import com.github.tomakehurst.wiremock.http.HttpHeader
import helpers.WiremockSpec
import models.RefreshIncomeSourceRequest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import play.api.http.Status._
import play.api.libs.json.Json
import utils.TaxYearHelper.desIfTaxYearConverter

class SavePensionsChargesUserDataISpec extends WiremockSpec with ScalaFutures { //scalastyle:off magic.number
  
  trait Setup {
   
    implicit val patienceConfig: PatienceConfig = PatienceConfig(Span(10L, Seconds))
    
    val nino: String = "AA123123A"
    val taxYear: Int = 2021
    
    val mtditidHeader: (String, String) = ("mtditid", "555555555")
    val headersSentToDes = Seq(new HttpHeader(mtditidHeader._1, mtditidHeader._2))
    val requestHeaders: Seq[(String, String)] = Seq(mtditidHeader, ("Authorization", "Bearer:XYZ"))
    
    val desUrl: String = s"/income-tax/charges/pensions/$nino/${desIfTaxYearConverter(taxYear)}"
    val serviceUrl: String = s"/income-tax-pensions/pension-charges/session-data/nino/$nino/taxYear/$taxYear"
    val submissionUri: String = s"/income-tax-submission-service/income-tax/nino/$nino/sources/session\\?taxYear=$taxYear"
    
    def stubChargesGET():Unit = stubGetWithResponseBody(desUrl, OK, GetPensionChargesDesResponseBody)

    auditStubs()
  }

  "save pension charges user data" when {

    "the user is an individual" must {

      "return a No content(204) Success response" in new Setup {
        stubChargesGET()
        stubPutWithoutResponseBody(desUrl, Json.toJson(createUpdatePensionChargesRequest).toString(), NO_CONTENT)
        stubPutWithoutResponseBody(submissionUri, Json.toJson(RefreshIncomeSourceRequest("pensions")).toString(), NO_CONTENT, headersSentToDes)
        authorised()
        whenReady(buildClient(serviceUrl).withHttpHeaders(requestHeaders: _*).put(createUpdatePensionChargesPayload)) { _.status mustBe NO_CONTENT}
      }
      
      "return a status error when" should {
        for ( errorStatus <- Seq(INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE, BAD_REQUEST)) {
          val errorResponseBody: String  = Json.obj("code" -> errorStatus.toString,"reason" -> "SOME_DES_ERROR_REASON").toString

          s"a downstream $errorStatus error occurs on a pension charges PUT" in new Setup {
            stubChargesGET()
            stubPutWithResponseBody(desUrl, Json.toJson(createUpdatePensionChargesRequest).toString(), errorResponseBody, errorStatus)
            authorised()
            whenReady(buildClient(serviceUrl).withHttpHeaders(requestHeaders: _*).put(createUpdatePensionChargesPayload)) { result =>
              result.body mustBe errorResponseBody
              result.status mustBe errorStatus
            }
          }

          s"a downstream $errorStatus error occurs on a submission refresh PUT" in new Setup {
            stubChargesGET()
            stubPutWithoutResponseBody(desUrl, Json.toJson(createUpdatePensionChargesRequest).toString(), NO_CONTENT)
            stubPutWithResponseBody(submissionUri, Json.toJson(RefreshIncomeSourceRequest("pensions")).toString(), errorResponseBody, errorStatus)
            authorised()
            whenReady(buildClient(serviceUrl).withHttpHeaders(requestHeaders: _*).put(createUpdatePensionChargesPayload)) { result =>
              result.body mustBe errorResponseBody
              result.status mustBe errorStatus
            }
          }
        }
      }
    }
  }

}
