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

package models.commonTaskList

import models.domain.AllJourneys
import org.scalatest.wordspec.AnyWordSpecLike
import testdata.appConfig.createAppConfig
import testdata.commonTaskList.emptyCommonTaskListModel
import utils.TestUtils.currTaxYear

class commonTaskListSpec extends AnyWordSpecLike {

  "fromAllJourneys" should {
    "generate TaskListModel in checkNow status for no data in journeys" in {
      val all       = AllJourneys.empty
      val appConfig = createAppConfig()
      val url       = appConfig.incomeTaxPensionsFrontendUrl

      val actual = fromAllJourneys(all, url, currTaxYear)

      assert(actual === emptyCommonTaskListModel(currTaxYear))
    }
  }
}
