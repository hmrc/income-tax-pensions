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

package models

import play.api.libs.json.Json
import utils.CustomerAddedStateBenefitBuilder.aCustomerAddedStateBenefit
import utils.TestUtils

import java.time.{Instant, LocalDate}
import java.util.UUID

class CustomerAddedStateBenefitSpec extends TestUtils {

  "writes" when {
    "passed CustomerAddedStateBenefit" should {
      "produce a valid json for fully populated object" in {
        val json = Json.parse("""
            |{
            |  "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c941",
            |  "startDate": "2019-04-23",
            |  "amount": 100.00,
            |  "taxPaid": 200.00
            |}
            |""".stripMargin)

        val underTest = aCustomerAddedStateBenefit
          .copy(startDate = LocalDate.parse("2019-04-23"))

        Json.toJson(underTest) mustBe json
      }

      "produce a valid json for a minimal object" in {
        val json = Json.parse("""
            |{
            |  "benefitId": "fc89ce78-8d5a-4a6c-bd9c-f7db844f6bc4",
            |  "startDate": "2019-04-23"
            |}
            |""".stripMargin)

        val underTest = CustomerAddedStateBenefit(
          benefitId = UUID.fromString("fc89ce78-8d5a-4a6c-bd9c-f7db844f6bc4"),
          startDate = LocalDate.parse("2019-04-23")
        )

        Json.toJson(underTest) mustBe json
      }
    }
  }
}
