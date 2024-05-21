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

package testdata

import models.charges.PensionContributions
import models.database.AnnualAllowancesStorageAnswers
import models.frontend.AnnualAllowancesAnswers

object annualAllowances {
  val annualAllowancesAnswers: AnnualAllowancesAnswers =
    AnnualAllowancesAnswers(Some(true), Some(true), Some(true), Some(true), Some(1.0), Some(true), Some(2.0), Some(Seq("12345678RA", "87654321RX")))
  val annualAllowancesEmptyAnswers: AnnualAllowancesAnswers =
    AnnualAllowancesAnswers(None, None, None, None, None, None, None, None)
  val annualAllowancesAnswersNoJourney: AnnualAllowancesAnswers =
    AnnualAllowancesAnswers(Some(false), None, None, None, None, None, None, None)

  val annualAllowancesStorageAnswers = AnnualAllowancesStorageAnswers(Some(true), Some(true))

  val pensionContributions: PensionContributions =
    PensionContributions(Seq("12345678RA", "87654321RX"), BigDecimal(1), BigDecimal(2), Some(true), Some(true), Some(true))
  val pensionContributionsNoJourney: PensionContributions =
    PensionContributions(Seq.empty, BigDecimal(0), BigDecimal(0), Some(false), None, None)
  val emptyPensionContributions: PensionContributions =
    PensionContributions(Seq.empty, BigDecimal(0), BigDecimal(0), None, None, None)
}
