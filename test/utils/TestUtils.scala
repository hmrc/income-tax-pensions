/*
 * Copyright 2022 HM Revenue & Customs
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

import akka.actor.ActorSystem
import akka.stream.SystemMaterializer
import com.codahale.metrics.SharedMetricRegistries
import common.{EnrolmentIdentifiers, EnrolmentKeys}
import config.AppConfig
import controllers.predicates.AuthorisedAction
import models.{GetStateBenefitsModel, StateBenefit, StateBenefits}
import models._
import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents, DefaultActionBuilder, Result}
import play.api.test.{FakeRequest, Helpers}
import services.AuthService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable, ExecutionContext, Future}

trait TestUtils extends AnyWordSpec with Matchers with MockFactory with GuiceOneAppPerSuite with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach()
    SharedMetricRegistries.clear()
  }

  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val materializer: SystemMaterializer = SystemMaterializer(actorSystem)

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("mtditid" -> "1234567890")
  val fakeRequestWithMtditid: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession("MTDITID" -> "1234567890")
  implicit val emptyHeaderCarrier: HeaderCarrier = HeaderCarrier()

  val mockAppConfig: AppConfig = app.injector.instanceOf[AppConfig]
  implicit val mockControllerComponents: ControllerComponents = Helpers.stubControllerComponents()
  implicit val mockExecutionContext: ExecutionContext = ExecutionContext.Implicits.global
  implicit val mockAuthConnector: AuthConnector = mock[AuthConnector]
  implicit val mockAuthService: AuthService = new AuthService(mockAuthConnector)
  val defaultActionBuilder: DefaultActionBuilder = DefaultActionBuilder(mockControllerComponents.parsers.default)
  val authorisedAction = new AuthorisedAction()(mockAuthConnector, defaultActionBuilder, mockControllerComponents)


  def status(awaitable: Future[Result]): Int = await(awaitable).header.status

  def bodyOf(awaitable: Future[Result]): String = {
    val awaited = await(awaitable)
    await(awaited.body.consumeData.map(_.utf8String))
  }

  val individualEnrolments: Enrolments = Enrolments(Set(
    Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "1234567890")), "Activated"),
    Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, "1234567890")), "Activated")))

  //noinspection ScalaStyle
  def mockAuth(enrolments: Enrolments = individualEnrolments) = {

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.affinityGroup, *, *)
      .returning(Future.successful(Some(AffinityGroup.Individual)))

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.allEnrolments and Retrievals.confidenceLevel, *, *)
      .returning(Future.successful(enrolments and ConfidenceLevel.L200))
  }

  val agentEnrolments: Enrolments = Enrolments(Set(
    Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "1234567890")), "Activated"),
    Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, "0987654321")), "Activated")
  ))

  //noinspection ScalaStyle
  def mockAuthAsAgent(enrolments: Enrolments = agentEnrolments) = {

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.affinityGroup, *, *)
      .returning(Future.successful(Some(AffinityGroup.Agent)))

    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, Retrievals.allEnrolments, *, *)
      .returning(Future.successful(enrolments))
  }

  //noinspection ScalaStyle
  def mockAuthReturnException(exception: Exception) = {
    (mockAuthConnector.authorise(_: Predicate, _: Retrieval[_])(_: HeaderCarrier, _: ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.failed(exception))
  }

  val fullPensionReliefsModel = GetPensionReliefsModel(
    "2020-01-04T05:01:01Z", Some("2020-01-04T05:01:01Z"), PensionReliefs(
      Some(100.01), Some(100.01), Some(100.01), Some(100.01), Some(100.01)
    )
  )
  val fullPensionChargesModel = GetPensionChargesRequestModel(
    submittedOn = "2020-07-27T17:00:19Z",
    pensionSavingsTaxCharges = Some(PensionSavingsTaxCharges(
      pensionSchemeTaxReference = Seq("00123456RA", "00123456RB"),
      lumpSumBenefitTakenInExcessOfLifetimeAllowance = Some(LifetimeAllowance(
        amount = 800.02,
        taxPaid = 200.02
      )),
      benefitInExcessOfLifetimeAllowance = Some(LifetimeAllowance(
        amount = 800.02,
        taxPaid = 200.02
      )),
      isAnnualAllowanceReduced = false,
      taperedAnnualAllowance = Some(false),
      moneyPurchasedAllowance = Some(false)
    )),
    pensionSchemeOverseasTransfers = Some(PensionSchemeOverseasTransfers(
      overseasSchemeProvider = Seq(OverseasSchemeProvider(
        providerName = "overseas providerName 1 qualifying scheme",
        providerAddress = "overseas address 1",
        providerCountryCode = "ESP",
        qualifyingRecognisedOverseasPensionScheme = Some(Seq("Q100000", "Q100002")),
        pensionSchemeTaxReference = None
      )),
      transferCharge = 22.77,
      transferChargeTaxPaid = 33.88
    )),
    pensionSchemeUnauthorisedPayments = Some(PensionSchemeUnauthorisedPayments(
      pensionSchemeTaxReference = Seq("00123456RA", "00123456RB"),
      surcharge = Some(Charge(
        amount = 124.44,
        foreignTaxPaid = 123.33
      )),
      noSurcharge = Some(Charge(
        amount = 222.44,
        foreignTaxPaid = 223.33
      ))
    )),
    pensionContributions = Some(PensionContributions(
      pensionSchemeTaxReference = Seq("00123456RA", "00123456RB"),
      inExcessOfTheAnnualAllowance = 150.67,
      annualAllowanceTaxPaid = 178.65)),
    overseasPensionContributions = Some(OverseasPensionContributions(
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

  val stateBenefitModel = StateBenefit(
    benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c934",
    startDate = "2019-11-13",
    dateIgnored = Some("2019-04-11T16:22:00Z"),
    endDate = Some("2020-08-23"),
    amount = Some(1212.34),
    submittedOn = Some("2020-09-11T17:23:00Z"),
    taxPaid = Some(22323.23)
  )

  val customerStateBenefitModel = StateBenefit(
    benefitId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c935",
    startDate = "2019-11-13",
    dateIgnored = None,
    endDate = Some("2020-08-23"),
    amount = Some(1212.34),
    submittedOn = Some("2020-09-11T17:23:00Z"),
    taxPaid = Some(22323.23)
  )

  val fullStateBenefitsModel = GetStateBenefitsModel(
    stateBenefits = Some(StateBenefits(
      incapacityBenefit = Some(Seq(stateBenefitModel)),
      statePension = Some(stateBenefitModel),
      statePensionLumpSum = Some(stateBenefitModel),
      employmentSupportAllowance = Some(Seq(stateBenefitModel)),
      jobSeekersAllowance = Some(Seq(stateBenefitModel)),
      bereavementAllowance = Some(stateBenefitModel),
      otherStateBenefits = Some(stateBenefitModel)
    )),
    customerAddedStateBenefits = Some(StateBenefits(
      incapacityBenefit = Some(Seq(customerStateBenefitModel)),
      statePension = Some(customerStateBenefitModel),
      statePensionLumpSum = Some(customerStateBenefitModel),
      employmentSupportAllowance = Some(Seq(customerStateBenefitModel)),
      jobSeekersAllowance = Some(Seq(customerStateBenefitModel)),
      bereavementAllowance = Some(customerStateBenefitModel),
      otherStateBenefits = Some(customerStateBenefitModel)
    ))
  )

  val fullPensionsModel = AllPensionsData(
    pensionReliefs = Some(fullPensionReliefsModel),
    pensionCharges = Some(fullPensionChargesModel),
    stateBenefits = Some(fullStateBenefitsModel)
  )
}
