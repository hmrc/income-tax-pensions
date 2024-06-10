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

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.libs.json.{JsValue, Json}
import utils.StateBenefitBuilder.aStateBenefit
import utils.StateBenefitsDataBuilder.{aStateBenefitsData, aStateBenefitsDataJsValue}
import utils.TestUtils

import java.time.{Instant, LocalDate}
import java.util.UUID

class StateBenefitsDataSpec extends TestUtils {

  private val stateBenefit = aStateBenefit
    .copy(startDate = LocalDate.parse("2019-04-23"))

  private val stateBenefitsData = aStateBenefitsData
    .copy(statePension = Some(stateBenefit.copy(benefitId = UUID.fromString("f1b9f4b2-3f3e-4b1b-8b1b-3b1b1b1b1b1b"))))
    .copy(statePensionLumpSum = Some(stateBenefit.copy(benefitId = UUID.fromString("f1b9f4b2-3f3e-4b1b-8b1b-3b1b1b1b1b1b"))))

  "stateBenefitsDataWrites" when {
    "convert StateBenefitsData to correct JsValue when full object" in {
      Json.toJson(stateBenefitsData) mustBe aStateBenefitsDataJsValue
    }

    "convert StateBenefitsData to correct JsValue when empty object" in {
      val jsValue: JsValue = Json.parse("""
          |{
          |}
          |""".stripMargin)

      Json.toJson(StateBenefitsData()) mustBe jsValue
    }
  }

  "stateBenefitsDataReads" when {
    "convert JsValue to StateBenefitsData when full object" in {
      Json.fromJson[StateBenefitsData](aStateBenefitsDataJsValue).get shouldBe stateBenefitsData
    }

    "convert JsValue to StateBenefitsData when empty object" in {
      val jsValue: JsValue = Json.parse("""
          |{
          |}
          |""".stripMargin)

      Json.fromJson[StateBenefitsData](jsValue).get shouldBe StateBenefitsData()
    }
  }
}
