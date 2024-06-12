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

package models.database

import models.database.IncomeFromPensionsStatePensionStorageAnswers._
import models.frontend.statepension.IncomeFromPensionsStatePensionAnswers
import org.scalatest.wordspec.AnyWordSpecLike
import testdata.connector.stateBenefits.allStateBenefitsData
import testdata.database.incomeFromPensionsStatePensionStorageAnswers
import testdata.frontend.incomeFromPensionsStatePensionAnswers

class IncomeFromPensionsStatePensionStorageAnswersSpec extends AnyWordSpecLike {

  "toIncomeFromPensionsStatePensionAnswers" should {
    "return all answers" in {
      val result = incomeFromPensionsStatePensionStorageAnswers.sampleAnswers.toIncomeFromPensionsStatePensionAnswers(
        Some("sessionId"),
        Some(allStateBenefitsData))

      assert(result === incomeFromPensionsStatePensionAnswers.sample)
    }

    "overwrite answers from database with answers from IFS if needed" in {
      val result =
        IncomeFromPensionsStatePensionStorageAnswers(Some(false), Some(false))
          .toIncomeFromPensionsStatePensionAnswers(Some("sessionId"), Some(allStateBenefitsData))

      assert(result === incomeFromPensionsStatePensionAnswers.sample)
    }
  }

  "fromJourneyAnswers" should {
    "return empty answers" in {
      assert(fromJourneyAnswers(IncomeFromPensionsStatePensionAnswers.empty) === IncomeFromPensionsStatePensionStorageAnswers(None, None))
    }

    "convert answers from API to DB" in {
      assert(
        fromJourneyAnswers(incomeFromPensionsStatePensionAnswers.sample) === IncomeFromPensionsStatePensionStorageAnswers(Some(true), Some(true)))
    }

  }
}
