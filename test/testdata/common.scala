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

import models.common.{Mtditid, Nino, TaxYear}

import java.util.UUID

object common {
  val uuid             = UUID.fromString("f1b9f4b2-3f3e-4b1b-8b1b-3b1b1b1b1b1b")
  val nino: Nino       = Nino("123456789")
  val taxYear: TaxYear = TaxYear(2021)
  val mtditid: Mtditid = Mtditid("mtditid")
}
