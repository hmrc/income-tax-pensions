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

package utils

import models.StateBenefitsData
import play.api.libs.json.{JsValue, Json}
import utils.StateBenefitBuilder.aStateBenefit

object StateBenefitsDataBuilder {

  val aStateBenefitsData: StateBenefitsData = StateBenefitsData(
    incapacityBenefits = Some(Set(aStateBenefit)),
    statePension = Some(aStateBenefit),
    statePensionLumpSum = Some(aStateBenefit),
    employmentSupportAllowances = Some(Set(aStateBenefit)),
    jobSeekersAllowances = Some(Set(aStateBenefit)),
    bereavementAllowance = Some(aStateBenefit),
    other = Some(aStateBenefit)
  )

  val aStateBenefitsDataJsValue: JsValue = Json.parse(
    """
      |{
      |  "incapacityBenefit": [
      |    {
      |      "dateIgnored": "2019-07-08T05:23:00Z",
      |      "submittedOn": "2020-09-11T17:23:00Z",
      |      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |      "startDate": "2019-04-23",
      |      "endDate": "2020-08-13",
      |      "amount": 300.00,
      |      "taxPaid": 400.00
      |    }
      |  ],
      |  "statePension": {
      |    "dateIgnored": "2019-07-08T05:23:00Z",
      |    "submittedOn": "2020-09-11T17:23:00Z",
      |    "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c935",
      |    "startDate": "2019-04-23",
      |    "endDate": "2020-08-13",
      |    "amount": 300.00,
      |    "taxPaid": 400.00
      |  },
      |  "statePensionLumpSum": {
      |    "dateIgnored": "2019-07-08T05:23:00Z",
      |    "submittedOn": "2020-09-11T17:23:00Z",
      |    "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c936",
      |    "startDate": "2019-04-23",
      |    "endDate": "2020-08-13",
      |    "amount": 300.00,
      |    "taxPaid": 400.00
      |  },
      |  "employmentSupportAllowance": [
      |    {
      |      "dateIgnored": "2019-07-08T05:23:00Z",
      |      "submittedOn": "2020-09-11T17:23:00Z",
      |      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c937",
      |      "startDate": "2019-04-23",
      |      "endDate": "2020-08-13",
      |      "amount": 300.00,
      |      "taxPaid": 400.00
      |    }
      |  ],
      |  "jobSeekersAllowance": [
      |    {
      |      "dateIgnored": "2019-07-08T05:23:00Z",
      |      "submittedOn": "2020-09-11T17:23:00Z",
      |      "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c938",
      |      "startDate": "2019-04-23",
      |      "endDate": "2020-08-13",
      |      "amount": 300.00,
      |      "taxPaid": 400.00
      |    }
      |  ],
      |  "bereavementAllowance": {
      |    "dateIgnored": "2019-07-08T05:23:00Z",
      |    "submittedOn": "2020-09-11T17:23:00Z",
      |    "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c939",
      |    "startDate": "2019-04-23",
      |    "endDate": "2020-08-13",
      |    "amount": 300.00,
      |    "taxPaid": 400.00
      |  },
      |  "otherStateBenefits": {
      |    "dateIgnored": "2019-07-08T05:23:00Z",
      |    "submittedOn": "2020-09-11T17:23:00Z",
      |    "benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c940",
      |    "startDate": "2019-04-23",
      |    "endDate": "2020-08-13",
      |    "amount": 300.00,
      |    "taxPaid": 400.00
      |  }
      |}
      |""".stripMargin)
}
