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

import models.database.IncomeFromPensionsStatePensionStorageAnswers
import models.frontend.statepension.{IncomeFromPensionsStatePensionAnswers, StateBenefitAnswers}
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsValue, Json}
import testdata.connector.stateBenefits._
import testdata.database.incomeFromPensionsStatePensionStorageAnswers
import testdata.frontend.stateBenefitAnswers

import java.util.UUID

class AllStateBenefitsDataSpec extends AnyWordSpecLike {

  "allStateBenefitsDataWrites" should {
    "convert AllStateBenefitsData to correct JsValue when full object" in {
      assert(Json.toJson(allStateBenefitsData) === allStateBenefitsDataJsValue)
    }

    "convert AllStateBenefitsData to correct JsValue when empty object" in {
      val jsValue: JsValue = Json.parse("""
          |{
          |}
          |""".stripMargin)

      val allStateBenefitsData = AllStateBenefitsData(
        stateBenefitsData = Some(StateBenefitsData()),
        customerAddedStateBenefitsData = None
      )

      assert(Json.toJson(allStateBenefitsData) === jsValue)
    }
  }

  "allStateBenefitsDataReads" should {
    "convert JsValue to AllStateBenefitsData when full object" in {
      assert(Json.fromJson[AllStateBenefitsData](allStateBenefitsDataJsValue).get === allStateBenefitsData)
    }

    "convert JsValue to AllStateBenefitsData when empty object" in {
      val jsValue: JsValue = Json.parse("""
          |{
          |   "stateBenefits": {}
          |}
          |""".stripMargin)

      assert(
        Json.fromJson[AllStateBenefitsData](jsValue).get === AllStateBenefitsData(
          stateBenefitsData = Some(StateBenefitsData()),
          customerAddedStateBenefitsData = None
        ))
    }
  }

  "toIncomeFromPensionsStatePensionAnswers" should {
    "return an empty" in {
      val result = AllStateBenefitsData.empty.toIncomeFromPensionsStatePensionAnswers(None, None)
      assert(result === IncomeFromPensionsStatePensionAnswers.empty)
    }

    "return all answers" in {
      val result = allStateBenefitsData.toIncomeFromPensionsStatePensionAnswers(
        Some("sessionId"),
        Some(incomeFromPensionsStatePensionStorageAnswers.sampleAnswers))

      assert(
        result === IncomeFromPensionsStatePensionAnswers(
          statePension = Some(stateBenefitAnswers.sample.copy(benefitId = Some(UUID.fromString("f1b9f4b2-3f3e-4b1b-8b1b-3b1b1b1b1b1b")))),
          statePensionLumpSum = Some(stateBenefitAnswers.sample.copy(benefitId = Some(UUID.fromString("f1b9f4b2-3f3e-4b1b-8b1b-3b1b1b1b1b1b")))),
          sessionId = Some("sessionId")
        ))
    }

    "fallback to answers from database when no answers in IFS" in {
      val result = AllStateBenefitsData.empty.toIncomeFromPensionsStatePensionAnswers(
        Some("sessionId"),
        Some(IncomeFromPensionsStatePensionStorageAnswers(Some(false), Some(true))))

      assert(
        result === IncomeFromPensionsStatePensionAnswers(
          Some(StateBenefitAnswers(None, None, None, Some(false), None, None, None)),
          Some(StateBenefitAnswers(None, None, None, Some(true), None, None, None)),
          sessionId = Some("sessionId")
        ))
    }

  }
}
