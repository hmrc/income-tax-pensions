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

import config.AppConfig
import uk.gov.hmrc.http.HeaderNames.{xRequestChain, xSessionId}
import uk.gov.hmrc.http.{HeaderCarrier, SessionId}
import utils.TestUtils

class ConnectorSpec extends TestUtils{

  class FakeConnector(override val appConfig: AppConfig) extends Connector {
    def headerCarrierTest(url: String)(hc: HeaderCarrier): HeaderCarrier = headerCarrier(url)(hc)
  }
  val connector = new FakeConnector(appConfig = mockAppConfig)

  "Connector" when {

    "host is Internal" should {
      val internalHost = "http://localhost"

      "extraHeaders is empty when the host is external and no extraHeaders were added" in {
        val hc = HeaderCarrier(sessionId = Some(SessionId("sessionIdHeaderValue")))
        val result = connector.headerCarrierTest(internalHost)(hc)

        result.extraHeaders.isEmpty mustBe true
      }
    }

    "host is External" should {
      val externalHost = "http://127.0.0.1"

      "include all HeaderCarrier headers in the extraHeaders when the host is external" in {
        val hc = HeaderCarrier(sessionId = Some(SessionId("sessionIdHeaderValue")))
        val result = connector.headerCarrierTest(externalHost)(hc)

        result.extraHeaders.size mustBe 2
        result.extraHeaders.contains(xSessionId -> "sessionIdHeaderValue") mustBe true
        result.extraHeaders.exists(x => x._1.equalsIgnoreCase(xRequestChain)) mustBe true
      }
    }
  }

}
