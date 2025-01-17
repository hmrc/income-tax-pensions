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

package services

import mocks.{MockPensionChargesConnector, MockPensionIncomeConnector, MockPensionReliefsConnector}
import models.common.TaxYear
import models.commonTaskList.TaskStatus.CheckNow
import models.commonTaskList.taskItemTitles.{PaymentsIntoPensionsTitles, PensionsTitles}
import models.commonTaskList.{SectionTitle, TaskListSection, TaskListSectionItem}
import org.scalatest.EitherValues._
import org.scalatest.wordspec.AnyWordSpecLike
import stubs.repositories.StubJourneyAnswersRepository
import stubs.services.{StubEmploymentService, StubJourneyStatusService, StubStateBenefitService}
import testdata.appConfig.createAppConfig
import utils.EitherTTestOps.convertScalaFuture
import utils.TestUtils.{hc, journeyCtxWithNino}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class PensionsServiceSpec extends AnyWordSpecLike {

  val currentTaxYear: TaxYear = TaxYear(LocalDate.now().getYear)
  val baseURL                 = s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$currentTaxYear"

  "getCommonTaskList" should {
    "return all journeys as NotStarted if no data" in {
      val appConfig = createAppConfig()

      val stateBenfitService = StubStateBenefitService()
      val employmentService  = StubEmploymentService()
      val statusService      = StubJourneyStatusService()
      val repository         = StubJourneyAnswersRepository()
      val mocks = new MockPensionReliefsConnector with MockPensionChargesConnector with MockPensionIncomeConnector {
        mockGetPensionReliefsT(Right(None))
        mockGetPensionChargesT(Right(None))
        mockGetPensionIncomeT(Right(None))
      }

      val underTest = new PensionsServiceImpl(
        appConfig,
        mocks.mockReliefsConnector,
        mocks.mockChargesConnector,
        stateBenfitService,
        mocks.mockIncomeConnector,
        employmentService,
        statusService,
        repository
      )

      val actual = underTest.getCommonTaskList(journeyCtxWithNino).value.futureValue.value

      assert(
        actual === List(
          TaskListSection(
            SectionTitle.PensionsTitle(),
            Some(List(
              TaskListSectionItem(PensionsTitles.StatePension(), CheckNow(), Some(s"$baseURL/pension-income/state-pension")),
              TaskListSectionItem(
                PensionsTitles.OtherUkPensions(),
                CheckNow(),
                Some(s"$baseURL/pension-income/uk-pension-income")
              ),
              TaskListSectionItem(
                PensionsTitles.UnauthorisedPayments(),
                CheckNow(),
                Some(s"$baseURL/unauthorised-payments-from-pensions/unauthorised-payments")
              ),
              TaskListSectionItem(
                PensionsTitles.ShortServiceRefunds(),
                CheckNow(),
                Some(s"$baseURL/overseas-pensions/short-service-refunds/taxable-short-service-refunds")
              ),
              TaskListSectionItem(
                PensionsTitles.IncomeFromOverseas(),
                CheckNow(),
                Some(s"$baseURL/overseas-pensions/income-from-overseas-pensions/pension-overseas-income-status")
              )
            ))
          ),
          TaskListSection(
            SectionTitle.PaymentsIntoPensionsTitle(),
            Some(List(
              TaskListSectionItem(
                PaymentsIntoPensionsTitles.PaymentsIntoUk(),
                CheckNow(),
                Some(s"$baseURL/payments-into-pensions/relief-at-source")
              ),
              TaskListSectionItem(
                PaymentsIntoPensionsTitles.AnnualAllowances(),
                CheckNow(),
                Some(s"$baseURL/annual-allowance/reduced-annual-allowance")
              ),
              TaskListSectionItem(
                PaymentsIntoPensionsTitles.PaymentsIntoOverseas(),
                CheckNow(),
                Some(s"$baseURL/overseas-pensions/payments-into-overseas-pensions/payments-into-schemes")
              ),
              TaskListSectionItem(
                PaymentsIntoPensionsTitles.OverseasTransfer(),
                CheckNow(),
                Some(s"$baseURL/overseas-pensions/overseas-transfer-charges/transfer-pension-savings")
              )
            ))
          )
        ))
    }

  }
}
