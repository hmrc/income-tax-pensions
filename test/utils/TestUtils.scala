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

package utils

import cats.data.EitherT
import com.codahale.metrics.SharedMetricRegistries
import common.{EnrolmentIdentifiers, EnrolmentKeys}
import config.AppConfig
import controllers.predicates.AuthorisedAction
import models._
import models.charges._
import models.common._
import models.database.JourneyAnswers
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.SystemMaterializer
import org.scalamock.handlers.CallHandler4
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsObject, Json, Writes}
import play.api.mvc._
import play.api.test.{FakeRequest, Helpers}
import play.libs.pekko.PekkoGuiceSupport
import services.AuthService
import testdata.transfersIntoOverseasPensions.{nonUkOverseasSchemeProvider, ukOverseasSchemeProvider}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.HeaderCarrier
import utils.AllStateBenefitsDataBuilder.anAllStateBenefitsData
import utils.EmploymentPensionsBuilder.employmentPensionsData
import utils.TestUtils.currTaxYear

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, ZoneOffset}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable, ExecutionContext, Future}

trait TestUtils extends AnyWordSpec with Matchers with MockFactory with GuiceOneAppPerSuite with BeforeAndAfterEach with PekkoGuiceSupport {

  val taxYear        = 2022
  val currentTaxYear = TaxYear(LocalDate.now().getYear)
  val nino           = "AA123456A"
  val validNino      = common.Nino("AA123456A")
  val mtditid        = "1234567890"
  val validMtditid   = Mtditid("1234567890")
  val testContext    = JourneyContextWithNino(currTaxYear, validMtditid, validNino)

  override def beforeEach(): Unit = {
    super.beforeEach()
    SharedMetricRegistries.clear()
  }

