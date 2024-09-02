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

import cats.data.EitherT
import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId}
import com.codahale.metrics.SharedMetricRegistries
import connectors._
import connectors.httpParsers.GetPensionChargesHttpParser.GetPensionChargesResponse
import connectors.httpParsers.GetPensionIncomeHttpParser.GetPensionIncomeResponse
import connectors.httpParsers.GetPensionReliefsHttpParser.GetPensionReliefsResponse
import connectors.httpParsers.GetStateBenefitsHttpParser.GetStateBenefitsResponse
import mocks.{MockPensionChargesConnector, MockPensionIncomeConnector, MockPensionReliefsConnector}
import models._
import models.charges.{CreateUpdatePensionChargesRequestModel, GetPensionChargesRequestModel, OverseasPensionContributions}
import models.common._
import models.database._
import models.employment.AllEmploymentData
import models.error.ServiceError
import models.frontend.statepension.{IncomeFromPensionsStatePensionAnswers, StateBenefitAnswers}
import models.frontend.ukpensionincome.UkPensionIncomeAnswers
import models.frontend.{AnnualAllowancesAnswers, PaymentsIntoOverseasPensionsAnswers, PaymentsIntoPensionsAnswers}
import models.submission.EmploymentPensions
import org.scalatest.BeforeAndAfterEach
import org.scalatest.EitherValues._
import org.scalatest.OptionValues._
import play.api.http.Status.INTERNAL_SERVER_ERROR
import stubs.repositories.StubJourneyAnswersRepository
import stubs.services.{StubEmploymentService, StubJourneyStatusService, StubStateBenefitService}
import testdata.annualAllowances.{annualAllowancesAnswers, annualAllowancesStorageAnswers, pensionContributions}
import testdata.commonTaskList.emptyCommonTaskListModel
import testdata.connector.stateBenefits
import testdata.frontend.stateBenefitAnswers
import testdata.incomeFromOverseasPensions.{foreignPension, incomeFromOverseasPensionsAnswers, incomeFromOverseasPensionsStorageAnswers}
import testdata.paymentsIntoOverseasPensions._
import testdata.paymentsIntoPensions.paymentsIntoPensionsAnswers
import testdata.shortServiceRefunds.{overseasPensionContributions, shortServiceRefundsAnswers, shortServiceRefundsCtxStorageAnswers}
import testdata.transfersIntoOverseasPensions._
import testdata.ukpensionincome.sampleSingleUkPensionIncome
import uk.gov.hmrc.http.HeaderCarrier
import utils.AllEmploymentsDataBuilder.allEmploymentsData
import utils.AllStateBenefitsDataBuilder.anAllStateBenefitsData
import utils.EitherTTestOps.convertScalaFuture
import utils.EmploymentPensionsBuilder.employmentPensionsData
import utils.TestUtils.currTaxYear
import utils.{EmploymentPensionsBuilder, TestUtils}

import scala.concurrent.Future

// TODO It can be simplified, less createPensionWithStubs creation, maybe switch to Stubs not mocks, and should be easier
class PensionsServiceImplSpec extends TestUtils with BeforeAndAfterEach {

  SharedMetricRegistries.clear()
  private val sampleCtx = JourneyContextWithNino(currTaxYear, Mtditid(mtditid), TestUtils.nino)

  val mockStateBenefitsService: StateBenefitService  = mock[StateBenefitService]
  val pensionIncomeConnector: PensionIncomeConnector = mock[PensionIncomeConnector]
  val stubRepository: StubJourneyAnswersRepository   = StubJourneyAnswersRepository()
  val stubEmploymentService: StubEmploymentService   = StubEmploymentService()
  val stubStatusService: StubJourneyStatusService    = StubJourneyStatusService()
  val stubStateBenefit: StubStateBenefitService      = StubStateBenefitService()

  var mocks: MockPensionReliefsConnector with MockPensionChargesConnector with MockPensionIncomeConnector = new MockPensionReliefsConnector
    with MockPensionChargesConnector
    with MockPensionIncomeConnector

