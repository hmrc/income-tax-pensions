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

package stubs.services

import cats.data.EitherT
import models.common._
import models.error.ServiceError
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import services.JourneyStatusService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockJourneyStatusService extends MockFactory {
  self: TestSuite =>

  val mockJourneyStatusService: JourneyStatusService = mock[JourneyStatusService]

  def mockGetAllStatuses(
                          taxYear: TaxYear,
                          mtditid: Mtditid
                        )(
                          result: Either[ServiceError, List[JourneyNameAndStatus]]
                        ): Unit =
    (mockJourneyStatusService
      .getAllStatuses(_: TaxYear, _: Mtditid)(_: HeaderCarrier))
      .expects(taxYear, mtditid, *)
      .returning(EitherT.fromEither[Future](result))

  def mockGetJourneyStatus(
                            ctx: JourneyContext
                          )(
                            result: Either[ServiceError, List[JourneyNameAndStatus]]
                          ): Unit =
    (mockJourneyStatusService
      .getJourneyStatus(_: JourneyContext))
      .expects(ctx)
      .returning(EitherT.fromEither[Future](result))

  def mockSaveJourneyStatus(
                             ctx: JourneyContext,
                             journeyStatus: JourneyStatus
                           )(
                             result: Either[ServiceError, Unit] = Right(())
                           ): Unit =
    (mockJourneyStatusService
      .saveJourneyStatus(_: JourneyContext, _: JourneyStatus))
      .expects(ctx, journeyStatus)
      .returning(EitherT.fromEither[Future](result))
}
