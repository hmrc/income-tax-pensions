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
import utils.StateBenefitBuilder.aStateBenefit
import utils.TestUtils

import java.time.LocalDate
import java.util.UUID

class StateBenefitSpec extends TestUtils {

  "writes" when {
    "passed StateBenefit" should {
      "produce a valid json for fully populated object" in {
        val json = Json.parse("""
            |{
            |  "benefitId": "f1b9f4b2-3f3e-4b1b-8b1b-3b1b1b1b1b1b",
            |  "startDate": "2019-04-23",
            |  "amount": 300.00,
            |  "taxPaid": 400.00
            |}
            |""".stripMargin)

        val underTest = aStateBenefit
          .copy(startDate = LocalDate.parse("2019-04-23"))

        Json.toJson(underTest) mustBe json
      }

      "produce a valid json for a minimal object" in {
        val json = Json.parse("""
            |{
            |  "benefitId": "f1b9f4b2-3f3e-4b1b-8b1b-3b1b1b1b1b1b",
            |  "startDate": "2019-04-23"
            |}
            |""".stripMargin)

        val underTest =
          StateBenefit(
            benefitId = UUID.fromString("f1b9f4b2-3f3e-4b1b-8b1b-3b1b1b1b1b1b"),
            startDate = LocalDate.parse("2019-04-23"),
            None,
            None,
            None)

        Json.toJson(underTest) mustBe json
      }
    }
  }
}
