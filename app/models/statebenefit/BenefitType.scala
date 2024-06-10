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

package models.statebenefit

sealed trait BenefitType {
  val value: String
}

object BenefitType {
  case object StatePension extends BenefitType {
    override val value: String = "statePension"
  }
  case object StatePensionLumpSum extends BenefitType {
    override val value: String = "statePensionLumpSum"
  }

  val values: List[BenefitType] = List(StatePension, StatePensionLumpSum)
}