  def createPensionWithStubs(
      stubEmploymentService: StubEmploymentService,
      stubStateBenefitService: StateBenefitService = StubStateBenefitService(),
      stubStatusService: StubJourneyStatusService = StubJourneyStatusService()
  ) =
    new PensionsServiceImpl(
      mockAppConfig,
      mocks.mockReliefsConnector,
      mocks.mockChargesConnector,
      stubStateBenefitService,
      mocks.mockIncomeConnector,
      stubEmploymentService,
      stubStatusService,
      stubRepository
    )

  def createPensionWithStubStateBenefit(stub: StubStateBenefitService) =
    new PensionsServiceImpl(
      mockAppConfig,
      mocks.mockReliefsConnector,
      mocks.mockChargesConnector,
      stub,
      mocks.mockIncomeConnector,
      stubEmploymentService,
      stubStatusService,
      stubRepository
    )

  def service: PensionsService         = createPensionWithStubs(stubEmploymentService)
  def serviceWithMock: PensionsService = createPensionWithStubs(stubEmploymentService, mockStateBenefitsService)

  val expectedReliefsResult: GetPensionReliefsResponse                        = Right(Some(fullPensionReliefsModel))
  val expectedChargesResult: GetPensionChargesResponse                        = Right(Some(fullPensionChargesModel))
  val expectedStateBenefitsResult: GetStateBenefitsResponse                   = Right(Some(anAllStateBenefitsData))
  val expectedEmploymentsResult: DownstreamErrorOr[Option[AllEmploymentData]] = Right(Some(allEmploymentsData))
  val expectedPensionIncomeResult: GetPensionIncomeResponse                   = Right(Some(fullPensionIncomeModel))

  override def beforeEach(): Unit = {
    mocks = new MockPensionReliefsConnector with MockPensionChargesConnector with MockPensionIncomeConnector
    stubRepository.testOnlyClearAllData()
    super.beforeEach()
  }

  "getAllPensionsData" should {
    "get all data and return a full AllPensionsData model" in {
      (mocks.mockReliefsConnector
        .getPensionReliefs(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedReliefsResult))

      (mocks.mockChargesConnector
        .getPensionCharges(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedChargesResult))

      (mockStateBenefitsService
        .getStateBenefits(_: JourneyContextWithNino)(_: HeaderCarrier))
        .expects(*, *)
        .returning(EitherT.rightT[Future, ServiceError](Some(anAllStateBenefitsData)))

      (mocks.mockIncomeConnector
        .getPensionIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedPensionIncomeResult))

      val result = await(serviceWithMock.getAllPensionsData(nino, taxYear, mtditid))

      result mustBe Right(fullPensionsModel)
    }

    "return a Right when all except EmploymentConnector return None" in {
      (mocks.mockReliefsConnector
        .getPensionReliefs(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(None)))

      (mocks.mockChargesConnector
        .getPensionCharges(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(None)))

      (mockStateBenefitsService
        .getStateBenefits(_: JourneyContextWithNino)(_: HeaderCarrier))
        .expects(*, *)
        .returning(EitherT.rightT[Future, ServiceError](None))

      (mocks.mockIncomeConnector
        .getPensionIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(None)))

      val result = await(serviceWithMock.getAllPensionsData(nino, taxYear, mtditid))

