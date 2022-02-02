/*
 * Copyright 2022 HM Revenue & Customs
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

import com.codahale.metrics.SharedMetricRegistries
import play.api.libs.json.{JsObject, Json}
import utils.TestUtils

class GetStateBenefitsModelSpec extends TestUtils {
  SharedMetricRegistries.clear()

  val jsonModel = Json.parse {
    """
      |{
      |   "stateBenefits":{
      |      "incapacityBenefit":[
      |         {
      |            "dateIgnored":"2019-04-11T16:22:00Z",
      |            "submittedOn":"2020-09-11T17:23:00Z",
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |            "startDate":"2019-11-13",
      |            "endDate":"2020-08-23",
      |            "amount":1212.34,
      |            "taxPaid":22323.23
      |         }
      |      ],
      |      "statePension":{
      |         "dateIgnored":"2018-09-09T19:23:00Z",
      |         "submittedOn":"2020-08-07T12:23:00Z",
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c935",
      |         "startDate":"2018-06-03",
      |         "endDate":"2020-09-13",
      |         "amount":42323.23,
      |         "taxPaid":2323.44
      |      },
      |      "statePensionLumpSum":{
      |         "dateIgnored":"2019-07-08T05:23:00Z",
      |         "submittedOn":"2020-03-13T19:23:00Z",
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c936",
      |         "startDate":"2019-04-23",
      |         "endDate":"2020-08-13",
      |         "amount":45454.23,
      |         "taxPaid":45432.56
      |      },
      |      "employmentSupportAllowance":[
      |         {
      |            "dateIgnored":"2019-09-28T10:23:00Z",
      |            "submittedOn":"2020-11-13T19:23:00Z",
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c937",
      |            "startDate":"2019-09-23",
      |            "endDate":"2020-08-23",
      |            "amount":44545.43,
      |            "taxPaid":35343.23
      |         }
      |      ],
      |      "jobSeekersAllowance":[
      |         {
      |            "dateIgnored":"2019-08-18T13:23:00Z",
      |            "submittedOn":"2020-07-10T18:23:00Z",
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c938",
      |            "startDate":"2019-09-19",
      |            "endDate":"2020-09-23",
      |            "amount":33223.12,
      |            "taxPaid":44224.56
      |         }
      |      ],
      |      "bereavementAllowance":{
      |         "dateIgnored":"2020-08-10T12:23:00Z",
      |         "submittedOn":"2020-09-19T19:23:00Z",
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c939",
      |         "startDate":"2019-05-22",
      |         "endDate":"2020-09-26",
      |         "amount":56534.23,
      |         "taxPaid":34343.57
      |      },
      |      "otherStateBenefits":{
      |         "dateIgnored":"2020-01-11T15:23:00Z",
      |         "submittedOn":"2020-09-13T15:23:00Z",
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c940",
      |         "startDate":"2018-09-03",
      |         "endDate":"2020-06-03",
      |         "amount":56532.45,
      |         "taxPaid":5656.89
      |      }
      |   },
      |   "customerAddedStateBenefits":{
      |      "incapacityBenefit":[
      |         {
      |            "submittedOn":"2020-11-17T19:23:00Z",
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c941",
      |            "startDate":"2018-07-17",
      |            "endDate":"2020-09-23",
      |            "amount":45646.78,
      |            "taxPaid":4544.34
      |         }
      |      ],
      |      "statePension":{
      |         "submittedOn":"2020-06-11T10:23:00Z",
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c943",
      |         "startDate":"2018-04-03",
      |         "endDate":"2020-09-13",
      |         "amount":45642.45,
      |         "taxPaid":6764.34
      |      },
      |      "statePensionLumpSum":{
      |         "submittedOn":"2020-06-13T05:29:00Z",
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c956",
      |         "startDate":"2019-09-23",
      |         "endDate":"2020-09-26",
      |         "amount":34322.34,
      |         "taxPaid":4564.45
      |      },
      |      "employmentSupportAllowance":[
      |         {
      |            "submittedOn":"2020-02-10T11:20:00Z",
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c988",
      |            "startDate":"2019-09-11",
      |            "endDate":"2020-06-13",
      |            "amount":45424.23,
      |            "taxPaid":23232.34
      |         }
      |      ],
      |      "jobSeekersAllowance":[
      |         {
      |            "submittedOn":"2020-05-13T14:23:00Z",
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c990",
      |            "startDate":"2019-07-10",
      |            "endDate":"2020-05-11",
      |            "amount":34343.78,
      |            "taxPaid":3433.56
      |         }
      |      ],
      |      "bereavementAllowance":{
      |         "submittedOn":"2020-02-13T11:23:00Z",
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c997",
      |         "startDate":"2018-08-12",
      |         "endDate":"2020-07-13",
      |         "amount":45423.45,
      |         "taxPaid":4543.64
      |      },
      |      "otherStateBenefits":{
      |         "submittedOn":"2020-09-12T12:23:00Z",
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c957",
      |         "startDate":"2018-01-13",
      |         "endDate":"2020-08-13",
      |         "amount":63333.33,
      |         "taxPaid":4644.45
      |      }
      |   }
      |}
      |
      |""".stripMargin
  }

  val model = GetStateBenefitsModel(
    Some(StateBenefits(
      incapacityBenefit = Some(List(StateBenefit(
        benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
        startDate = "2019-11-13",
        dateIgnored = Some("2019-04-11T16:22:00Z"),
        submittedOn = Some("2020-09-11T17:23:00Z"),
        endDate = Some("2020-08-23"),
        amount = Some(1212.34),
        taxPaid = Some(22323.23)
      ))),
      statePension = Some(StateBenefit(
        benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c935",
        startDate = "2018-06-03",
        dateIgnored = Some("2018-09-09T19:23:00Z"),
        submittedOn = Some("2020-08-07T12:23:00Z"),
        endDate = Some("2020-09-13"),
        amount = Some(42323.23),
        taxPaid = Some(2323.44)
      )),
      statePensionLumpSum = Some(StateBenefit(
        benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c936",
        startDate = "2019-04-23",
        dateIgnored = Some("2019-07-08T05:23:00Z"),
        submittedOn = Some("2020-03-13T19:23:00Z"),
        endDate = Some("2020-08-13"),
        amount = Some(45454.23),
        taxPaid = Some(45432.56)
      )),
      employmentSupportAllowance = Some(List(StateBenefit(
        benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c937",
        startDate = "2019-09-23",
        dateIgnored = Some("2019-09-28T10:23:00Z"),
        submittedOn = Some("2020-11-13T19:23:00Z"),
        endDate = Some("2020-08-23"),
        amount = Some(44545.43),
        taxPaid = Some(35343.23)
      ))),
      jobSeekersAllowance = Some(List(StateBenefit(
        benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c938",
        startDate = "2019-09-19",
        dateIgnored = Some("2019-08-18T13:23:00Z"),
        submittedOn = Some("2020-07-10T18:23:00Z"),
        endDate = Some("2020-09-23"),
        amount = Some(33223.12),
        taxPaid = Some(44224.56)
      ))),
      bereavementAllowance = Some(StateBenefit(
        benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c939",
        startDate = "2019-05-22",
        dateIgnored = Some("2020-08-10T12:23:00Z"),
        submittedOn = Some("2020-09-19T19:23:00Z"),
        endDate = Some("2020-09-26"),
        amount = Some(56534.23),
        taxPaid = Some(34343.57)
      )),
      otherStateBenefits = Some(StateBenefit(
        benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c940",
        startDate = "2018-09-03",
        dateIgnored = Some("2020-01-11T15:23:00Z"),
        submittedOn = Some("2020-09-13T15:23:00Z"),
        endDate = Some("2020-06-03"),
        amount = Some(56532.45),
        taxPaid = Some(5656.89)
      )),
    )),
    Some(StateBenefits(
      incapacityBenefit = Some(List(StateBenefit(
        benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c941",
        startDate = "2018-07-17",
        submittedOn = Some("2020-11-17T19:23:00Z"),
        endDate = Some("2020-09-23"),
        amount = Some(45646.78),
        taxPaid = Some(4544.34),
        dateIgnored = None
      ))),
      statePension = Some(StateBenefit(
        benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c943",
        startDate = "2018-04-03",
        submittedOn = Some("2020-06-11T10:23:00Z"),
        endDate = Some("2020-09-13"),
        amount = Some(45642.45),
        taxPaid = Some(6764.34),
        dateIgnored = None

      )),
      statePensionLumpSum = Some(StateBenefit(
        benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c956",
        startDate = "2019-09-23",
        submittedOn = Some("2020-06-13T05:29:00Z"),
        endDate = Some("2020-09-26"),
        amount = Some(34322.34),
        taxPaid = Some(4564.45),
        dateIgnored = None

      )),
      employmentSupportAllowance = Some(List(StateBenefit(
        benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c988",
        startDate = "2019-09-11",
        submittedOn = Some("2020-02-10T11:20:00Z"),
        endDate = Some("2020-06-13"),
        amount = Some(45424.23),
        taxPaid = Some(23232.34),
        dateIgnored = None

      ))),
      jobSeekersAllowance = Some(List(StateBenefit(
        benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c990",
        startDate = "2019-07-10",
        submittedOn = Some("2020-05-13T14:23:00Z"),
        endDate = Some("2020-05-11"),
        amount = Some(34343.78),
        taxPaid = Some(3433.56),
        dateIgnored = None

      ))),
      bereavementAllowance = Some(StateBenefit(
        benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c997",
        startDate = "2018-08-12",
        submittedOn = Some("2020-02-13T11:23:00Z"),
        endDate = Some("2020-07-13"),
        amount = Some(45423.45),
        taxPaid = Some(4543.64),
        dateIgnored = None

      )),
      otherStateBenefits = Some(StateBenefit(
        benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c957",
        startDate = "2018-01-13",
        submittedOn = Some("2020-09-12T12:23:00Z"),
        endDate = Some("2020-08-13"),
        amount = Some(63333.33),
        taxPaid = Some(4644.45),
        dateIgnored = None

      )),
    ))
  )

  "GetStateBenefitsModel with all values" should {

    "parse to Json" in {
      Json.toJson(model) mustBe jsonModel
    }

    "parse from Json" in {
      jsonModel.as[GetStateBenefitsModel]
    }
  }
}
