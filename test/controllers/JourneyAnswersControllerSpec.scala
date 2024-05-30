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

package controllers

import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import models.common.JourneyContextWithNino
import models.error.ServiceError
import models.error.ServiceError.DownstreamError
import models.frontend._
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Action, AnyContent}
import services.PensionsService
import stubs.services.PensionsServiceStub
import testdata.annualAllowances.annualAllowancesAnswers
import testdata.gens.JourneyAnswersGen._
import testdata.incomeFromOverseasPensions.incomeFromOverseasPensionsAnswers
import testdata.paymentsIntoOverseasPensions.paymentsIntoOverseasPensionsAnswers
import testdata.paymentsIntoPensions.paymentsIntoPensionsAnswers
import testdata.transfersIntoOverseasPensions.transfersIntoOverseasPensionsAnswers
import testdata.unauthorisedPayments._
import uk.gov.hmrc.http.HeaderCarrier
import utils.EitherTTestOps.convertScalaFuture
import utils.TestUtils

import scala.concurrent.Future

class JourneyAnswersControllerSpec extends TestUtils with ScalaCheckPropertyChecks {

  private val pensionsService         = mock[PensionsService]
  private val underTest               = new JourneyAnswersController(pensionsService, authorisedAction, mockControllerComponents)
  private val errorResult             = DownstreamError("ERROR")
  private val invalidJsonErrorMessage = "Cannot read JSON"
  private val downstreamErrorMessage  = "Downstream error: ERROR"

  private def underTestStubbed(service: PensionsServiceStub = PensionsServiceStub()) =
    new JourneyAnswersController(service, authorisedAction, mockControllerComponents)

  "getPaymentsIntoPensions" should {
    "return any PaymentsIntoPensions journey answers from downstream" in {
      val result = {
        mockAuth()
        (pensionsService
          .getPaymentsIntoPensions(_: JourneyContextWithNino)(_: HeaderCarrier))
          .expects(*, *)
          .returning(EitherT.fromEither[Future](Some(paymentsIntoPensionsAnswers).asRight[ServiceError]))
        underTest.getPaymentsIntoPensions(currentTaxYear, validNino)(fakeRequest)
      }

      status(result) mustBe OK
      bodyOf(result) mustBe Json.toJson(Some(paymentsIntoPensionsAnswers)).toString()
    }
    "return an Error from downstream" in {
      val result = {
        mockAuth()
        (pensionsService
          .getPaymentsIntoPensions(_: JourneyContextWithNino)(_: HeaderCarrier))
          .expects(*, *)
          .returning(EitherT.fromEither[Future](errorResult.asLeft[Option[PaymentsIntoPensionsAnswers]]))
        underTest.getPaymentsIntoPensions(currentTaxYear, validNino)(fakeRequest)
      }.futureValue.header

      assert(result.status == INTERNAL_SERVER_ERROR)
    }
  }

  "savePaymentsIntoPensions" should {
    def errorServiceResponse = PensionsServiceStub(upsertPaymentsIntoPensionsResult = errorResult.asLeft[Unit])
    returnCorrectResponsesToSaveJourneyAnswers(
      paymentsIntoPensionsAnswersGen,
      underTestStubbed().savePaymentsIntoPensions(currentTaxYear, validNino),
      underTestStubbed(errorServiceResponse).savePaymentsIntoPensions(currentTaxYear, validNino)
    )
  }

  "getAnnualAllowances" should {
    "return any AnnualAllowances journey answers from downstream" in {
      val result = {
        mockAuth()
        (pensionsService
          .getAnnualAllowances(_: JourneyContextWithNino)(_: HeaderCarrier))
          .expects(*, *)
          .returning(EitherT.fromEither[Future](Some(annualAllowancesAnswers).asRight[ServiceError]))
        underTest.getAnnualAllowances(currentTaxYear, validNino)(fakeRequest)
      }

      status(result) mustBe OK
      bodyOf(result) mustBe Json.toJson(Some(annualAllowancesAnswers)).toString()
    }
    "return an Error from downstream" in {
      val result = {
        mockAuth()
        (pensionsService
          .getAnnualAllowances(_: JourneyContextWithNino)(_: HeaderCarrier))
          .expects(*, *)
          .returning(EitherT.fromEither[Future](errorResult.asLeft[Option[AnnualAllowancesAnswers]]))
        underTest.getAnnualAllowances(currentTaxYear, validNino)(fakeRequest)
      }.futureValue.header

      assert(result.status == INTERNAL_SERVER_ERROR)
    }
  }

