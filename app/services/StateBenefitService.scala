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
import cats.implicits.{catsSyntaxList, toFunctorOps, toTraverseOps}
import connectors.{StateBenefitsConnector, _}
import models.common.{JourneyContextWithNino, Nino, TaxYear}
import models.domain.ApiResultT
import models.error.ServiceError
import models.statebenefit.StateBenefitsUserData
import models.{APIErrorModel, AllStateBenefitsData, IncomeTaxUserData, StateBenefit, StateBenefitsData, User}
import uk.gov.hmrc.http.HeaderCarrier
import utils.HeaderCarrierUtils.HeaderCarrierOps

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait StateBenefitService {
  def getStateBenefits(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[AllStateBenefitsData]
  def upsertStateBenefits(ctx: JourneyContextWithNino, stateBenefits: List[StateBenefitsUserData])(implicit hc: HeaderCarrier): ApiResultT[Unit]
}

class StateBenefitServiceImpl @Inject() (connector: StateBenefitsConnector)(implicit ec: ExecutionContext) extends StateBenefitService {

  def getStateBenefits(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[AllStateBenefitsData] = {
    implicit val updatedHc: HeaderCarrier = hc.withInternalId(ctx.mtditid.value)

    EitherT(connector.getStateBenefits(ctx.nino, ctx.taxYear)(updatedHc))
      .leftMap(err => err.toServiceError)
      .map(_.getOrElse(AllStateBenefitsData.empty))
  }

  def upsertStateBenefits(ctx: JourneyContextWithNino, stateBenefits: List[StateBenefitsUserData])(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    stateBenefits.traverse { stateBenefit =>
      for {
        _ <- runDeleteIfRequired(stateBenefit, ctx)
        _ <- runSaveIfRequired(stateBenefit, ctx)
      } yield ()
    }.void

  private def runSaveIfRequired(answers: StateBenefitsUserData, ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[Unit] =
    if (answers.claim.exists(_.amount.nonEmpty))
      EitherT(connector.saveClaim(ctx.nino, answers)).leftMap(err => err.toServiceError)
    else
      EitherT.pure[Future, ServiceError](())

  private def runDeleteIfRequired(stateBenefitsData: StateBenefitsData, ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[Unit] =
    for {
      priorBenefitIds <- obtainExistingBenefitIds(ctx)
      answerBenefitIds = obtainAnswersBenefitIds(stateBenefitsData)
      ids              = priorBenefitIds.diff(answerBenefitIds).toNel
      _ <- doDelete(ids.flatMap(_.toList).toList, ctx.nino, ctx.taxYear)
    } yield ()

  private def obtainAnswersBenefitIds(answers: StateBenefitsData) = {
    val maybeSpId        = answers.statePension.map(_.benefitId)
    val maybeSpLumpSumId = answers.statePensionLumpSum.map(_.benefitId)

    List(maybeSpId, maybeSpLumpSumId).flatten
  }

  private def doDelete(ids: List[UUID], nino: Nino, taxYear: TaxYear)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] =
    ids
      .traverse(id => EitherT(connector.deleteClaim(nino, taxYear, id)).leftMap(_.toServiceError))
      .void

  private def obtainExistingBenefitIds(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[List[UUID]] =
    getStateBenefits(ctx).map { stateBenefitData =>
      val maybeSpId        = stateBenefitData.stateBenefitsData.flatMap(_.statePension.map(_.benefitId))
      val maybeSpLumpSumId = stateBenefitData.stateBenefitsData.flatMap(_.statePensionLumpSum.map(_.benefitId))
      List(maybeSpId, maybeSpLumpSumId).flatten
    }
}
