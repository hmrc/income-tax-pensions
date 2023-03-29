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

package utils

import models.CustomerAddedStateBenefitsData
import play.api.libs.json.{JsValue, Json}
import utils.CustomerAddedStateBenefitBuilder.aCustomerAddedStateBenefit

object CustomerAddedStateBenefitsDataBuilder {

  val aCustomerAddedStateBenefitsData: CustomerAddedStateBenefitsData = CustomerAddedStateBenefitsData(
    incapacityBenefits = Some(Set(aCustomerAddedStateBenefit)),
    statePensions = Some(Set(aCustomerAddedStateBenefit)),
    statePensionLumpSums = Some(Set(aCustomerAddedStateBenefit)),
    employmentSupportAllowances = Some(Set(aCustomerAddedStateBenefit)),
    jobSeekersAllowances = Some(Set(aCustomerAddedStateBenefit)),
    bereavementAllowances = Some(Set(aCustomerAddedStateBenefit)),
    otherStateBenefits = Some(Set(aCustomerAddedStateBenefit))
  )

  val aCustomerAddedStateBenefitsDataJsValue: JsValue = Json.parse(
    """
      |{
      |  "incapacityBenefit": [
      |    {
      |      "submittedOn": "2020-11-17T19:23:00Z",
      |      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c941",
      |      "startDate": "2018-07-17",
      |      "endDate": "2020-09-23",
      |      "amount": 100.00,
      |      "taxPaid": 200.00
      |    }
      |  ],
      |  "statePension": [
      |    {
      |      "submittedOn": "2020-11-17T19:23:00Z",
      |      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c943",
      |      "startDate": "2018-07-17",
      |      "endDate": "2020-09-23",
      |      "amount": 100.00,
      |      "taxPaid": 200.00
      |    }
      |  ],
      |  "statePensionLumpSum": [
      |    {
      |      "submittedOn": "2020-11-17T19:23:00Z",
      |      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c956",
      |      "startDate": "2018-07-17",
      |      "endDate": "2020-09-23",
      |      "amount": 100.00,
      |      "taxPaid": 200.00
      |    }
      |  ],
      |  "employmentSupportAllowance": [
      |    {
      |      "submittedOn": "2020-11-17T19:23:00Z",
      |      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c988",
      |      "startDate": "2018-07-17",
      |      "endDate": "2020-09-23",
      |      "amount": 100.00,
      |      "taxPaid": 200.00
      |    }
      |  ],
      |  "jobSeekersAllowance": [
      |    {
      |      "submittedOn": "2020-11-17T19:23:00Z",
      |      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c990",
      |      "startDate": "2018-07-17",
      |      "endDate": "2020-09-23",
      |      "amount": 100.00,
      |      "taxPaid": 200.00
      |    }
      |  ],
      |  "bereavementAllowance": [
      |    {
      |      "submittedOn": "2020-11-17T19:23:00Z",
      |      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c997",
      |      "startDate": "2018-07-17",
      |      "endDate": "2020-09-23",
      |      "amount": 100.00,
      |      "taxPaid": 200.00
      |    }
      |  ],
      |  "otherStateBenefits": [
      |    {
      |      "submittedOn": "2020-11-17T19:23:00Z",
      |      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c957",
      |      "startDate": "2018-07-17",
      |      "endDate": "2020-09-23",
      |      "amount": 100.00,
      |      "taxPaid": 200.00
      |    }
      |  ]
      |}
      |""".stripMargin)
}
