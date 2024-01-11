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

package models

import play.api.libs.json.Json
import utils.StateBenefitBuilder.aStateBenefit
import utils.TestUtils

import java.time.{Instant, LocalDate}
import java.util.UUID

class StateBenefitSpec extends TestUtils {

  "writes" when {
    "passed StateBenefit" should {
      "produce a valid json for fully populated object" in {
        val json = Json.parse(
          """
            |{
            |  "dateIgnored": "2019-07-08T05:23:00Z",
            |  "submittedOn": "2020-03-13T19:23:00Z",
            |  "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c936",
            |  "startDate": "2019-04-23",
            |  "endDate": "2020-08-13",
            |  "amount": 300.00,
            |  "taxPaid": 400.00
            |}
            |""".stripMargin)

        val underTest = aStateBenefit
          .copy(startDate = LocalDate.parse("2019-04-23"))
          .copy(endDate = Some(LocalDate.parse("2020-08-13")))
          .copy(submittedOn = Some(Instant.parse("2020-03-13T19:23:00Z")))
          .copy(dateIgnored = Some(Instant.parse("2019-07-08T05:23:00Z")))

        Json.toJson(underTest) mustBe json
      }

      "produce a valid json for a minimal object" in {
        val json = Json.parse(
          """
            |{
            |  "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c936",
            |  "startDate": "2019-04-23"
            |}
            |""".stripMargin)

        val underTest = StateBenefit(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c936"), startDate = LocalDate.parse("2019-04-23"))

        Json.toJson(underTest) mustBe json
      }
    }
  }
}
