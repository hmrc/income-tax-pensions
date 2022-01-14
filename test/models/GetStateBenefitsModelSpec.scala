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
import models.GetStateBenefitsModel._
import play.api.libs.json.{JsValue, Json}
import utils.TestUtils

class GetStateBenefitsModelSpec extends TestUtils {

  SharedMetricRegistries.clear()
  val benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c934"

  val defaultFullStateBenefit: StateBenefit =
    StateBenefit(
      benefitId = benefitId,
      startDate = "2019-11-13",
      dateIgnored = Some("2019-04-11T16:22:00Z"),
      endDate = Some("2020-08-23"),
      amount = Some(1212.34),
      submittedOn = Some("2020-09-11T17:23:00Z"),
      taxPaid = Some(22323.23)
    )

  val defaultFullCustomerAddedStateBenefit: CustomerStateBenefit =
    CustomerStateBenefit(
      benefitId = benefitId,
      startDate = "2018-07-17",
      endDate = Some("2020-09-23"),
      amount = Some(45646.78),
      submittedOn = Some("2020-11-17T19:23:00Z"),
      taxPaid = Some(4544.34)
    )

  val fullGetStateBenefitsJson: JsValue = Json.parse(
    """
      |{
      |   "stateBenefits":{
      |      "incapacityBenefit":[
      |         {
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |            "startDate":"2019-11-13",
      |            "dateIgnored":"2019-04-11T16:22:00Z",
      |            "submittedOn":"2020-09-11T17:23:00Z",
      |            "endDate":"2020-08-23",
      |            "amount":1212.34,
      |            "taxPaid":22323.23
      |         }
      |      ],
      |      "statePension":{
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |         "startDate":"2019-11-13",
      |         "dateIgnored":"2019-04-11T16:22:00Z",
      |         "submittedOn":"2020-09-11T17:23:00Z",
      |         "endDate":"2020-08-23",
      |         "amount":1212.34,
      |         "taxPaid":22323.23
      |      },
      |      "statePensionLumpSum":{
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |         "startDate":"2019-11-13",
      |         "dateIgnored":"2019-04-11T16:22:00Z",
      |         "submittedOn":"2020-09-11T17:23:00Z",
      |         "endDate":"2020-08-23",
      |         "amount":1212.34,
      |         "taxPaid":22323.23
      |      },
      |      "employmentSupportAllowance":[
      |         {
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |            "startDate":"2019-11-13",
      |            "dateIgnored":"2019-04-11T16:22:00Z",
      |            "submittedOn":"2020-09-11T17:23:00Z",
      |            "endDate":"2020-08-23",
      |            "amount":1212.34,
      |            "taxPaid":22323.23
      |         }
      |      ],
      |      "jobSeekersAllowance":[
      |         {
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |            "startDate":"2019-11-13",
      |            "dateIgnored":"2019-04-11T16:22:00Z",
      |            "submittedOn":"2020-09-11T17:23:00Z",
      |            "endDate":"2020-08-23",
      |            "amount":1212.34,
      |            "taxPaid":22323.23
      |         }
      |      ],
      |      "bereavementAllowance":{
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |         "startDate":"2019-11-13",
      |         "dateIgnored":"2019-04-11T16:22:00Z",
      |         "submittedOn":"2020-09-11T17:23:00Z",
      |         "endDate":"2020-08-23",
      |         "amount":1212.34,
      |         "taxPaid":22323.23
      |      },
      |      "otherStateBenefits":{
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |         "startDate":"2019-11-13",
      |         "dateIgnored":"2019-04-11T16:22:00Z",
      |         "submittedOn":"2020-09-11T17:23:00Z",
      |         "endDate":"2020-08-23",
      |         "amount":1212.34,
      |         "taxPaid":22323.23
      |      }
      |   },
      |   "customerAddedStateBenefits":{
      |      "incapacityBenefit":[
      |         {
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |            "startDate":"2018-07-17",
      |            "submittedOn":"2020-11-17T19:23:00Z",
      |            "endDate":"2020-09-23",
      |            "amount":45646.78,
      |            "taxPaid":4544.34
      |         }
      |      ],
      |      "statePension":{
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |         "startDate":"2018-07-17",
      |         "submittedOn":"2020-11-17T19:23:00Z",
      |         "endDate":"2020-09-23",
      |         "amount":45646.78,
      |         "taxPaid":4544.34
      |      },
      |      "statePensionLumpSum":{
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |         "startDate":"2018-07-17",
      |         "submittedOn":"2020-11-17T19:23:00Z",
      |         "endDate":"2020-09-23",
      |         "amount":45646.78,
      |         "taxPaid":4544.34
      |      },
      |      "employmentSupportAllowance":[
      |         {
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |            "startDate":"2018-07-17",
      |            "submittedOn":"2020-11-17T19:23:00Z",
      |            "endDate":"2020-09-23",
      |            "amount":45646.78,
      |            "taxPaid":4544.34
      |         }
      |      ],
      |      "jobSeekersAllowance":[
      |         {
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |            "startDate":"2018-07-17",
      |            "submittedOn":"2020-11-17T19:23:00Z",
      |            "endDate":"2020-09-23",
      |            "amount":45646.78,
      |            "taxPaid":4544.34
      |         }
      |      ],
      |      "bereavementAllowance":{
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |         "startDate":"2018-07-17",
      |         "submittedOn":"2020-11-17T19:23:00Z",
      |         "endDate":"2020-09-23",
      |         "amount":45646.78,
      |         "taxPaid":4544.34
      |      },
      |      "otherStateBenefits":{
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |         "startDate":"2018-07-17",
      |         "submittedOn":"2020-11-17T19:23:00Z",
      |         "endDate":"2020-09-23",
      |         "amount":45646.78,
      |         "taxPaid":4544.34
      |      }
      |   }
      |}
      |""".stripMargin
  )
  val modelWithoutCustomerAddedBenefitsJson: JsValue = Json.parse(
    """
      |{
      |   "stateBenefits":{
      |      "incapacityBenefit":[
      |         {
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |            "startDate":"2019-11-13",
      |            "dateIgnored":"2019-04-11T16:22:00Z",
      |            "submittedOn":"2020-09-11T17:23:00Z",
      |            "endDate":"2020-08-23",
      |            "amount":1212.34,
      |            "taxPaid":22323.23
      |         }
      |      ],
      |      "statePension":{
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |         "startDate":"2019-11-13",
      |         "dateIgnored":"2019-04-11T16:22:00Z",
      |         "submittedOn":"2020-09-11T17:23:00Z",
      |         "endDate":"2020-08-23",
      |         "amount":1212.34,
      |         "taxPaid":22323.23
      |      },
      |      "statePensionLumpSum":{
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |         "startDate":"2019-11-13",
      |         "dateIgnored":"2019-04-11T16:22:00Z",
      |         "submittedOn":"2020-09-11T17:23:00Z",
      |         "endDate":"2020-08-23",
      |         "amount":1212.34,
      |         "taxPaid":22323.23
      |      },
      |      "employmentSupportAllowance":[
      |         {
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |            "startDate":"2019-11-13",
      |            "dateIgnored":"2019-04-11T16:22:00Z",
      |            "submittedOn":"2020-09-11T17:23:00Z",
      |            "endDate":"2020-08-23",
      |            "amount":1212.34,
      |            "taxPaid":22323.23
      |         }
      |      ],
      |      "jobSeekersAllowance":[
      |         {
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |            "startDate":"2019-11-13",
      |            "dateIgnored":"2019-04-11T16:22:00Z",
      |            "submittedOn":"2020-09-11T17:23:00Z",
      |            "endDate":"2020-08-23",
      |            "amount":1212.34,
      |            "taxPaid":22323.23
      |         }
      |      ],
      |      "bereavementAllowance":{
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |         "startDate":"2019-11-13",
      |         "dateIgnored":"2019-04-11T16:22:00Z",
      |         "submittedOn":"2020-09-11T17:23:00Z",
      |         "endDate":"2020-08-23",
      |         "amount":1212.34,
      |         "taxPaid":22323.23
      |      },
      |      "otherStateBenefits":{
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |         "startDate":"2019-11-13",
      |         "dateIgnored":"2019-04-11T16:22:00Z",
      |         "submittedOn":"2020-09-11T17:23:00Z",
      |         "endDate":"2020-08-23",
      |         "amount":1212.34,
      |         "taxPaid":22323.23
      |      }
      |   }
      |}
      |""".stripMargin
  )

