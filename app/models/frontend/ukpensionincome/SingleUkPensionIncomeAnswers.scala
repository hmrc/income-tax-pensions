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

package models.frontend.ukpensionincome

import play.api.libs.json.{Json, OFormat}

final case class SingleUkPensionIncomeAnswers(
    employmentId: Option[String],
    pensionId: Option[String],
    startDate: Option[String],
    endDate: Option[String],
    pensionSchemeName: Option[String],
    pensionSchemeRef: Option[String],
    amount: Option[BigDecimal],
    taxPaid: Option[BigDecimal],
    isCustomerEmploymentData: Option[Boolean]
)

object SingleUkPensionIncomeAnswers {
  implicit val format: OFormat[SingleUkPensionIncomeAnswers] = Json.format[SingleUkPensionIncomeAnswers]
}
