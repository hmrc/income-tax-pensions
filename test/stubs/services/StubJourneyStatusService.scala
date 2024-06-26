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
import models.domain.ApiResultT
import models.error.ServiceError
import services.JourneyStatusService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class StubJourneyStatusService(getAllStatusesResult: List[JourneyNameAndStatus] = Nil) extends JourneyStatusService {

  def getAllStatuses(taxYear: TaxYear, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[List[JourneyNameAndStatus]] =
    EitherT.rightT[Future, ServiceError](getAllStatusesResult)

  def getJourneyStatus(ctx: JourneyContext): ApiResultT[List[JourneyNameAndStatus]] =
    EitherT.rightT[Future, ServiceError](List.empty[JourneyNameAndStatus])

  def saveJourneyStatus(ctx: JourneyContext, journeyStatus: JourneyStatus): ApiResultT[Unit] =
    EitherT.rightT[Future, ServiceError](())
}
