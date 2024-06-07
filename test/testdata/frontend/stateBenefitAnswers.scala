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

package testdata.frontend

import models.frontend.statepension.StateBenefitAnswers

object stateBenefitAnswers {
  val sample = StateBenefitAnswers(
    benefitId = Some(java.util.UUID.fromString("f1b9f4b2-3f3e-4b1b-8b1b-3b1b1b1b1b1b")),
    startDateQuestion = Some(true),
    startDate = Some(java.time.LocalDate.parse("2019-04-23")),
    amountPaidQuestion = Some(true),
    amount = Some(300.0),
    taxPaidQuestion = Some(true),
    taxPaid = Some(400.0)
  )
}
