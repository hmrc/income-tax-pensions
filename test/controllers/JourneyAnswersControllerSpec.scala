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
import models.frontend.{AnnualAllowancesAnswers, PaymentsIntoPensionsAnswers, TransfersIntoOverseasPensionsAnswers}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import services.PensionsService
import testdata.annualAllowances.annualAllowancesAnswers
import testdata.paymentsIntoPensions.paymentsIntoPensionsAnswers
import testdata.transfersIntoOverseasPensions.transfersIntoOverseasPensionsAnswers
import uk.gov.hmrc.http.HeaderCarrier
import utils.EitherTTestOps.convertScalaFuture
import utils.TestUtils

import scala.concurrent.Future

class JourneyAnswersControllerSpec extends TestUtils {

  private val pensionsService           = mock[PensionsService]
  private val underTest                 = new JourneyAnswersController(pensionsService, authorisedAction, mockControllerComponents)
  private val errorResult: ServiceError = DownstreamError("ERROR")

//  private def fakePutRequestWithBody(body: JsValue, url: String): FakeRequest[_] =
//    FakeRequest("PUT", url, headers = FakeHeaders(Seq(HeaderNames.HOST -> "localhost", "mtditid" -> mtditid)), body = body)

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

  // TODO add/fix PUT tests, FakeRequest contains AnyContentAsEmpty rather than the body being passed
//  "savePaymentsIntoPensions" should {
//    val url = s"/update-and-submit-income-tax-return/pensions/${currentTaxYear.endYear}/payments-into-pensions/$nino/answers"
//
//    def request(body: JsValue): FakeRequest[_] = fakePutRequestWithBody(body, url)
//    "return a Unit when journey answers are saved downstream" in {
//      val result = {
//        mockAuth()
//        (pensionsService
//          .upsertPaymentsIntoPensions(_: JourneyContextWithNino, _: PaymentsIntoPensionsAnswers)(_: HeaderCarrier))
//          .expects(sampleCtx, paymentsIntoPensionsAnswers, *)
//          .returning(EitherT.fromEither[Future](().asRight[ServiceError]))
//
//        underTest.savePaymentsIntoPensions(currentTaxYear, validNino)(request(Json.toJson(paymentsIntoPensionsAnswers)))
//      }.run().futureValue.header
//
//      assert(result.status == NO_CONTENT)
//    }
//    "return an Error" when {
//      "service returns an error" in {
//        val result = {
//          mockAuth()
//          (pensionsService
//            .upsertPaymentsIntoPensions(_: JourneyContextWithNino, _: PaymentsIntoPensionsAnswers)(_: HeaderCarrier))
//            .expects(sampleCtx, paymentsIntoPensionsAnswers, *)
//            .returning(EitherT.fromEither[Future](errorResult.asLeft[Unit]))
//          underTest.savePaymentsIntoPensions(currentTaxYear, validNino)(request(Json.toJson(paymentsIntoPensionsAnswers)))
//        }.run().futureValue.header
//
//        assert(result.status == INTERNAL_SERVER_ERROR)
//      }
//      "request contains invalid journey answers" in {
//        val result = {
//          mockAuth()
//          underTest.savePaymentsIntoPensions(currentTaxYear, validNino)(request(Json.toJson("Invalid")))
//        }.run().futureValue.header
//
//        assert(result.status == BAD_REQUEST)
//      }
//    }
//  }

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

  "getTransfersIntoOverseasPensions" should {
    "return any AnnualAllowances journey answers from downstream" in {
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

}
