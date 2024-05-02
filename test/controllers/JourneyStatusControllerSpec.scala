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
import models.common.{JourneyNameAndStatus, Mtditid, TaxYear}
import models.error.ServiceError
import play.api.http.Status.OK
import play.api.libs.json.Json
import services.JourneyStatusService
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class JourneyStatusControllerSpec extends TestUtils {
  private val journeyStatusService = mock[JourneyStatusService]

  private val underTest = new JourneyStatusController(journeyStatusService, authorisedAction, mockControllerComponents)
  private val journeyNamesAndStatusList =
    List(JourneyNameAndStatus(PaymentsIntoPensions, Completed), JourneyNameAndStatus(UnauthorisedPayments, InProgress))

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

}
