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
import connectors.StateBenefitsConnector
import models.AllStateBenefitsData
import models.common.JourneyContextWithNino
import models.domain.ApiResultT
import models.statebenefit.StateBenefitsUserData
import uk.gov.hmrc.http.HeaderCarrier
import utils.HeaderCarrierUtils.HeaderCarrierOps

import javax.inject.Inject
import scala.concurrent.ExecutionContext

trait StateBenefitService {
  def getStateBenefit(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[AllStateBenefitsData]
  def upsertUkPensionIncome(ctx: JourneyContextWithNino, stateBenefits: StateBenefitsUserData)(implicit hc: HeaderCarrier): ApiResultT[Unit]
}

class StateBenefitServiceImpl @Inject() (connector: StateBenefitsConnector)(implicit ec: ExecutionContext) extends StateBenefitService {

  def getStateBenefit(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[AllStateBenefitsData] = {
    implicit val updatedHc: HeaderCarrier = hc.withInternalId(ctx.mtditid.value)

    EitherT(connector.getStateBenefits(ctx.nino, ctx.taxYear)(updatedHc))
      .leftMap(err => err.toServiceError)
      .map(_.getOrElse(AllStateBenefitsData.empty))
  }

  def upsertUkPensionIncome(ctx: JourneyContextWithNino, stateBenefits: StateBenefitsUserData)(implicit hc: HeaderCarrier): ApiResultT[Unit] = ???
}
