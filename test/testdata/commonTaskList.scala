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

import models.common.TaxYear
import models.commonTaskList._

object commonTaskList {
  def emptyCommonTaskListModel(taxYear: TaxYear): Seq[TaskListSection] =
    Seq(
      TaskListSection(
        SectionTitle.PensionsTitle(),
        Some(
          List(
            TaskListSectionItem(
              TaskTitle.pensionsTitles.StatePension(),
              TaskStatus.CheckNow(),
              Some(s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/pension-income/state-pension")
            ),
            TaskListSectionItem(
              TaskTitle.pensionsTitles.OtherUkPensions(),
              TaskStatus.CheckNow(),
              Some(s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/pension-income/uk-pension-income")
            ),
            TaskListSectionItem(
              TaskTitle.pensionsTitles.UnauthorisedPayments(),
              TaskStatus.CheckNow(),
              Some(s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/unauthorised-payments-from-pensions/unauthorised-payments")
            ),
            TaskListSectionItem(
              TaskTitle.pensionsTitles.ShortServiceRefunds(),
              TaskStatus.CheckNow(),
              Some(s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/overseas-pensions/short-service-refunds/taxable-short-service-refunds")
            ),
            TaskListSectionItem(
              TaskTitle.pensionsTitles.IncomeFromOverseas(),
              TaskStatus.CheckNow(),
              Some(s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/overseas-pensions/income-from-overseas-pensions/pension-overseas-income-status")
            )
          ))
      ),
      TaskListSection(
        SectionTitle.PaymentsIntoPensionsTitle(),
        Some(
          List(
            TaskListSectionItem(
              TaskTitle.paymentsIntoPensionsTitles.PaymentsIntoUk(),
              TaskStatus.CheckNow(),
              Some(s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/payments-into-pensions/relief-at-source")
            ),
            TaskListSectionItem(
              TaskTitle.paymentsIntoPensionsTitles.AnnualAllowances(),
              TaskStatus.CheckNow(),
              Some(s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/annual-allowance/reduced-annual-allowance")
            ),
            TaskListSectionItem(
              TaskTitle.paymentsIntoPensionsTitles.PaymentsIntoOverseas(),
              TaskStatus.CheckNow(),
              Some(s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/overseas-pensions/payments-into-overseas-pensions/payments-into-schemes")
            ),
            TaskListSectionItem(
              TaskTitle.paymentsIntoPensionsTitles.OverseasTransfer(),
              TaskStatus.CheckNow(),
              Some(s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/overseas-pensions/overseas-transfer-charges/transfer-pension-savings")
            )
          ))
      )
    )

}