  val modelWithoutStateBenefitJson: JsValue = Json.parse(
    """
      |{
      |   "customerAddedStateBenefits":{
      |      "incapacityBenefit":[
      |         {
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |            "startDate":"2018-07-17",
      |            "submittedOn":"2020-11-17T19:23:00Z",
      |            "endDate":"2020-09-23",
      |            "amount":45646.78,
      |            "taxPaid":4544.34
      |         }
      |      ],
      |      "statePension":{
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |         "startDate":"2018-07-17",
      |         "submittedOn":"2020-11-17T19:23:00Z",
      |         "endDate":"2020-09-23",
      |         "amount":45646.78,
      |         "taxPaid":4544.34
      |      },
      |      "statePensionLumpSum":{
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |         "startDate":"2018-07-17",
      |         "submittedOn":"2020-11-17T19:23:00Z",
      |         "endDate":"2020-09-23",
      |         "amount":45646.78,
      |         "taxPaid":4544.34
      |      },
      |      "employmentSupportAllowance":[
      |         {
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |            "startDate":"2018-07-17",
      |            "submittedOn":"2020-11-17T19:23:00Z",
      |            "endDate":"2020-09-23",
      |            "amount":45646.78,
      |            "taxPaid":4544.34
      |         }
      |      ],
      |      "jobSeekersAllowance":[
      |         {
      |            "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |            "startDate":"2018-07-17",
      |            "submittedOn":"2020-11-17T19:23:00Z",
      |            "endDate":"2020-09-23",
      |            "amount":45646.78,
      |            "taxPaid":4544.34
      |         }
      |      ],
      |      "bereavementAllowance":{
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |         "startDate":"2018-07-17",
      |         "submittedOn":"2020-11-17T19:23:00Z",
      |         "endDate":"2020-09-23",
      |         "amount":45646.78,
      |         "taxPaid":4544.34
      |      },
      |      "otherStateBenefits":{
      |         "benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |         "startDate":"2018-07-17",
      |         "submittedOn":"2020-11-17T19:23:00Z",
      |         "endDate":"2020-09-23",
      |         "amount":45646.78,
      |         "taxPaid":4544.34
      |      }
      |   }
      |}
      |""".stripMargin
  )

