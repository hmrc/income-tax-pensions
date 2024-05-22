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

import models.database.PaymentsIntoPensionsStorageAnswers
import models.frontend.PaymentsIntoPensionsAnswers

object paymentsIntoPensions {

  val paymentsIntoPensionsAnswers: PaymentsIntoPensionsAnswers =
    PaymentsIntoPensionsAnswers(true, Some(1.0), Some(true), Some(2.0), true, Some(true), Some(3.0), Some(true), Some(4.0))

  val paymentsIntoPensionsStorageAnswers: PaymentsIntoPensionsStorageAnswers =
    PaymentsIntoPensionsStorageAnswers(
      rasPensionPaymentQuestion = true,
      Some(true),
      pensionTaxReliefNotClaimedQuestion = true,
      Some(true),
      Some(true))

}
