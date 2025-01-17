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

import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId}
import config.BackendAppConfig
import helpers.WiremockSpec
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import play.api.Configuration
import play.api.http.Status.{NO_CONTENT, OK}
import play.api.libs.json.Json
import testdata.connector.employment.fullEmploymentRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.AllEmploymentsDataBuilder.allEmploymentsData
import utils.TestUtils._

class EmploymentConnectorISpec extends WiremockSpec {
  val httpClient                 = app.injector.instanceOf[HttpClient]
  val configuration              = app.injector.instanceOf[Configuration]
  val servicesConfig             = app.injector.instanceOf[ServicesConfig]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val appConfig = new BackendAppConfig(configuration, servicesConfig) {
    override val employmentBaseUrl: String = s"http://localhost:$wireMockPort"
  }

  val employmentId            = "employmentId"
  val source                  = "CUSTOMER"
  val sourcesDownstreamUrl    = s"/income-tax/nino/$nino/sources\\?taxYear=$taxYear"
  val employmentDownstreamUrl = s"/income-tax/nino/$nino/sources/$employmentId/$source\\?taxYear=$taxYear"
  val connector               = new EmploymentConnector(httpClient, appConfig)

  "getEmployments" must {
    "return a success response" in {
      stubGetWithResponseBody(
        url = sourcesDownstreamUrl,
        status = OK,
        response = Json.toJson(allEmploymentsData).toString()
      )

      val res = connector.getEmployments(nino, taxYear).futureValue
      assert(res === allEmploymentsData.some.asRight)
    }
  }

  "saveEmployment" must {
    "return a success response" in {
      stubPostWithoutResponseBody(
        url = sourcesDownstreamUrl,
        status = NO_CONTENT,
        requestBody = Json.toJson(fullEmploymentRequest).toString()
      )

      val res = connector.saveEmployment(nino, taxYear, fullEmploymentRequest).futureValue
      assert(res === ().asRight)
    }
  }

  "deleteEmployment" must {
    "return a success response" in {
      stubDeleteWithoutResponseBody(
        url = employmentDownstreamUrl,
        status = NO_CONTENT
      )

      val res = connector.deleteEmployment(nino, taxYear, employmentId).futureValue
      assert(res === ().asRight)
    }
  }
}
