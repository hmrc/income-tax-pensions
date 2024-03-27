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

package utils

import cats.implicits.catsSyntaxOptionId
import models.employment.{AllEmploymentData, EmploymentSource}

object AllEmploymentsDataBuilder {

  private val employmentSource = EmploymentSource(
    employmentId = "some_id",
    employerName = "some name",
    employerRef = "some_ref".some,
    payrollId = "some_id".some,
    startDate = "2020-01-01".some,
    cessationDate = "2021-01-01".some,
    dateIgnored = None,
    submittedOn = "2024-03-21T13:42:40.433965Z".some,
    employmentData = None,
    employmentBenefits = None,
    occupationalPension = true.some
  )

  val allEmploymentsData = AllEmploymentData(
    hmrcEmploymentData = Seq.empty,
    hmrcExpenses = None,
    customerEmploymentData = Seq(employmentSource),
    customerExpenses = None,
    otherEmploymentIncome = None
  )
}
