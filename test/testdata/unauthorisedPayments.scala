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

import models.database.UnauthorisedPaymentsStorageAnswers
import models.frontend.UnauthorisedPaymentsAnswers

object unauthorisedPayments {
  val unauthorisedPaymentsAnswers = UnauthorisedPaymentsAnswers(
    Some(true),
    Some(true),
    Some(1.0),
    Some(true),
    Some(2.0),
    Some(3.0),
    Some(true),
    Some(4.0),
    Some(true),
    Some(List("pstr1"))
  )

  val storageAnswers = UnauthorisedPaymentsStorageAnswers(Some(true), Some(false), Some(true), Some(true), Some(false))
}
