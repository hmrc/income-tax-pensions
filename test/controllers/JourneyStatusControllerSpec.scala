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

package controllers

import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import models.common.Journey.{PaymentsIntoPensions, UnauthorisedPayments}
import models.common.JourneyStatus.{Completed, InProgress}
import models.common._
import models.error.ServiceError
import play.api.http.Status.{NO_CONTENT, OK}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import services.JourneyStatusService
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class JourneyStatusControllerSpec extends TestUtils {
  private val journeyStatusService = mock[JourneyStatusService]

  private val underTest = new JourneyStatusController(journeyStatusService, authorisedAction, mockControllerComponents)
  private val journeyNamesAndStatusList =
    List(JourneyNameAndStatus(PaymentsIntoPensions, Completed), JourneyNameAndStatus(UnauthorisedPayments, InProgress))
  private val singleJourneyNameAndStatusList =
    List(JourneyNameAndStatus(PaymentsIntoPensions, Completed))

  "getAllStatuses" should {
    "return a list of all journey statuses" in {
      val result = {
        mockAuth()
        (journeyStatusService
          .getAllStatuses(_: TaxYear, _: Mtditid)(_: HeaderCarrier))
          .expects(*, *, *)
          .returning(EitherT.fromEither[Future](journeyNamesAndStatusList.asRight[ServiceError]))
        underTest.getAllStatuses(taxYear)(fakeRequest)
      }

      status(result) mustBe OK
      bodyOf(result) mustBe Json.toJson(journeyNamesAndStatusList).toString()
    }
  }

  "getJourneyStatus" should {
    "return a list containing a journey status" in {
      val result = {
        mockAuth()
        (journeyStatusService
          .getJourneyStatus(_: JourneyContext))
          .expects(*)
          .returning(EitherT.fromEither[Future](singleJourneyNameAndStatusList.asRight[ServiceError]))
        underTest.getJourneyStatus(taxYear.endYear, PaymentsIntoPensions)(fakeRequest)
      }

      status(result) mustBe OK
      bodyOf(result) mustBe Json.toJson(singleJourneyNameAndStatusList).toString()
    }
  }

  "saveJourneyStatus" should {
    "return unit as a right confirming the journey status has been saved correctly" in {
      val fakeRequestWithJourney =
        FakeRequest()
          .withHeaders("mtditid" -> "1234567890")
          .withJsonBody(Json.toJson(Completed))

      val result = {
        mockAuth()
        (journeyStatusService
          .saveJourneyStatus(_: JourneyContext, _: JourneyStatus))
          .expects(*, *)
          .returning(EitherT.fromEither[Future](().asRight))
        underTest.saveJourneyStatus(taxYear.endYear, PaymentsIntoPensions)(fakeRequestWithJourney)
      }

      status(result) mustBe NO_CONTENT
      bodyOf(result) mustBe ""
    }
  }

}
