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

import models.database.UkPensionIncomeStorageAnswers
import models.frontend.ukpensionincome.SingleUkPensionIncomeAnswers

object ukpensionincome {
  val sampleSingleUkPensionIncome: SingleUkPensionIncomeAnswers = SingleUkPensionIncomeAnswers(
    Some("some_id"),
    Some("some_id"),
    Some("2020-01-01"),
    Some("2021-01-01"),
    Some("some name"),
    Some("some_ref"),
    None,
    None,
    Some(true)
  )

  val storageAnswers: UkPensionIncomeStorageAnswers = UkPensionIncomeStorageAnswers(true)

}
