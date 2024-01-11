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

import play.api.libs.json.{JsObject, JsValue, Json}
import utils.AllStateBenefitsDataBuilder.anAllStateBenefitsData
import utils.CustomerAddedStateBenefitBuilder.aCustomerAddedStateBenefit
import utils.CustomerAddedStateBenefitsDataBuilder.{aCustomerAddedStateBenefitsData, aCustomerAddedStateBenefitsDataJsValue}
import utils.StateBenefitBuilder.aStateBenefit
import utils.StateBenefitsDataBuilder.{aStateBenefitsData, aStateBenefitsDataJsValue}
import utils.TestUtils

import java.time.{Instant, LocalDate}
import java.util.UUID

class AllStateBenefitsDataSpec extends TestUtils {

  private val stateBenefit = aStateBenefit
    .copy(startDate = LocalDate.parse("2019-04-23"))
    .copy(endDate = Some(LocalDate.parse("2020-08-13")))
    .copy(dateIgnored = Some(Instant.parse("2019-07-08T05:23:00Z")))
    .copy(submittedOn = Some(Instant.parse("2020-09-11T17:23:00Z")))

  private val stateBenefitsData = aStateBenefitsData
    .copy(incapacityBenefits = Some(Set(stateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c934")))))
    .copy(statePension = Some(stateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c935"))))
    .copy(statePensionLumpSum = Some(stateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c936"))))
    .copy(employmentSupportAllowances = Some(Set(stateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c937")))))
    .copy(jobSeekersAllowances = Some(Set(stateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c938")))))
    .copy(bereavementAllowance = Some(stateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c939"))))
    .copy(other = Some(stateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c940"))))

  private val customerAddedStateBenefit = aCustomerAddedStateBenefit
    .copy(submittedOn = Some(Instant.parse("2020-11-17T19:23:00Z")))
    .copy(startDate = LocalDate.parse("2018-07-17"))
    .copy(endDate = Some(LocalDate.parse("2020-09-23")))

  private val customerAddedStateBenefitsData = aCustomerAddedStateBenefitsData
    .copy(incapacityBenefits = Some(Set(customerAddedStateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c941")))))
    .copy(statePensions = Some(Set(customerAddedStateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c943")))))
    .copy(statePensionLumpSums = Some(Set(customerAddedStateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c956")))))
    .copy(employmentSupportAllowances = Some(Set(customerAddedStateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c988")))))
    .copy(jobSeekersAllowances = Some(Set(customerAddedStateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c990")))))
    .copy(bereavementAllowances = Some(Set(customerAddedStateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c997")))))
    .copy(otherStateBenefits = Some(Set(customerAddedStateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c957")))))

  private val allStateBenefitsData = anAllStateBenefitsData.copy(
    stateBenefitsData = Some(stateBenefitsData),
    customerAddedStateBenefitsData = Some(customerAddedStateBenefitsData)
  )

  private val allStateBenefitsDataJsValue = JsObject(Seq(
    "stateBenefits" -> aStateBenefitsDataJsValue,
    "customerAddedStateBenefits" -> aCustomerAddedStateBenefitsDataJsValue
  ))

  "allStateBenefitsDataWrites" should {
    "convert AllStateBenefitsData to correct JsValue when full object" in {
      Json.toJson(allStateBenefitsData) mustBe allStateBenefitsDataJsValue
    }

    "convert AllStateBenefitsData to correct JsValue when empty object" in {
      val jsValue: JsValue = Json.parse(
        """
          |{
          |}
          |""".stripMargin)

      val allStateBenefitsData = AllStateBenefitsData(
        stateBenefitsData = Some(StateBenefitsData()),
        customerAddedStateBenefitsData = None
      )

      Json.toJson(allStateBenefitsData) mustBe jsValue
    }
  }

  "allStateBenefitsDataReads" should {
    "convert JsValue to AllStateBenefitsData when full object" in {
      Json.fromJson[AllStateBenefitsData](allStateBenefitsDataJsValue).get mustBe allStateBenefitsData
    }

    "convert JsValue to AllStateBenefitsData when empty object" in {
      val jsValue: JsValue = Json.parse(
        """
          |{
          |   "stateBenefits": {}
          |}
          |""".stripMargin)

      Json.fromJson[AllStateBenefitsData](jsValue).get mustBe AllStateBenefitsData(
        stateBenefitsData = Some(StateBenefitsData()),
        customerAddedStateBenefitsData = None
      )
    }
  }
}
