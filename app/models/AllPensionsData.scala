/*
 * Copyright 2023 HM Revenue & Customs
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

package models

import models.submission.EmploymentPensions
import play.api.libs.json.{Json, OFormat}

case class AllPensionsData(pensionReliefs: Option[GetPensionReliefsModel],
                           pensionCharges: Option[GetPensionChargesRequestModel],
                           stateBenefits: Option[AllStateBenefitsData],
                           employmentPensions: Option[EmploymentPensions],
                           pensionIncome: Option[GetPensionIncomeModel]) {
  def isEmpty: Boolean =
    pensionReliefs.isEmpty &&
      pensionCharges.isEmpty &&
      stateBenefits.isEmpty &&
      employmentPensions.isEmpty &&
      pensionIncome.isEmpty
}

object AllPensionsData {
  implicit val formats: OFormat[AllPensionsData] = Json.format[AllPensionsData]

  def empty: AllPensionsData = AllPensionsData(None, None, None, None, None)

}