  "saveAnnualAllowances" should {
    def errorServiceResponse = PensionsServiceStub(upsertAnnualAllowancesResult = errorResult.asLeft[Unit])
    returnCorrectResponsesToSaveJourneyAnswers(
      annualAllowancesAnswersGen,
      underTestStubbed().saveAnnualAllowances(currentTaxYear, validNino),
      underTestStubbed(errorServiceResponse).saveAnnualAllowances(currentTaxYear, validNino)
    )
  }

  "getUnauthorisedPaymentsFromPensions" should {
    "return any UnauthorisedPayments journey answers from downstream" in {
      val result = {
        mockAuth()
        (pensionsService
          .getUnauthorisedPaymentsFromPensions(_: JourneyContextWithNino)(_: HeaderCarrier))
          .expects(*, *)
          .returning(EitherT.fromEither[Future](Some(unauthorisedPaymentsAnswers).asRight[ServiceError]))
        underTest.getUnauthorisedPaymentsFromPensions(currentTaxYear, validNino)(fakeRequest)
      }

      status(result) mustBe OK
      bodyOf(result) mustBe Json.toJson(Some(unauthorisedPaymentsAnswers)).toString()
    }
    "return an Error from downstream" in {
      val result = {
        mockAuth()
        (pensionsService
          .getUnauthorisedPaymentsFromPensions(_: JourneyContextWithNino)(_: HeaderCarrier))
          .expects(*, *)
          .returning(EitherT.fromEither[Future](errorResult.asLeft[Option[UnauthorisedPaymentsAnswers]]))
        underTest.getUnauthorisedPaymentsFromPensions(currentTaxYear, validNino)(fakeRequest)
      }.futureValue.header

      assert(result.status == INTERNAL_SERVER_ERROR)
    }
  }

  "saveUnauthorisedPaymentsFromPensions" should {
    def errorServiceResponse = PensionsServiceStub(upsertUnauthorisedPaymentsFromPensionsResult = errorResult.asLeft[Unit])
    returnCorrectResponsesToSaveJourneyAnswers(
      unauthorisedPaymentsAnswersGen,
      underTestStubbed().saveUnauthorisedPaymentsFromPensions(currentTaxYear, validNino),
      underTestStubbed(errorServiceResponse).saveUnauthorisedPaymentsFromPensions(currentTaxYear, validNino)
    )
  }

  "getPaymentsIntoOverseasPensions" should {
    "return any PaymentsIntoOverseasPensions journey answers from downstream" in {
      val result = {
        mockAuth()
        (pensionsService
          .getPaymentsIntoOverseasPensions(_: JourneyContextWithNino)(_: HeaderCarrier))
          .expects(*, *)
          .returning(EitherT.fromEither[Future](Some(paymentsIntoOverseasPensionsAnswers).asRight[ServiceError]))
        underTest.getPaymentsIntoOverseasPensions(currentTaxYear, validNino)(fakeRequest)
      }

      status(result) mustBe OK
      bodyOf(result) mustBe Json.toJson(Some(paymentsIntoOverseasPensionsAnswers)).toString()
    }
    "return an Error from downstream" in {
      val result = {
        mockAuth()
        (pensionsService
          .getPaymentsIntoOverseasPensions(_: JourneyContextWithNino)(_: HeaderCarrier))
          .expects(*, *)
          .returning(EitherT.fromEither[Future](errorResult.asLeft[Option[PaymentsIntoOverseasPensionsAnswers]]))
        underTest.getPaymentsIntoOverseasPensions(currentTaxYear, validNino)(fakeRequest)
      }.futureValue.header

      assert(result.status == INTERNAL_SERVER_ERROR)
    }
  }

  "savePaymentsIntoOverseasPensions" should {
    def errorServiceResponse = PensionsServiceStub(upsertPaymentsIntoOverseasPensionsResult = errorResult.asLeft[Unit])
    returnCorrectResponsesToSaveJourneyAnswers(
      paymentsIntoOverseasPensionsAnswersGen,
      underTestStubbed().savePaymentsIntoOverseasPensions(currentTaxYear, validNino),
      underTestStubbed(errorServiceResponse).savePaymentsIntoOverseasPensions(currentTaxYear, validNino)
    )
  }