      result mustBe Right(AllPensionsData(None, None, None, Some(employmentPensionsData), None))
    }

    "return an error if a connector call fails" in {
      val expectedErrorResult: GetPensionChargesResponse = Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError))

      (mocks.mockReliefsConnector
        .getPensionReliefs(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedReliefsResult))

      (mocks.mockChargesConnector
        .getPensionCharges(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedErrorResult))

      val result = await(service.getAllPensionsData(nino, taxYear, mtditid))

      result mustBe expectedErrorResult

    }
  }

  "getPaymentsIntoPensions" should {
    val answers = PaymentsIntoPensionsStorageAnswers(
      rasPensionPaymentQuestion = true,
      Some(true),
      pensionTaxReliefNotClaimedQuestion = true,
      Some(true),
      Some(true))

    "get None if no answers" in {
      mocks.mockGetPensionReliefsT(Right(None))
      val result = service.getPaymentsIntoPensions(sampleCtx).value.futureValue
      assert(result.value === None)
    }

    // TODO It is not valid situation, we probably need to think how we want to handle mismatches IFS vs our DB
    "get answers if there are DB answers, but IFS return None (favour IFS)" in {
      mocks.mockGetPensionReliefsT(Right(None))

      val result = (for {
        _   <- stubRepository.upsertPaymentsIntoPensions(sampleCtx, answers)
        res <- service.getPaymentsIntoPensions(sampleCtx)
      } yield res).value.futureValue.value

      assert(result === Some(PaymentsIntoPensionsAnswers(false, None, Some(true), None, true, Some(true), None, Some(true), None)))
    }

    "return answers" in {
      mocks.mockGetPensionReliefsT(
        Right(Some(GetPensionReliefsModel("unused", None, PensionReliefs(Some(1.0), Some(2.0), Some(3.0), Some(4.0), None)))))
      val result = (for {
        _   <- stubRepository.upsertPaymentsIntoPensions(sampleCtx, answers)
        res <- service.getPaymentsIntoPensions(sampleCtx)
      } yield res).value.futureValue.value

      assert(
        result.value === PaymentsIntoPensionsAnswers(
          rasPensionPaymentQuestion = true,
          Some(1.0),
          Some(true),
          Some(2.0),
          pensionTaxReliefNotClaimedQuestion = true,
          Some(true),
          Some(3.0),
          Some(true),
          Some(4.0)))
    }
  }

  "upsertPaymentsIntoPensions" should {
    "upsert answers if overseasPensionSchemeContributions does not exist" in {
      val overseasPensionSchemeContributions: Option[BigDecimal] = None
      mocks.mockGetPensionReliefsT(Right(None))
      mocks.mockCreateOrAmendPensionReliefsT(
        Right(None),
        expectedModel =
          CreateOrUpdatePensionReliefsModel(PensionReliefs(Some(1.0), Some(2.0), Some(3.0), Some(4.0), overseasPensionSchemeContributions))
      )

      service.upsertPaymentsIntoPensions(sampleCtx, paymentsIntoPensionsAnswers).value.futureValue

      val persistedAnswers = stubRepository.getPaymentsIntoPensionsRes.value.value
      assert(
        persistedAnswers === PaymentsIntoPensionsStorageAnswers(
          rasPensionPaymentQuestion = true,
          Some(true),
          pensionTaxReliefNotClaimedQuestion = true,
          Some(true),
          Some(true)))
    }

    "upsert answers if overseasPensionSchemeContributions exist" in {
      val overseasPensionSchemeContributions: Option[BigDecimal] = Some(5.0)
      mocks.mockGetPensionReliefsT(
        Right(Some(GetPensionReliefsModel("unused", None, PensionReliefs(None, None, None, None, overseasPensionSchemeContributions)))))

      mocks.mockCreateOrAmendPensionReliefsT(
        Right(None),
        expectedModel =
          CreateOrUpdatePensionReliefsModel(PensionReliefs(Some(1.0), Some(2.0), Some(3.0), Some(4.0), overseasPensionSchemeContributions))
      )

      service.upsertPaymentsIntoPensions(sampleCtx, paymentsIntoPensionsAnswers).value.futureValue

      val persistedAnswers = stubRepository.getPaymentsIntoPensionsRes.value.value
      assert(
        persistedAnswers === PaymentsIntoPensionsStorageAnswers(
          rasPensionPaymentQuestion = true,
          Some(true),
          pensionTaxReliefNotClaimedQuestion = true,
          Some(true),
          Some(true)))
    }

  }

  "getUkPensionIncome" should {
    "get None if No downstream and DB answers" in {
      val service = createPensionWithStubs(
        StubEmploymentService(
          loadEmploymentResult = EmploymentPensions(Nil).asRight
        ))

      val result = service.getUkPensionIncome(sampleCtx).value.futureValue

      assert(result.value === None)
    }

    "get uKPensionIncomesQuestion=false with no incomes if no downstream answers, but if db answers exist" in {
      val answers = UkPensionIncomeStorageAnswers(true) // it doesn't matter if true or false. IT will be false if no incomes
      val service = createPensionWithStubs(
        StubEmploymentService(
          loadEmploymentResult = EmploymentPensions(Nil).asRight
        ))

      val result = (for {
        _   <- stubRepository.upsertUkPensionIncome(sampleCtx, answers)
        res <- service.getUkPensionIncome(sampleCtx)
      } yield res).value.futureValue

      assert(result.value === Some(UkPensionIncomeAnswers(uKPensionIncomesQuestion = false, Nil)))
    }

    "get uKPensionIncomesQuestion=false with no incomes" in {
      val answers = UkPensionIncomeStorageAnswers(true) // it doesn't matter if true or false. IT will be false if no incomes
      val service = createPensionWithStubs(
        StubEmploymentService(
          loadEmploymentResult = EmploymentPensionsBuilder.employmentPensionsData.asRight
        ))

      val result = (for {
        _   <- stubRepository.upsertUkPensionIncome(sampleCtx, answers)
        res <- service.getUkPensionIncome(sampleCtx)
      } yield res).value.futureValue

      assert(result.value === Some(UkPensionIncomeAnswers(uKPensionIncomesQuestion = true, List(sampleSingleUkPensionIncome))))
    }
  }

  "upsertUkPensionIncome" should {
    val employmentStub = StubEmploymentService(
      loadEmploymentResult = EmploymentPensionsBuilder.employmentPensionsData.asRight
    )
    val service = createPensionWithStubs(employmentStub)

    "upsert answers" in {
      val answers = UkPensionIncomeAnswers(uKPensionIncomesQuestion = true, List(sampleSingleUkPensionIncome))

      service.upsertUkPensionIncome(sampleCtx, answers).value.futureValue

      assert(
        employmentStub.ukPensionIncome === List(
          UkPensionIncomeAnswers(
            uKPensionIncomesQuestion = true,
            List(sampleSingleUkPensionIncome)
          )))

      val persistedAnswers = stubRepository.getUkPensionIncomeRes.value.value
      assert(persistedAnswers === UkPensionIncomeStorageAnswers(true))
    }
  }

  "getStatePension" should {
    "get None if No downstream and DB answers" in {
      val service = createPensionWithStubStateBenefit(stubStateBenefit)
      val result  = service.getStatePension(sampleCtx).value.futureValue.value

      assert(result === None)
    }

    "return answers from downstream even when DB answers does not exist" in {
      val service = createPensionWithStubStateBenefit(stubStateBenefit)
      val answers = IncomeFromPensionsStatePensionStorageAnswers(Some(true), Some(true))

      val result = (for {
        _   <- stubRepository.upsertStatePension(sampleCtx, answers)
        res <- service.getStatePension(sampleCtx)
      } yield res).value.futureValue.value

      assert(
        result ===
          Some(
            IncomeFromPensionsStatePensionAnswers(
              Some(StateBenefitAnswers(None, None, None, Some(true), None, None, None)),
              Some(StateBenefitAnswers(None, None, None, Some(true), None, None, None)),
              None)))

    }

    "get answers from downstream" in {
      val service = createPensionWithStubStateBenefit(
        StubStateBenefitService(
          getStateBenefitsResult = Right(Some(stateBenefits.allStateBenefitsData))
        ))

      val answers = IncomeFromPensionsStatePensionStorageAnswers(Some(true), Some(true))

      val result = (for {
        _   <- stubRepository.upsertStatePension(sampleCtx, answers)
        res <- service.getStatePension(sampleCtx)
      } yield res).value.futureValue.value

      assert(
        result === Some(
          IncomeFromPensionsStatePensionAnswers(
            Some(stateBenefitAnswers.sample),
            Some(stateBenefitAnswers.sample),
            None
          )))
    }
  }

  "upsertStatePension" should {}

  "getAnnualAllowances" should {
    "get None if no answers" in {
      mocks.mockGetPensionChargesT(Right(None))
      val result = service.getAnnualAllowances(sampleCtx).value.futureValue
      assert(result.value === None)
    }

    "get answers from DB, even if no IFS answers (best effort)" in {
      mocks.mockGetPensionChargesT(Right(None))

      val result = (for {
        _   <- stubRepository.upsertAnnualAllowances(sampleCtx, annualAllowancesStorageAnswers)
        res <- service.getAnnualAllowances(sampleCtx)
      } yield res).value.futureValue.value

      assert(
        result ===
          Some(AnnualAllowancesAnswers(None, None, None, Some(true), None, Some(true), None, None)))
    }

    "return answers" in {
      mocks.mockGetPensionChargesT(Right(Some(GetPensionChargesRequestModel("unused", None, None, Some(pensionContributions), None))))
      val result = (for {
        _   <- stubRepository.upsertAnnualAllowances(sampleCtx, annualAllowancesStorageAnswers)
        res <- service.getAnnualAllowances(sampleCtx)
      } yield res).value.futureValue.value

      assert(result.value === annualAllowancesAnswers)
    }
  }

  "upsertAnnualAllowances" should {
    "upsert answers " in {
      mocks.mockGetPensionChargesT(Right(None))
      mocks.mockCreateOrAmendPensionChargesT(
        Right(None),
        expectedModel = CreateUpdatePensionChargesRequestModel(None, None, Some(pensionContributions), None)
      )

      service.upsertAnnualAllowances(sampleCtx, annualAllowancesAnswers).value.futureValue

      val persistedAnswers = stubRepository.getAnnualAllowancesRes.value.value
      assert(persistedAnswers === annualAllowancesStorageAnswers)
    }
  }

  "getPaymentsIntoOverseasPensions" should {
    "get None if no answers" in {
      mocks.mockGetPensionReliefsT(Right(None))
      mocks.mockGetPensionIncomeT(Right(None))

      val result = service.getPaymentsIntoOverseasPensions(sampleCtx).value.futureValue
      assert(result.value === None)
    }

    "return a 'No' journey if IFS returns None but DB answers exist (regardless of the DB answers' values)" in {
      mocks.mockGetPensionReliefsT(Right(None))
      mocks.mockGetPensionIncomeT(Right(None))

      val storageAnswers = PaymentsIntoOverseasPensionsStorageAnswers(Some(true), Some(true), Some(true))
      val result = (for {
        _   <- stubRepository.upsertPaymentsIntoOverseasPensions(sampleCtx, storageAnswers)
        res <- service.getPaymentsIntoOverseasPensions(sampleCtx)
      } yield res).value.futureValue.value

      val expectedResult = PaymentsIntoOverseasPensionsAnswers(Some(false), None, None, None, List.empty).some

      assert(result === expectedResult)
    }

    "return answers" in {
      mocks.mockGetPensionReliefsT(
        Right(Some(GetPensionReliefsModel("unused", None, PensionReliefs.empty.copy(overseasPensionSchemeContributions = Some(2))))))
      mocks.mockGetPensionIncomeT(
        Right(Some(GetPensionIncomeModel("unused", None, None, Some(Seq(mmrOverseasPensionContribution, tcrOverseasPensionContribution))))))

      val result = (for {
        _   <- stubRepository.upsertPaymentsIntoOverseasPensions(sampleCtx, piopStorageAnswers)
        res <- service.getPaymentsIntoOverseasPensions(sampleCtx)
      } yield res).value.futureValue.value

      assert(result.value === paymentsIntoOverseasPensionsAnswers)
    }
  }

  "upsertPaymentsIntoOverseasPensions" should {
    "upsert answers " in {
      mocks.mockGetPensionReliefsT(Right(None))
      mocks.mockGetPensionIncomeT(Right(None))
      mocks.mockCreateOrAmendPensionReliefsT(
        Right(None),
        expectedModel = CreateOrUpdatePensionReliefsModel(PensionReliefs.empty.copy(overseasPensionSchemeContributions = Some(2.0)))
      )
      mocks.mockCreateOrAmendPensionIncomeT(
        Right(None),
        expectedModel = CreateUpdatePensionIncomeModel(None, Some(Seq(mmrOverseasPensionContribution, tcrOverseasPensionContribution)))
      )

      service.upsertPaymentsIntoOverseasPensions(sampleCtx, paymentsIntoOverseasPensionsAnswers).value.futureValue

      val persistedAnswers = stubRepository.getPaymentsIntoOverseasPensionsRes.value.value
      assert(persistedAnswers === piopStorageAnswers)
    }
  }

  "getTransfersIntoOverseasPensions" should {
    "get None if no answers" in {
      mocks.mockGetPensionChargesT(Right(None))
      val result = service.getTransfersIntoOverseasPensions(sampleCtx).value.futureValue
      assert(result.value === None)
    }

    "return answers" in {
      mocks.mockGetPensionChargesT(Right(Some(GetPensionChargesRequestModel("unused", Some(pensionSchemeOverseasTransfers), None, None, None))))
      val result = (for {
        _   <- stubRepository.upsertTransferIntoOverseasPensions(sampleCtx, transfersIntoOverseasPensionsStorageAnswers)
        res <- service.getTransfersIntoOverseasPensions(sampleCtx)
      } yield res).value.futureValue.value

      assert(result.value === transfersIntoOverseasPensionsAnswers)
    }
  }

  "upsertTransfersIntoOverseasPensions" should {
    "upsert answers " in {
      mocks.mockGetPensionChargesT(Right(None))
      mocks.mockCreateOrAmendPensionChargesT(
        Right(None),
        expectedModel = CreateUpdatePensionChargesRequestModel.empty.copy(pensionSchemeOverseasTransfers = Some(pensionSchemeOverseasTransfers))
      )

      service.upsertTransfersIntoOverseasPensions(sampleCtx, transfersIntoOverseasPensionsAnswers).value.futureValue

      val persistedAnswers = stubRepository.getTransferIntoOverseasPensionsRes.value.value
      assert(persistedAnswers === transfersIntoOverseasPensionsStorageAnswers)
    }
  }

  "getIncomeFromOverseasPensions" should {
    "get None if no answers" in {
      mocks.mockGetPensionIncomeT(Right(None))
      val result = service.getIncomeFromOverseasPensions(sampleCtx).value.futureValue
      assert(result.value === None)
    }

    "return answers" in {
      mocks.mockGetPensionIncomeT(Right(Some(GetPensionIncomeModel("date", None, Some(Seq(foreignPension)), None))))
      val result = (for {
        _   <- stubRepository.upsertIncomeFromOverseasPensions(sampleCtx, incomeFromOverseasPensionsStorageAnswers)
        res <- service.getIncomeFromOverseasPensions(sampleCtx)
      } yield res).value.futureValue.value

      assert(result.value === incomeFromOverseasPensionsAnswers)
    }
  }

  "upsertIncomeFromOverseasPensions" should {
    "upsert answers " in {
      mocks.mockGetPensionIncomeT(Right(None))
      mocks.mockCreateOrAmendPensionIncomeT(
        Right(None),
        expectedModel = CreateUpdatePensionIncomeModel(Some(List(foreignPension)), None)
      )

      service.upsertIncomeFromOverseasPensions(sampleCtx, incomeFromOverseasPensionsAnswers).value.futureValue

      val persistedAnswers = stubRepository.getIncomeFromOverseasPensionsRes.value.value
      assert(persistedAnswers === incomeFromOverseasPensionsStorageAnswers)
    }
  }

  "getShortServiceRefunds" should {
    "get None if no answers" in {
      mocks.mockGetPensionChargesT(Right(None))
      val result = service.getShortServiceRefunds(sampleCtx).value.futureValue
      assert(result.value === None)
    }

    "get None even if there are some DB answers, but IFS return None (favour IFS)" in {
      mocks.mockGetPensionChargesT(Right(None))
      val result = (for {
        _   <- stubRepository.upsertShortServiceRefunds(sampleCtx, ShortServiceRefundsStorageAnswers())
        res <- service.getShortServiceRefunds(sampleCtx)
      } yield res).value.futureValue.value

      assert(result === None)
    }

    "return answers" in {
      mocks.mockGetPensionChargesT(
        Right(Some(GetPensionChargesRequestModel("unused", None, None, None, Some(overseasPensionContributions))))
      )
      val result = (for {
        _   <- stubRepository.upsertShortServiceRefunds(sampleCtx, shortServiceRefundsCtxStorageAnswers)
        res <- service.getShortServiceRefunds(sampleCtx)
      } yield res).value.futureValue.value

      assert(result.value === shortServiceRefundsAnswers)
    }
  }

  "upsertShortServiceRefunds" should {
    "insert answers if overseasPensionContributions does not exist" in {
      mocks.mockGetPensionChargesT(Right(None))
      mocks.mockCreateOrAmendPensionChargesT(
        Right(None),
        expectedModel = CreateUpdatePensionChargesRequestModel(None, None, None, Some(overseasPensionContributions))
      )

      service.upsertShortServiceRefunds(sampleCtx, shortServiceRefundsAnswers).value.futureValue

      val persistedAnswers = stubRepository.getShortServiceRefundsRes.value.value
      assert(persistedAnswers === shortServiceRefundsCtxStorageAnswers)
    }

    "update answers if overseasPensionContributions exist" in {
      mocks.mockGetPensionChargesT(
        Right(
          Some(
            GetPensionChargesRequestModel("unused", None, None, None, Some(OverseasPensionContributions(Seq(), BigDecimal(0.0), BigDecimal(0.0))))))
      )
      mocks.mockCreateOrAmendPensionChargesT(
        Right(None),
        expectedModel = CreateUpdatePensionChargesRequestModel(None, None, None, Some(overseasPensionContributions))
      )

      service.upsertShortServiceRefunds(sampleCtx, shortServiceRefundsAnswers).value.futureValue

      val persistedAnswers = stubRepository.getShortServiceRefundsRes.value.value
      assert(persistedAnswers === shortServiceRefundsCtxStorageAnswers)
    }
  }

  "getCommonTaskList" should {
    "return None when no data in DB and IFS" in {
      mocks.mockGetPensionReliefsT(Right(None))
      mocks.mockGetPensionChargesT(Right(None))
      mocks.mockGetPensionIncomeT(Right(None))

      val underTest: PensionsService = new PensionsServiceImpl(
        mockAppConfig,
        mocks.mockReliefsConnector,
        mocks.mockChargesConnector,
        StubStateBenefitService(),
        mocks.mockIncomeConnector,
        StubEmploymentService(Right(EmploymentPensions.empty)),
        StubJourneyStatusService(),
        StubJourneyAnswersRepository()
      )

      val result = underTest.getCommonTaskList(sampleCtx).value.futureValue.value

      val expected = emptyCommonTaskListModel(sampleCtx.taxYear)
      assert(result === expected)
    }

    "return a full task list in a proper status" in {
      val taxYear = sampleCtx.taxYear

      val stubStatusService: StubJourneyStatusService = StubJourneyStatusService(getAllStatusesResult = List(
        JourneyNameAndStatus(Journey.PaymentsIntoPensions, JourneyStatus.NotStarted),
        JourneyNameAndStatus(Journey.UkPensionIncome, JourneyStatus.InProgress),
        JourneyNameAndStatus(Journey.StatePension, JourneyStatus.Completed),
        JourneyNameAndStatus(Journey.AnnualAllowances, JourneyStatus.Completed),
        JourneyNameAndStatus(Journey.UnauthorisedPayments, JourneyStatus.Completed),
        JourneyNameAndStatus(Journey.PaymentsIntoOverseasPensions, JourneyStatus.Completed),
        JourneyNameAndStatus(Journey.IncomeFromOverseasPensions, JourneyStatus.Completed),
        JourneyNameAndStatus(Journey.TransferIntoOverseasPensions, JourneyStatus.Completed),
        JourneyNameAndStatus(Journey.ShortServiceRefunds, JourneyStatus.Completed)
      ))

      val underTest: PensionsService = new PensionsServiceImpl(
        mockAppConfig,
        mocks.mockReliefsConnector,
        mocks.mockChargesConnector,
        StubStateBenefitService(),
        mocks.mockIncomeConnector,
        StubEmploymentService(Right(EmploymentPensions.empty)),
        stubStatusService,
        StubJourneyAnswersRepository()
      )

      mocks.mockGetPensionReliefsT(Right(None))
      mocks.mockGetPensionChargesT(Right(None))
      mocks.mockGetPensionIncomeT(Right(None))

      val result = underTest.getCommonTaskList(sampleCtx).value.futureValue.value

      val expected = emptyCommonTaskListModel(taxYear)
      assert(result === expected)
    }
  }
}
