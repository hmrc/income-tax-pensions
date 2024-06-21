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
import models.commonTaskList._
import models.database._
import models.employment.AllEmploymentData
import models.error.ServiceError
import models.frontend.statepension.IncomeFromPensionsStatePensionAnswers
import models.frontend.ukpensionincome.UkPensionIncomeAnswers
import models.frontend.{PaymentsIntoOverseasPensionsAnswers, PaymentsIntoPensionsAnswers}
import models.submission.EmploymentPensions
import org.scalatest.BeforeAndAfterEach
import org.scalatest.EitherValues._
import org.scalatest.OptionValues._
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.Json
import stubs.repositories.StubJourneyAnswersRepository
import stubs.services.{StubEmploymentService, StubJourneyStatusService, StubStateBenefitService}
import testdata.annualAllowances.{annualAllowancesAnswers, annualAllowancesStorageAnswers, pensionContributions}
import testdata.commonTaskList.emptyCommonTaskListModel
import testdata.connector.{getPensionReliefsModel, stateBenefits}
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
import utils.TestUtils.currTaxYear
import utils.{EmploymentPensionsBuilder, TestUtils}

import scala.concurrent.Future

class PensionsServiceImplSpec
    extends TestUtils
    with MockPensionReliefsConnector
    with MockPensionChargesConnector
    with MockPensionIncomeConnector
    with BeforeAndAfterEach {

  SharedMetricRegistries.clear()
  private val sampleCtx = JourneyContextWithNino(currTaxYear, Mtditid(mtditid), TestUtils.nino)

  val mockStateBenefitsService: StateBenefitService  = mock[StateBenefitService]
  val pensionIncomeConnector: PensionIncomeConnector = mock[PensionIncomeConnector]
  val stubRepository: StubJourneyAnswersRepository   = StubJourneyAnswersRepository()
  val stubEmploymentService: StubEmploymentService   = StubEmploymentService()
  val stubStatusService: StubJourneyStatusService    = StubJourneyStatusService()
  val stubStateBenefit: StubStateBenefitService      = StubStateBenefitService()

  def createPensionWithStubs(
      stubEmploymentService: StubEmploymentService,
      stubStateBenefitService: StubStateBenefitService = StubStateBenefitService(),
      stubStatusService: StubJourneyStatusService = StubJourneyStatusService()
  ) =
    new PensionsServiceImpl(
      mockAppConfig,
      mockReliefsConnector,
      mockChargesConnector,
      stubStateBenefitService,
      mockIncomeConnector,
      stubEmploymentService,
      stubStatusService,
      stubRepository
    )

  def createPensionWithStubStateBenefit(stub: StubStateBenefitService) =
    new PensionsServiceImpl(
      mockAppConfig,
      mockReliefsConnector,
      mockChargesConnector,
      stub,
      mockIncomeConnector,
      stubEmploymentService,
      stubStatusService,
      stubRepository
    )

  val service: PensionsService = createPensionWithStubs(stubEmploymentService)

  val expectedReliefsResult: GetPensionReliefsResponse                        = Right(Some(fullPensionReliefsModel))
  val expectedChargesResult: GetPensionChargesResponse                        = Right(Some(fullPensionChargesModel))
  val expectedStateBenefitsResult: GetStateBenefitsResponse                   = Right(Some(anAllStateBenefitsData))
  val expectedEmploymentsResult: DownstreamErrorOr[Option[AllEmploymentData]] = Right(Some(allEmploymentsData))
  val expectedPensionIncomeResult: GetPensionIncomeResponse                   = Right(Some(fullPensionIncomeModel))

  override def beforeEach(): Unit = {
    stubRepository.testOnlyClearAllData()
    super.beforeEach()
  }

  "getAllPensionsData" should {

    // TODO fix in https://jira.tools.tax.service.gov.uk/browse/SASS-8136
    "get all data and return a full AllPensionsData model" ignore {
      (mockReliefsConnector
        .getPensionReliefs(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedReliefsResult))

      (mockChargesConnector
        .getPensionCharges(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedChargesResult))

      (mockStateBenefitsService
        .getStateBenefits(_: JourneyContextWithNino)(_: HeaderCarrier))
        .expects(*, *)
        .returning(EitherT.rightT[Future, ServiceError](Some(anAllStateBenefitsData)))

      (mockIncomeConnector
        .getPensionIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedPensionIncomeResult))

      val result = await(service.getAllPensionsData(nino, taxYear, mtditid))

      result mustBe Right(fullPensionsModel)
    }

    // TODO fix in https://jira.tools.tax.service.gov.uk/browse/SASS-8136
    "return a Right if all connectors return None" ignore {
      (mockReliefsConnector
        .getPensionReliefs(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(None)))

      (mockChargesConnector
        .getPensionCharges(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(None)))

      (mockStateBenefitsService
        .getStateBenefits(_: JourneyContextWithNino)(_: HeaderCarrier))
        .expects(*, *)
        .returning(EitherT.rightT[Future, ServiceError](None))

      (mockIncomeConnector
        .getPensionIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(None)))

      val result = await(service.getAllPensionsData(nino, taxYear, mtditid))

      result mustBe Right(AllPensionsData(None, None, None, None, None))
    }

    "return an error if a connector call fails" in {
      val expectedErrorResult: GetPensionChargesResponse = Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError))

      (mockReliefsConnector
        .getPensionReliefs(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedReliefsResult))

      (mockChargesConnector
        .getPensionCharges(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedErrorResult))

      val result = await(service.getAllPensionsData(nino, taxYear, mtditid))

      result mustBe expectedErrorResult

    }
  }

  "getPaymentsIntoPensions" should {
    val paymentIntoPensionsCtx = sampleCtx.toJourneyContext(Journey.PaymentsIntoPensions)
    val answers = PaymentsIntoPensionsStorageAnswers(
      rasPensionPaymentQuestion = true,
      Some(true),
      pensionTaxReliefNotClaimedQuestion = true,
      Some(true),
      Some(true))

    "get None if no answers" in {
      mockGetPensionReliefsT(Right(None))
      val result = service.getPaymentsIntoPensions(sampleCtx).value.futureValue
      assert(result.value === None)
    }

    // TODO It is not valid situation, we probably need to think how we want to handle mismatches IFS vs our DB
    "get answers if there are DB answers, but IFS return None (favour IFS)" in {
      mockGetPensionReliefsT(Right(None))

      val result = (for {
        _   <- stubRepository.upsertAnswers(paymentIntoPensionsCtx, Json.toJson(answers))
        res <- service.getPaymentsIntoPensions(sampleCtx)
      } yield res).value.futureValue.value

      assert(result === Some(PaymentsIntoPensionsAnswers(false, None, Some(true), None, true, Some(true), None, Some(true), None)))
    }

    "return answers" in {
      mockGetPensionReliefsT(Right(Some(GetPensionReliefsModel("unused", None, PensionReliefs(Some(1.0), Some(2.0), Some(3.0), Some(4.0), None)))))
      val result = (for {
        _   <- stubRepository.upsertAnswers(paymentIntoPensionsCtx, Json.toJson(answers))
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
      mockGetPensionReliefsT(Right(None))
      mockCreateOrAmendPensionReliefsT(
        Right(None),
        expectedModel =
          CreateOrUpdatePensionReliefsModel(PensionReliefs(Some(1.0), Some(2.0), Some(3.0), Some(4.0), overseasPensionSchemeContributions))
      )

      val result = service.upsertPaymentsIntoPensions(sampleCtx, paymentsIntoPensionsAnswers).value.futureValue

      assert(result.isRight)
      assert(stubRepository.upsertAnswersList.size === 1)
      val persistedAnswers = stubRepository.upsertAnswersList.head.as[PaymentsIntoPensionsStorageAnswers]
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
      mockGetPensionReliefsT(
        Right(Some(GetPensionReliefsModel("unused", None, PensionReliefs(None, None, None, None, overseasPensionSchemeContributions)))))

      mockCreateOrAmendPensionReliefsT(
        Right(None),
        expectedModel =
          CreateOrUpdatePensionReliefsModel(PensionReliefs(Some(1.0), Some(2.0), Some(3.0), Some(4.0), overseasPensionSchemeContributions))
      )

      val result = service.upsertPaymentsIntoPensions(sampleCtx, paymentsIntoPensionsAnswers).value.futureValue

      assert(result.isRight)
      assert(stubRepository.upsertAnswersList.size === 1)
      val persistedAnswers = stubRepository.upsertAnswersList.head.as[PaymentsIntoPensionsStorageAnswers]
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
    val ctx = sampleCtx.toJourneyContext(Journey.UkPensionIncome)

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
        _   <- stubRepository.upsertAnswers(ctx, Json.toJson(answers))
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
        _   <- stubRepository.upsertAnswers(ctx, Json.toJson(answers))
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

      val result = service.upsertUkPensionIncome(sampleCtx, answers).value.futureValue

      assert(result.isRight)
      assert(
        employmentStub.ukPensionIncome === List(
          UkPensionIncomeAnswers(
            uKPensionIncomesQuestion = true,
            List(sampleSingleUkPensionIncome)
          )))
      assert(stubRepository.upsertAnswersList.size === 1)
      val persistedAnswers = stubRepository.upsertAnswersList.head.as[UkPensionIncomeStorageAnswers]
      assert(persistedAnswers === UkPensionIncomeStorageAnswers(true))
    }
  }

  "getStatePension" should {
    "get None if No downstream and DB answers" in {
      val service = createPensionWithStubStateBenefit(stubStateBenefit)
      val result  = service.getStatePension(sampleCtx).value.futureValue.value

      assert(result === None)
    }

    "get empty answers if no answers from downstream but DB answers exist" in {
      val service = createPensionWithStubStateBenefit(stubStateBenefit)
      val answers = IncomeFromPensionsStatePensionStorageAnswers(Some(true), Some(true))

      val result = (for {
        _   <- stubRepository.upsertAnswers(sampleCtx.toJourneyContext(Journey.StatePension), Json.toJson(answers))
        res <- service.getStatePension(sampleCtx)
      } yield res).value.futureValue.value

      assert(result === Some(IncomeFromPensionsStatePensionAnswers(None, None, None)))
    }

    "get answers from downstream" in {
      val service = createPensionWithStubStateBenefit(
        StubStateBenefitService(
          getStateBenefitsResult = Right(Some(stateBenefits.allStateBenefitsData))
        ))

      val answers = IncomeFromPensionsStatePensionStorageAnswers(Some(true), Some(true))

      val result = (for {
        _   <- stubRepository.upsertAnswers(sampleCtx.toJourneyContext(Journey.StatePension), Json.toJson(answers))
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
    val annualAllowancesCtx = sampleCtx.toJourneyContext(Journey.AnnualAllowances)

    "get None if no answers" in {
      mockGetPensionChargesT(Right(None))
      val result = service.getAnnualAllowances(sampleCtx).value.futureValue
      assert(result.value === None)
    }

    "get None even if there are some DB answers, but IFS return None (favour IFS)" in {
      mockGetPensionChargesT(Right(None))

      val result = (for {
        _   <- stubRepository.upsertAnswers(annualAllowancesCtx, Json.toJson(annualAllowancesStorageAnswers))
        res <- service.getAnnualAllowances(sampleCtx)
      } yield res).value.futureValue.value

      assert(result === None)
    }

    "return answers" in {
      mockGetPensionChargesT(Right(Some(GetPensionChargesRequestModel("unused", None, None, Some(pensionContributions), None))))
      val result = (for {
        _   <- stubRepository.upsertAnswers(annualAllowancesCtx, Json.toJson(annualAllowancesStorageAnswers))
        res <- service.getAnnualAllowances(sampleCtx)
      } yield res).value.futureValue.value

      assert(result.value === annualAllowancesAnswers)
    }
  }

  "upsertAnnualAllowances" should {
    "upsert answers " in {
      mockGetPensionChargesT(Right(None))
      mockCreateOrAmendPensionChargesT(
        Right(None),
        expectedModel = CreateUpdatePensionChargesRequestModel(None, None, Some(pensionContributions), None)
      )

      val result = service.upsertAnnualAllowances(sampleCtx, annualAllowancesAnswers).value.futureValue

      assert(result.isRight)
      assert(stubRepository.upsertAnswersList.size === 1)
      val persistedAnswers = stubRepository.upsertAnswersList.head.as[AnnualAllowancesStorageAnswers]
      assert(persistedAnswers === annualAllowancesStorageAnswers)
    }
  }

  "getPaymentsIntoOverseasPensions" should {
    val piopCtx = sampleCtx.toJourneyContext(Journey.PaymentsIntoOverseasPensions)

    "get None if no answers" in {
      mockGetPensionReliefsT(Right(None))
      mockGetPensionIncomeT(Right(None))

      val result = service.getPaymentsIntoOverseasPensions(sampleCtx).value.futureValue
      assert(result.value === None)
    }

    "return a 'No' journey if IFS returns None but DB answers exist (regardless of the DB answers' values)" in {
      mockGetPensionReliefsT(Right(None))
      mockGetPensionIncomeT(Right(None))

      val storageAnswers = PaymentsIntoOverseasPensionsStorageAnswers(Some(true), Some(true), Some(true))
      val result = (for {
        _   <- stubRepository.upsertAnswers(piopCtx, Json.toJson(storageAnswers))
        res <- service.getPaymentsIntoOverseasPensions(sampleCtx)
      } yield res).value.futureValue.value

      val expectedResult = PaymentsIntoOverseasPensionsAnswers(Some(false), None, None, None, List.empty).some

      assert(result === expectedResult)
    }

    "return answers" in {
      mockGetPensionReliefsT(
        Right(Some(GetPensionReliefsModel("unused", None, PensionReliefs.empty.copy(overseasPensionSchemeContributions = Some(2))))))
      mockGetPensionIncomeT(
        Right(Some(GetPensionIncomeModel("unused", None, None, Some(Seq(mmrOverseasPensionContribution, tcrOverseasPensionContribution))))))

      val result = (for {
        _   <- stubRepository.upsertAnswers(piopCtx, Json.toJson(piopStorageAnswers))
        res <- service.getPaymentsIntoOverseasPensions(sampleCtx)
      } yield res).value.futureValue.value

      assert(result.value === paymentsIntoOverseasPensionsAnswers)
    }
  }

  "upsertPaymentsIntoOverseasPensions" should {
    "upsert answers " in {
      mockGetPensionReliefsT(Right(None))
      mockGetPensionIncomeT(Right(None))
      mockCreateOrAmendPensionReliefsT(
        Right(None),
        expectedModel = CreateOrUpdatePensionReliefsModel(PensionReliefs.empty.copy(overseasPensionSchemeContributions = Some(2.0)))
      )
      mockCreateOrAmendPensionIncomeT(
        Right(None),
        expectedModel = CreateUpdatePensionIncomeModel(None, Some(Seq(mmrOverseasPensionContribution, tcrOverseasPensionContribution)))
      )

      val result = service.upsertPaymentsIntoOverseasPensions(sampleCtx, paymentsIntoOverseasPensionsAnswers).value.futureValue

      assert(result.isRight)
      assert(stubRepository.upsertAnswersList.size === 1)
      val persistedAnswers = stubRepository.upsertAnswersList.head.as[PaymentsIntoOverseasPensionsStorageAnswers]
      assert(persistedAnswers === piopStorageAnswers)
    }
  }

  "getTransfersIntoOverseasPensions" should {
    val transferIntoOverseasPensionsCtx = sampleCtx.toJourneyContext(Journey.TransferIntoOverseasPensions)

    "get None if no answers" in {
      mockGetPensionChargesT(Right(None))
      val result = service.getTransfersIntoOverseasPensions(sampleCtx).value.futureValue
      assert(result.value === None)
    }

    "return answers" in {
      mockGetPensionChargesT(Right(Some(GetPensionChargesRequestModel("unused", Some(pensionSchemeOverseasTransfers), None, None, None))))
      val result = (for {
        _   <- stubRepository.upsertAnswers(transferIntoOverseasPensionsCtx, Json.toJson(transfersIntoOverseasPensionsStorageAnswers))
        res <- service.getTransfersIntoOverseasPensions(sampleCtx)
      } yield res).value.futureValue.value

      assert(result.value === transfersIntoOverseasPensionsAnswers)
    }
  }

  "upsertTransfersIntoOverseasPensions" should {
    "upsert answers " in {
      mockGetPensionChargesT(Right(None))
      mockCreateOrAmendPensionChargesT(
        Right(None),
        expectedModel = CreateUpdatePensionChargesRequestModel.empty.copy(pensionSchemeOverseasTransfers = Some(pensionSchemeOverseasTransfers))
      )

      val result = service.upsertTransfersIntoOverseasPensions(sampleCtx, transfersIntoOverseasPensionsAnswers).value.futureValue

      assert(result.isRight)
      assert(stubRepository.upsertAnswersList.size === 1)
      val persistedAnswers = stubRepository.upsertAnswersList.head.as[TransfersIntoOverseasPensionsStorageAnswers]
      assert(persistedAnswers === transfersIntoOverseasPensionsStorageAnswers)
    }
  }

  "getIncomeFromOverseasPensions" should {
    val incomeFromOverseasPensionsCtx = sampleCtx.toJourneyContext(Journey.IncomeFromOverseasPensions)

    "get None if no answers" in {
      mockGetPensionIncomeT(Right(None))
      val result = service.getIncomeFromOverseasPensions(sampleCtx).value.futureValue
      assert(result.value === None)
    }

    "return answers" in {
      mockGetPensionIncomeT(Right(Some(GetPensionIncomeModel("date", None, Some(Seq(foreignPension)), None))))
      val result = (for {
        _   <- stubRepository.upsertAnswers(incomeFromOverseasPensionsCtx, Json.toJson(incomeFromOverseasPensionsStorageAnswers))
        res <- service.getIncomeFromOverseasPensions(sampleCtx)
      } yield res).value.futureValue.value

      assert(result.value === incomeFromOverseasPensionsAnswers)
    }
  }

  "upsertIncomeFromOverseasPensions" should {
    "upsert answers " in {
      mockGetPensionIncomeT(Right(None))
      mockCreateOrAmendPensionIncomeT(
        Right(None),
        expectedModel = CreateUpdatePensionIncomeModel(Some(List(foreignPension)), None)
      )

      val result = service.upsertIncomeFromOverseasPensions(sampleCtx, incomeFromOverseasPensionsAnswers).value.futureValue

      assert(result.isRight)
      assert(stubRepository.upsertAnswersList.size === 1)
      val persistedAnswers = stubRepository.upsertAnswersList.head.as[IncomeFromOverseasPensionsStorageAnswers]
      assert(persistedAnswers === incomeFromOverseasPensionsStorageAnswers)
    }
  }

  "getShortServiceRefunds" should {
    val getShortServiceRefundsCtx = sampleCtx.toJourneyContext(Journey.ShortServiceRefunds)

    "get None if no answers" in {
      mockGetPensionChargesT(Right(None))
      val result = service.getShortServiceRefunds(sampleCtx).value.futureValue
      assert(result.value === None)
    }

    "get None even if there are some DB answers, but IFS return None (favour IFS)" in {
      mockGetPensionChargesT(Right(None))
      val result = (for {
        _   <- stubRepository.upsertAnswers(getShortServiceRefundsCtx, Json.toJson(ShortServiceRefundsStorageAnswers()))
        res <- service.getShortServiceRefunds(sampleCtx)
      } yield res).value.futureValue.value

      assert(result === None)
    }

    "return answers" in {
      mockGetPensionChargesT(
        Right(Some(GetPensionChargesRequestModel("unused", None, None, None, Some(overseasPensionContributions))))
      )
      val result = (for {
        _   <- stubRepository.upsertAnswers(getShortServiceRefundsCtx, Json.toJson(shortServiceRefundsCtxStorageAnswers))
        res <- service.getShortServiceRefunds(sampleCtx)
      } yield res).value.futureValue.value

      assert(result.value === shortServiceRefundsAnswers)
    }
  }

  "upsertShortServiceRefunds" should {
    "insert answers if overseasPensionContributions does not exist" in {
      mockGetPensionChargesT(Right(None))
      mockCreateOrAmendPensionChargesT(
        Right(None),
        expectedModel = CreateUpdatePensionChargesRequestModel(None, None, None, Some(overseasPensionContributions))
      )

      val result = service.upsertShortServiceRefunds(sampleCtx, shortServiceRefundsAnswers).value.futureValue

      assert(result.isRight)
      assert(stubRepository.upsertAnswersList.size === 1)
      val persistedAnswers = stubRepository.upsertAnswersList.head.as[ShortServiceRefundsStorageAnswers]
      assert(persistedAnswers === shortServiceRefundsCtxStorageAnswers)
    }

    "update answers if overseasPensionContributions exist" in {
      mockGetPensionChargesT(
        Right(
          Some(
            GetPensionChargesRequestModel("unused", None, None, None, Some(OverseasPensionContributions(Seq(), BigDecimal(0.0), BigDecimal(0.0))))))
      )
      mockCreateOrAmendPensionChargesT(
        Right(None),
        expectedModel = CreateUpdatePensionChargesRequestModel(None, None, None, Some(overseasPensionContributions))
      )

      val result = service.upsertShortServiceRefunds(sampleCtx, shortServiceRefundsAnswers).value.futureValue

      assert(result.isRight)
      assert(stubRepository.upsertAnswersList.size === 1)
      val persistedAnswers = stubRepository.upsertAnswersList.head.as[ShortServiceRefundsStorageAnswers]
      assert(persistedAnswers === shortServiceRefundsCtxStorageAnswers)
    }
  }

  "getCommonTaskList" should {
    "return None when no data in DB and IFS" in {
      mockGetPensionReliefsT(Right(None))
      mockGetPensionChargesT(Right(None))
      mockGetPensionIncomeT(Right(None))

      val underTest: PensionsService = new PensionsServiceImpl(
        mockAppConfig,
        mockReliefsConnector,
        mockChargesConnector,
        StubStateBenefitService(),
        mockIncomeConnector,
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

      val service: PensionsService = createPensionWithStubs(stubEmploymentService, stubStateBenefit, stubStatusService)
      mockGetPensionReliefsT(
        Right(Some(getPensionReliefsModel.getPensionReliefsModel))
      )
      mockGetPensionChargesT(Right(None))
      mockGetPensionIncomeT(Right(None))

      val result = service.getCommonTaskList(sampleCtx).value.futureValue.value

      val expected = TaskListModel(
        List(
          TaskListSection(
            SectionTitle.PensionsTitle(),
            Some(List(
              TaskListSectionItem(
                TaskTitle.pensionsTitles.StatePension(),
                TaskStatus.Completed(),
                Some(s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/pension-income/check-state-pension")
              ),
              TaskListSectionItem(
                TaskTitle.pensionsTitles.OtherUkPensions(),
                TaskStatus.InProgress(),
                Some(s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/pension-income/check-uk-pension-income")
              ),
              TaskListSectionItem(
                TaskTitle.pensionsTitles.UnauthorisedPayments(),
                TaskStatus.Completed(),
                Some(s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/unauthorised-payments-from-pensions/check-unauthorised-payments")
              ),
              TaskListSectionItem(
                TaskTitle.pensionsTitles.ShortServiceRefunds(),
                TaskStatus.Completed(),
                Some(s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/overseas-pensions/short-service-refunds/check-short-service-refund-details")
              ),
              TaskListSectionItem(
                TaskTitle.pensionsTitles.IncomeFromOverseas(),
                TaskStatus.Completed(),
                Some(s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/overseas-pensions/income-from-overseas-pensions/check-overseas-pension-income")
              )
            ))
          ),
          TaskListSection(
            SectionTitle.PaymentsIntoPensionsTitle(),
            Some(List(
              TaskListSectionItem(
                TaskTitle.paymentsIntoPensionsTitles.PaymentsIntoUk(),
                TaskStatus.InProgress(),
                Some(
                  s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/payments-into-pensions/check-payments-into-pensions")
              ),
              TaskListSectionItem(
                TaskTitle.paymentsIntoPensionsTitles.AnnualAllowances(),
                TaskStatus.Completed(),
                Some(s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/annual-allowance/check-annual-allowance")
              ),
              TaskListSectionItem(
                TaskTitle.paymentsIntoPensionsTitles.PaymentsIntoOverseas(),
                TaskStatus.Completed(),
                Some(s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/overseas-pensions/payments-into-overseas-pensions/check-overseas-pension-details")
              ),
              TaskListSectionItem(
                TaskTitle.paymentsIntoPensionsTitles.OverseasTransfer(),
                TaskStatus.Completed(),
                Some(s"http://localhost:9321/update-and-submit-income-tax-return/pensions/$taxYear/overseas-pensions/overseas-transfer-charges/transfer-charges/check-transfer-charges-details")
              )
            ))
          )
        ))

      assert(result === expected)
    }
  }
}