  implicit val actorSystem: ActorSystem         = ActorSystem()
  implicit val materializer: SystemMaterializer = SystemMaterializer(actorSystem)

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type]   = FakeRequest().withHeaders("mtditid" -> mtditid)
  val fakeRequestWithMtditid: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession("MTDITID" -> mtditid)
  implicit val emptyHeaderCarrier: HeaderCarrier                  = HeaderCarrier()

  val mockAppConfig: AppConfig                                = app.injector.instanceOf[AppConfig]
  implicit val mockControllerComponents: ControllerComponents = Helpers.stubControllerComponents()
  implicit val mockExecutionContext: ExecutionContext         = ExecutionContext.Implicits.global
  implicit val mockAuthConnector: AuthConnector               = mock[AuthConnector]
  implicit val mockAuthService: AuthService                   = new AuthService(mockAuthConnector)
  val defaultActionBuilder: DefaultActionBuilder              = DefaultActionBuilder(mockControllerComponents.parsers.default)
  val authorisedAction = new AuthorisedAction()(mockAuthConnector, defaultActionBuilder, mockControllerComponents)

  def status(awaitable: Future[Result]): Int = await(awaitable).header.status

  def bodyOf(awaitable: Future[Result]): String = {
    val awaited = await(awaitable)
    await(awaited.body.consumeData.map(_.utf8String))
  }

  val someServiceError: ServiceErrorModel =
    DesErrorModel(
      status = 500,
      body = DesErrorBodyModel.serverError
    )

  val individualEnrolments: Enrolments = Enrolments(
    Set(
      Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "1234567890")), "Activated"),
      Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, "1234567890")), "Activated")
    ))

  // noinspection ScalaStyle
  def mockAuth(enrolments: Enrolments = individualEnrolments) = {

    (mockAuthConnector
      .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.affinityGroup, *, *)
      .returning(Future.successful(Some(AffinityGroup.Individual)))

    (mockAuthConnector
      .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.allEnrolments and Retrievals.confidenceLevel, *, *)
      .returning(Future.successful(enrolments and ConfidenceLevel.L250))
  }

  val agentEnrolments: Enrolments = Enrolments(
    Set(
      Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "1234567890")), "Activated"),
      Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, "0987654321")), "Activated")
    ))

  // noinspection ScalaStyle
  def mockAuthAsAgent(enrolments: Enrolments = agentEnrolments) = {

    (mockAuthConnector
      .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.affinityGroup, *, *)
      .returning(Future.successful(Some(AffinityGroup.Agent)))

    (mockAuthConnector
      .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.allEnrolments, *, *)
      .returning(Future.successful(enrolments))
  }

  // noinspection ScalaStyle
  def mockAuthReturnException(exception: Exception) =
    (mockAuthConnector
      .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.failed(exception))

  def mockAuthorisePredicates[A](predicate: Predicate,
                                 returningResult: Future[A]): CallHandler4[Predicate, Retrieval[_], HeaderCarrier, ExecutionContext, Future[Any]] =
    (mockAuthConnector
      .authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(predicate, *, *, *)
      .returning(returningResult)

  def buildRequest[A: Writes](body: A): FakeRequest[AnyContentAsJson] = FakeRequest()
    .withHeaders("mtditid" -> "1234567890")
    .withJsonBody(Json.toJson(body))

  def testRoute(expectedStatus: Int, expectedBody: String, methodBlock: () => Action[AnyContent], request: Request[AnyContent] = fakeRequest): Unit =
    runControllerSpec(request, expectedStatus, expectedBody, () => (), methodBlock)

  private def runControllerSpec(request: Request[AnyContent],
                                expectedStatus: Int,
                                expectedBody: String,
                                stubs: () => Unit,
                                methodBlock: () => Action[AnyContent]): Unit = {
    val result = {
      mockAuth()
      stubs()
      methodBlock()(request)
    }
    status(result) mustBe expectedStatus
    assert(bodyOf(result) contains expectedBody)
  }

  val fullPensionReliefsModel = GetPensionReliefsModel(
    "2020-01-04T05:01:01Z",
    Some("2020-01-04T05:01:01Z"),
    PensionReliefs(
      Some(100.01),
      Some(100.01),
      Some(100.01),
      Some(100.01),
      Some(100.01)
    )
  )
  val fullPensionChargesModel = GetPensionChargesRequestModel(
    submittedOn = "2020-07-27T17:00:19Z",
    pensionSchemeOverseasTransfers = Some(
      PensionSchemeOverseasTransfers(
        overseasSchemeProvider = Seq(ukOverseasSchemeProvider, nonUkOverseasSchemeProvider),
        transferCharge = 22.77,
        transferChargeTaxPaid = 33.88
      )),
    pensionSchemeUnauthorisedPayments = Some(
      PensionSchemeUnauthorisedPayments(
        pensionSchemeTaxReference = Some(List("00123456RA", "00123456RB")),
        surcharge = Some(
          Charge(
            amount = 124.44,
            foreignTaxPaid = 123.33
          )),
        noSurcharge = Some(
          Charge(
            amount = 222.44,
            foreignTaxPaid = 223.33
          ))
      )),
    pensionContributions = Some(
      PensionContributions(
        pensionSchemeTaxReference = Seq("00123456RA", "00123456RB"),
        inExcessOfTheAnnualAllowance = 150.67,
        annualAllowanceTaxPaid = 178.65,
        isAnnualAllowanceReduced = Some(false),
        taperedAnnualAllowance = Some(false),
        moneyPurchasedAllowance = Some(false)
      )),
    overseasPensionContributions = Some(
      OverseasPensionContributions(
        overseasSchemeProvider = Seq(OverseasSchemeProvider(
          providerName = "overseas providerName 1 tax ref",
          providerAddress = "overseas address 1",
          providerCountryCode = "ESP",
          qualifyingRecognisedOverseasPensionScheme = None,
          pensionSchemeTaxReference = Some(Seq("00123456RA", "00123456RB"))
        )),
        shortServiceRefund = 1.11,
        shortServiceRefundTaxPaid = 2.22
      ))
  )

  val fullPensionIncomeModel: GetPensionIncomeModel =
    GetPensionIncomeModel(
      submittedOn = "2022-07-28T07:59:39.041Z",
      deletedOn = Some("2022-07-28T07:59:39.041Z"),
      foreignPension = Some(
        Seq(
          ForeignPension(
            countryCode = "FRA",
            taxableAmount = 1999.99,
            amountBeforeTax = Some(1999.99),
            taxTakenOff = Some(1999.99),
            specialWithholdingTax = Some(1999.99),
            foreignTaxCreditRelief = Some(false)
          )
        )),
      overseasPensionContribution = Some(
        Seq(
          OverseasPensionContribution(
            customerReference = Some("PENSIONINCOME245"),
            exemptEmployersPensionContribs = 1999.99,
            migrantMemReliefQopsRefNo = Some("QOPS000000"),
            dblTaxationRelief = Some(1999.99),
            dblTaxationCountry = Some("FRA"),
            dblTaxationArticle = Some("AB3211-1"),
            dblTaxationTreaty = Some("Munich"),
            sf74Reference = Some("SF74-123456")
          )
        ))
    )

  val fullPensionsModel: AllPensionsData = AllPensionsData(
    pensionReliefs = Some(fullPensionReliefsModel),
    pensionCharges = Some(fullPensionChargesModel),
    stateBenefits = Some(anAllStateBenefitsData),
    employmentPensions = Some(employmentPensionsData),
    pensionIncome = Some(fullPensionIncomeModel)
  )
}

