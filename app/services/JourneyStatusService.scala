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

import models.common._
import models.domain.ApiResultT
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}

trait JourneyStatusService {
  def getAllStatuses(taxYear: TaxYear, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[List[JourneyNameAndStatus]]
  def getJourneyStatus(ctx: JourneyContext): ApiResultT[List[JourneyNameAndStatus]]
  def saveJourneyStatus(ctx: JourneyContext, journeyStatus: JourneyStatus): ApiResultT[Unit]
}

@Singleton
class JourneyStatusServiceImpl @Inject() (repository: JourneyAnswersRepository) extends JourneyStatusService {

  def getAllStatuses(taxYear: TaxYear, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[List[JourneyNameAndStatus]] =
    repository.getAllJourneyStatuses(taxYear, mtditid)

  def getJourneyStatus(ctx: JourneyContext): ApiResultT[List[JourneyNameAndStatus]] =
    repository.getJourneyStatus(ctx)

  def saveJourneyStatus(ctx: JourneyContext, journeyStatus: JourneyStatus): ApiResultT[Unit] =
    repository.setStatus(ctx, journeyStatus)
}