  val minimalStateBenefits: GetStateBenefitsModel = GetStateBenefitsModel(
    Some(StateBenefits(None, None,
      Some(defaultFullStateBenefit.copy(dateIgnored = None, submittedOn =
        None, endDate = None, amount = None, taxPaid = None))
      , None, None, None, None)), None)

  val minimalStateBenefitsJson: JsValue = Json.parse(
    """
      |{"stateBenefits":
      |   {"statePensionLumpSum":
      |     {"benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |     "startDate":"2019-11-13"
      |     }
      |   }
      |}
      |""".stripMargin
  )

  val minimalCustomerStateBenefits: GetStateBenefitsModel =
    GetStateBenefitsModel(
      None,
      Some(CustomerStateBenefits(None,
        Some(defaultFullCustomerAddedStateBenefit.copy(submittedOn = None,
          endDate = None, amount = None, taxPaid = None))
        , None, None, None, None, None)))

  val minimalCustomerStateBenefitsJson: JsValue = Json.parse(
    """
      |{"customerAddedStateBenefits":
      |   {"statePension":
      |       {"benefitId":"a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |        "startDate":"2018-07-17"
      |       }
      |    }
      | }
      |""".stripMargin
  )

  "The GetStateBenefitsModel" should {
    val fullCustomerStateBenefitsModel: CustomerStateBenefits =
      CustomerStateBenefits(
        Some(List(defaultFullCustomerAddedStateBenefit)),
        Some(defaultFullCustomerAddedStateBenefit),
        Some(defaultFullCustomerAddedStateBenefit),
        Some(List(defaultFullCustomerAddedStateBenefit)),
        Some(List(defaultFullCustomerAddedStateBenefit)),
        Some(defaultFullCustomerAddedStateBenefit),
        Some(defaultFullCustomerAddedStateBenefit)
      )

    val fullStateBenefitsModel: StateBenefits = StateBenefits(
      Some(List(defaultFullStateBenefit)),
      Some(defaultFullStateBenefit),
      Some(defaultFullStateBenefit),
      Some(List(defaultFullStateBenefit)),
      Some(List(defaultFullStateBenefit)),
      Some(defaultFullStateBenefit),
      Some(defaultFullStateBenefit)
    )

    "serialize valid values" when {

      "there is a full model" in {
        val model = GetStateBenefitsModel(
          Some(fullStateBenefitsModel),
          Some(fullCustomerStateBenefitsModel))
        Json.toJson(model) mustBe fullGetStateBenefitsJson
      }
      "the state benefits model is missing the state benefits" in {
        val model = GetStateBenefitsModel(None,
          Some(fullCustomerStateBenefitsModel)
        )
        Json.toJson(model) mustBe modelWithoutStateBenefitJson
      }
      "the state benefits model is missing the customer added benefits" in {
        val model = GetStateBenefitsModel(
          Some(fullStateBenefitsModel),
          None
        )
        Json.toJson(model) mustBe modelWithoutCustomerAddedBenefitsJson
      }
      "there is a minimal state benefits model" in {
        Json.toJson(minimalStateBenefits) mustBe minimalStateBenefitsJson
      }
      "there is a minimal customer state benefits model" in {
        Json.toJson(minimalCustomerStateBenefits) mustBe
          minimalCustomerStateBenefitsJson
      }
    }

    "deserialize valid values" when {
      "parsing full pension charges json" in {
        fullGetStateBenefitsJson.as[GetStateBenefitsModel] mustBe
          GetStateBenefitsModel(
            Some(fullStateBenefitsModel),
            Some(fullCustomerStateBenefitsModel)
          )
      }
      "parsing and there are no customer state added benefits" in {
        modelWithoutCustomerAddedBenefitsJson.as[GetStateBenefitsModel] mustBe
          GetStateBenefitsModel(
            Some(fullStateBenefitsModel), None)
      }
      "parsing and there no state benefits" in {
        modelWithoutStateBenefitJson.as[GetStateBenefitsModel] mustBe
          GetStateBenefitsModel(
            None, Some(fullCustomerStateBenefitsModel))
      }
      "parsing when there is minimal state benefits model" in {
        minimalStateBenefitsJson.as[GetStateBenefitsModel] mustBe
          minimalStateBenefits
      }
      "parsing when there is minimal customer state benefits model" in {
        minimalCustomerStateBenefitsJson.as[GetStateBenefitsModel] mustBe
          minimalCustomerStateBenefits
      }
    }
  }

}
