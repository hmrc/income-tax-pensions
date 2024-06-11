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

package testdata.connector

import models.AllStateBenefitsData
import play.api.libs.json.JsObject
import utils.AllStateBenefitsDataBuilder.anAllStateBenefitsData
import utils.CustomerAddedStateBenefitBuilder.aCustomerAddedStateBenefit
import utils.CustomerAddedStateBenefitsDataBuilder.{aCustomerAddedStateBenefitsData, aCustomerAddedStateBenefitsDataJsValue}
import utils.StateBenefitBuilder.aStateBenefit
import utils.StateBenefitsDataBuilder.{aStateBenefitsData, aStateBenefitsDataJsValue}

import java.time.LocalDate
import java.util.UUID

object stateBenefits {
  val stateBenefit = aStateBenefit
    .copy(startDate = LocalDate.parse("2019-04-23"))

  val stateBenefitsData = aStateBenefitsData
    .copy(statePension = Some(stateBenefit.copy(benefitId = UUID.fromString("f1b9f4b2-3f3e-4b1b-8b1b-3b1b1b1b1b1b"))))
    .copy(statePensionLumpSum = Some(stateBenefit.copy(benefitId = UUID.fromString("f1b9f4b2-3f3e-4b1b-8b1b-3b1b1b1b1b1b"))))

  val customerAddedStateBenefit = aCustomerAddedStateBenefit
    .copy(startDate = LocalDate.parse("2018-07-17"))

  val customerAddedStateBenefitsData = aCustomerAddedStateBenefitsData
    .copy(statePensions = Some(Set(customerAddedStateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c943")))))
    .copy(statePensionLumpSums = Some(Set(customerAddedStateBenefit.copy(benefitId = UUID.fromString("a1e8057e-fbbc-47a8-a8b4-78d9f015c956")))))

  val allStateBenefitsData: AllStateBenefitsData = anAllStateBenefitsData.copy(
    stateBenefitsData = Some(stateBenefitsData),
    customerAddedStateBenefitsData = Some(customerAddedStateBenefitsData)
  )

  val allStateBenefitsDataJsValue = JsObject(
    Seq(
      "stateBenefits"              -> aStateBenefitsDataJsValue,
      "customerAddedStateBenefits" -> aCustomerAddedStateBenefitsDataJsValue
    ))

}
