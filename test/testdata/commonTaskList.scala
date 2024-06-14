package testdata

import models.common.TaxYear
import models.commonTaskList._

object commonTaskList {
  def emptyCommonTaskListModel(taxYear: TaxYear): TaskListModel =
    TaskListModel(
      List(
        TaskListSection(
          SectionTitle.PensionsTitle(),
          Some(List(
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
          Some(List(
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
      ))

}