  "getTransfersIntoOverseasPensions" should {
    "return any TransfersIntoOverseasPensions journey answers from downstream" in {
      val result = {
        mockAuth()
        (pensionsService
          .getTransfersIntoOverseasPensions(_: JourneyContextWithNino)(_: HeaderCarrier))
          .expects(*, *)
          .returning(EitherT.fromEither[Future](Some(transfersIntoOverseasPensionsAnswers).asRight[ServiceError]))
        underTest.getTransfersIntoOverseasPensions(currentTaxYear, validNino)(fakeRequest)
      }

      status(result) mustBe OK
      bodyOf(result) mustBe Json.toJson(Some(transfersIntoOverseasPensionsAnswers)).toString()
    }
    "return an Error from downstream" in {
      val result = {
        mockAuth()
        (pensionsService
          .getTransfersIntoOverseasPensions(_: JourneyContextWithNino)(_: HeaderCarrier))
          .expects(*, *)
          .returning(EitherT.fromEither[Future](errorResult.asLeft[Option[TransfersIntoOverseasPensionsAnswers]]))
        underTest.getTransfersIntoOverseasPensions(currentTaxYear, validNino)(fakeRequest)
      }.futureValue.header

      assert(result.status == INTERNAL_SERVER_ERROR)
    }
  }

  "saveTransfersIntoOverseasPensions" should {
    def errorServiceResponse = PensionsServiceStub(upsertTransfersIntoOverseasPensionsResult = errorResult.asLeft[Unit])
    returnCorrectResponsesToSaveJourneyAnswers(
      transfersIntoOverseasPensionsAnswersGen,
      underTestStubbed().saveTransfersIntoOverseasPensions(currentTaxYear, validNino),
      underTestStubbed(errorServiceResponse).saveTransfersIntoOverseasPensions(currentTaxYear, validNino)
    )
  }

  "getIncomeFromOverseasPensions" should {
    "return any IncomeFromOverseasPensions journey answers from downstream" in {
      val result = {
        mockAuth()
        (pensionsService
          .getIncomeFromOverseasPensions(_: JourneyContextWithNino)(_: HeaderCarrier))
          .expects(*, *)
          .returning(EitherT.fromEither[Future](Some(incomeFromOverseasPensionsAnswers).asRight[ServiceError]))
        underTest.getIncomeFromOverseasPensions(currentTaxYear, validNino)(fakeRequest)
      }

      status(result) mustBe OK
      bodyOf(result) mustBe Json.toJson(Some(incomeFromOverseasPensionsAnswers)).toString()
    }
    "return an Error from downstream" in {
      val result = {
        mockAuth()
        (pensionsService
          .getIncomeFromOverseasPensions(_: JourneyContextWithNino)(_: HeaderCarrier))
          .expects(*, *)
          .returning(EitherT.fromEither[Future](errorResult.asLeft[Option[IncomeFromOverseasPensionsAnswers]]))
        underTest.getIncomeFromOverseasPensions(currentTaxYear, validNino)(fakeRequest)
      }.futureValue.header

      assert(result.status == INTERNAL_SERVER_ERROR)
    }
  }

  "saveIncomeFromOverseasPensions" should {
    def errorServiceResponse = PensionsServiceStub(upsertIncomeFromOverseasPensionsResult = errorResult.asLeft[Unit])
    returnCorrectResponsesToSaveJourneyAnswers(
      incomeFromOverseasPensionsAnswersGen,
      underTestStubbed().saveIncomeFromOverseasPensions(currentTaxYear, validNino),
      underTestStubbed(errorServiceResponse).saveIncomeFromOverseasPensions(currentTaxYear, validNino)
    )
  }

  private def returnCorrectResponsesToSaveJourneyAnswers[A: Writes](genData: Gen[A],
                                                                    methodBlock: Action[AnyContent],
                                                                    errorResponseBlock: Action[AnyContent]): Unit = {
    s"return a $NO_CONTENT when successful" in forAll(genData) { data =>
      behave like testRoute(
        request = buildRequest(data),
        expectedStatus = NO_CONTENT,
        expectedBody = "",
        methodBlock = () => methodBlock
      )
    }

    "return an Error" when {
      "service returns an error" in forAll(genData) { data =>
        behave like testRoute(
          request = buildRequest(data),
          expectedStatus = INTERNAL_SERVER_ERROR,
          expectedBody = downstreamErrorMessage,
          methodBlock = () => errorResponseBlock
        )
      }
      "request contains invalid journey answers" in {
        behave like testRoute(
          request = buildRequest(Json.toJson("Invalid")),
          expectedStatus = BAD_REQUEST,
          expectedBody = invalidJsonErrorMessage,
          methodBlock = () => methodBlock
        )
      }
    }
  }
}
