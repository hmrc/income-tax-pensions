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

package models.frontend

import org.scalatest.wordspec.AnyWordSpecLike
import testdata.annualAllowances._

class AnnualAllowancesAnswersSpec extends AnyWordSpecLike {

  "toPensionChargesContributions" should {
    "convert answers to a Pension Contributions" in {
      assert(annualAllowancesAnswers.toPensionChargesContributions === pensionContributions)
    }
    "convert a 'No' journey to a Pension Contributions" in {
      assert(annualAllowancesAnswersNoJourney.toPensionChargesContributions === pensionContributionsNoJourney)
    }
    "convert empty answers to an empty Pension Contributions" in {
      assert(annualAllowancesEmptyAnswers.toPensionChargesContributions === emptyPensionContributions)
    }
  }
}