object TestUtils {
  implicit val hc: HeaderCarrier = HeaderCarrier()

  // static data
  val taxYear: TaxYear         = TaxYear(LocalDate.now().getYear)
  val taxYearStart: String     = TaxYear.startDate(taxYear)
  val taxYearEnd: String       = TaxYear.endDate(taxYear)
  val nino: models.common.Nino = models.common.Nino("nino")
  val mtditid: Mtditid         = Mtditid("1234567890")

  // dynamic & generated data
  val currTaxYear: TaxYear     = TaxYear(LocalDate.now().getYear)
  val currTaxYearStart: String = TaxYear.startDate(currTaxYear)
  val currTaxYearEnd: String   = TaxYear.endDate(currTaxYear)

  // more complex data
  val journeyCtxWithNino: JourneyContextWithNino      = JourneyContextWithNino(currTaxYear, mtditid, nino)
  val paymentsIntoPensionsCtx: JourneyContext         = journeyCtxWithNino.toJourneyContext(Journey.PaymentsIntoPensions)
  val ukPensionIncomeCtx: JourneyContext              = journeyCtxWithNino.toJourneyContext(Journey.UkPensionIncome)
  val statePensionCtx: JourneyContext                 = journeyCtxWithNino.toJourneyContext(Journey.StatePension)
  val annualAllowancesCtx: JourneyContext             = journeyCtxWithNino.toJourneyContext(Journey.AnnualAllowances)
  val unauthorisedPaymentsCtx: JourneyContext         = journeyCtxWithNino.toJourneyContext(Journey.UnauthorisedPayments)
  val paymentsIntoOverseasPensionsCtx: JourneyContext = journeyCtxWithNino.toJourneyContext(Journey.PaymentsIntoOverseasPensions)
  val incomeFromOverseasPensionsCtx: JourneyContext   = journeyCtxWithNino.toJourneyContext(Journey.IncomeFromOverseasPensions)
  val transferIntoOverseasPensionsCtx: JourneyContext = journeyCtxWithNino.toJourneyContext(Journey.TransferIntoOverseasPensions)
  val shortServiceRefundsCtx: JourneyContext          = journeyCtxWithNino.toJourneyContext(Journey.ShortServiceRefunds)

  // operations
  def mkNow(): Instant                 = Instant.now().truncatedTo(ChronoUnit.SECONDS)
  def mkClock(now: Instant): TestClock = TestClock(now, ZoneOffset.UTC)
  def mkJourneyAnswers(journey: Journey, status: JourneyStatus, data: JsObject): JourneyAnswers = JourneyAnswers(
    mtditid,
    currTaxYear,
    journey,
    status,
    data,
    Instant.now(),
    Instant.now(),
    Instant.now()
  )

  implicit class ToEitherTOps[A, B](value: Either[A, B]) {
    def toEitherT(implicit ec: ExecutionContext): EitherT[Future, A, B] =
      EitherT.fromEither[Future](value)
  }
}
