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

package models.submission

import cats.implicits.catsSyntaxOptionId
import models.employment.AllEmploymentData
import play.api.libs.json.{Json, OFormat}

final case class EmploymentPensions(employmentData: Seq[EmploymentPensionModel])

object EmploymentPensions {
  implicit val format: OFormat[EmploymentPensions] = Json.format[EmploymentPensions]

  /** Case class for converting the raw employment data response we retrieve from income-tax-employments into a model containing data only relevant
    * for pensions. This is what will be sent over the wire to income-tax-submission
    */
  // TODO: Check that the mapping of these fields is correct
  def fromEmploymentResponse(resp: AllEmploymentData): EmploymentPensions =
    EmploymentPensions(
      employmentData = resp.customerEmploymentData.map { e =>
        EmploymentPensionModel(
          employmentId = e.employmentId,
          pensionSchemeName = e.employerName,
          pensionSchemeRef = e.employerRef,
          pensionId = e.payrollId,
          startDate = e.startDate,
          endDate = e.cessationDate,
          amount = e.employmentData.flatMap(_.pay.flatMap(_.taxablePayToDate)),
          taxPaid = e.employmentData.flatMap(_.pay.flatMap(_.totalTaxToDate)),
          isCustomerEmploymentData = true.some // Can we have hmrc employments?
        )
      }
    )
}
