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

import com.codahale.metrics.SharedMetricRegistries
import play.api.libs.json.{JsValue, Json}
import utils.TestUtils

class GetStateBenefitsModelSpec extends TestUtils {
  SharedMetricRegistries.clear()

  val jsonModel: JsValue = Json.parse {
    """
      |{
      |	"stateBenefits": {
      |		"incapacityBenefit": [{
      |			"submittedOn": "2020-09-11T17:23:00Z",
      |   	"dateIgnored": "2019-04-11T16:22:00Z",
      |			"benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |			"startDate": "2019-11-13",
      |			"endDate": "2020-08-23",
      |			"amount": 1212.34,
      |			"taxPaid": 22323.23
      |		}],
      |		"statePension": {
      |			"submittedOn": "2020-09-11T17:23:00Z",
      |   	"dateIgnored": "2019-04-11T16:22:00Z",
      |			"benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |			"startDate": "2019-11-13",
      |			"endDate": "2020-08-23",
      |			"amount": 1212.34,
      |			"taxPaid": 22323.23
      |		},
      |		"statePensionLumpSum": {
      |			"submittedOn": "2020-09-11T17:23:00Z",
      |   	"dateIgnored": "2019-04-11T16:22:00Z",
      |			"benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |			"startDate": "2019-11-13",
      |			"endDate": "2020-08-23",
      |			"amount": 1212.34,
      |			"taxPaid": 22323.23
      |		},
      |		"employmentSupportAllowance": [{
      |			"submittedOn": "2020-09-11T17:23:00Z",
      |   	"dateIgnored": "2019-04-11T16:22:00Z",
      |			"benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |			"startDate": "2019-11-13",
      |			"endDate": "2020-08-23",
      |			"amount": 1212.34,
      |			"taxPaid": 22323.23
      |		}],
      |		"jobSeekersAllowance": [{
      |			"submittedOn": "2020-09-11T17:23:00Z",
      |   	"dateIgnored": "2019-04-11T16:22:00Z",
      |			"benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |			"startDate": "2019-11-13",
      |			"endDate": "2020-08-23",
      |			"amount": 1212.34,
      |			"taxPaid": 22323.23
      |		}],
      |		"bereavementAllowance": {
      |			"submittedOn": "2020-09-11T17:23:00Z",
      |   	"dateIgnored": "2019-04-11T16:22:00Z",
      |			"benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |			"startDate": "2019-11-13",
      |			"endDate": "2020-08-23",
      |			"amount": 1212.34,
      |			"taxPaid": 22323.23
      |		},
      |		"otherStateBenefits": {
      |			"submittedOn": "2020-09-11T17:23:00Z",
      |   	"dateIgnored": "2019-04-11T16:22:00Z",
      |			"benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
      |			"startDate": "2019-11-13",
      |			"endDate": "2020-08-23",
      |			"amount": 1212.34,
      |			"taxPaid": 22323.23
      |		}
      |	},
      |	"customerAddedStateBenefits": {
      |		"incapacityBenefit": [{
      |			"submittedOn": "2020-09-11T17:23:00Z",
      |			"benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c935",
      |			"startDate": "2019-11-13",
      |			"endDate": "2020-08-23",
      |			"amount": 1212.34,
      |			"taxPaid": 22323.23
      |		}],
      |		"statePension": {
      |			"submittedOn": "2020-09-11T17:23:00Z",
      |			"benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c935",
      |			"startDate": "2019-11-13",
      |			"endDate": "2020-08-23",
      |			"amount": 1212.34,
      |			"taxPaid": 22323.23
      |		},
      |		"statePensionLumpSum": {
      |			"submittedOn": "2020-09-11T17:23:00Z",
      |			"benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c935",
      |			"startDate": "2019-11-13",
      |			"endDate": "2020-08-23",
      |			"amount": 1212.34,
      |			"taxPaid": 22323.23
      |		},
      |		"employmentSupportAllowance": [{
      |			"submittedOn": "2020-09-11T17:23:00Z",
      |			"benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c935",
      |			"startDate": "2019-11-13",
      |			"endDate": "2020-08-23",
      |			"amount": 1212.34,
      |			"taxPaid": 22323.23
      |		}],
      |		"jobSeekersAllowance": [{
      |			"submittedOn": "2020-09-11T17:23:00Z",
      |			"benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c935",
      |			"startDate": "2019-11-13",
      |			"endDate": "2020-08-23",
      |			"amount": 1212.34,
      |			"taxPaid": 22323.23
      |		}],
      |		"bereavementAllowance": {
      |			"submittedOn": "2020-09-11T17:23:00Z",
      |			"benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c935",
      |			"startDate": "2019-11-13",
      |			"endDate": "2020-08-23",
      |			"amount": 1212.34,
      |			"taxPaid": 22323.23
      |		},
      |		"otherStateBenefits": {
      |			"submittedOn": "2020-09-11T17:23:00Z",
      |			"benefitId": "a1e8057e-fbbc-47a8-a8b4-78d9f015c935",
      |			"startDate": "2019-11-13",
      |			"endDate": "2020-08-23",
      |			"amount": 1212.34,
      |			"taxPaid": 22323.23
      |		}
      |	}
      |}""".stripMargin
  }

  "GetStateBenefitsModel with all values" should {

    "parse to Json" in {
      Json.toJson(fullStateBenefitsModel) mustBe jsonModel
    }

    "parse from Json" in {
      jsonModel.as[GetStateBenefitsModel]
    }
  }
}
