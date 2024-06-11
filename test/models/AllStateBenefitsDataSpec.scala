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

import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsValue, Json}
import testdata.connector.stateBenefits._

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
}
