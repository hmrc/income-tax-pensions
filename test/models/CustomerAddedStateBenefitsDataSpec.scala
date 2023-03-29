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

import play.api.libs.json.{JsValue, Json}
import utils.CustomerAddedStateBenefitBuilder.aCustomerAddedStateBenefit
import utils.CustomerAddedStateBenefitsDataBuilder.{aCustomerAddedStateBenefitsData, aCustomerAddedStateBenefitsDataJsValue}
import utils.TestUtils

import java.time.{Instant, LocalDate}
import java.util.UUID

class CustomerAddedStateBenefitsDataSpec extends TestUtils {

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

  "customerAddedStateBenefitsDataWrites" should {
    "convert CustomerAddedStateBenefitsData to correct JsValue when full object" in {
      Json.toJson(customerAddedStateBenefitsData) mustBe aCustomerAddedStateBenefitsDataJsValue
    }

    "convert CustomerAddedStateBenefitsData to correct JsValue when empty object" in {
      val jsValue: JsValue = Json.parse(
        """
          |{
          |}
          |""".stripMargin)

      Json.toJson(CustomerAddedStateBenefitsData()) mustBe jsValue
    }
  }

  "customerAddedStateBenefitsDataReads" should {
    "convert JsValue to CustomerAddedStateBenefitsData when full object" in {
      Json.fromJson[CustomerAddedStateBenefitsData](aCustomerAddedStateBenefitsDataJsValue).get mustBe customerAddedStateBenefitsData
    }

    "convert JsValue to CustomerAddedStateBenefitsData when empty object" in {
      val jsValue: JsValue = Json.parse(
        """
          |{
          |}
          |""".stripMargin)

      Json.fromJson[CustomerAddedStateBenefitsData](jsValue).get mustBe CustomerAddedStateBenefitsData()
    }
  }
}
