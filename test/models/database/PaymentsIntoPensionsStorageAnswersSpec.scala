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

import models.frontend.PaymentsIntoPensionsAnswers
import org.scalatest.wordspec.AnyWordSpecLike
import PaymentsIntoPensionsStorageAnswers._

class PaymentsIntoPensionsStorageAnswersSpec extends AnyWordSpecLike {

  "fromJourneyAnswers" should {
    "convert answers to a storage model" in {
      val answers =
        PaymentsIntoPensionsAnswers(true, Some(1.0), Some(true), Some(2.0), true, Some(true), Some(3.0), Some(true), Some(4.0))
      val result = fromJourneyAnswers(answers)
      assert(result === PaymentsIntoPensionsStorageAnswers(true, Some(true), true, Some(true), Some(true)))
    }
  }
}
