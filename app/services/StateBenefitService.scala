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
import connectors.StateBenefitsConnector
import models.AllStateBenefitsData
import models.common.{JourneyContextWithNino, Nino, TaxYear}
import models.domain.ApiResultT
import models.frontend.statepension.IncomeFromPensionsStatePensionAnswers
import models.statebenefit.StateBenefitsUserData
import uk.gov.hmrc.http.HeaderCarrier
import utils.HeaderCarrierUtils.HeaderCarrierOps

import java.time.{ZoneOffset, ZonedDateTime}
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait StateBenefitService {
  def getStateBenefits(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[AllStateBenefitsData]
  def upsertStateBenefits(ctx: JourneyContextWithNino, answers: IncomeFromPensionsStatePensionAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
}

@Singleton
class StateBenefitServiceImpl @Inject() (connector: StateBenefitsConnector)(implicit ec: ExecutionContext) extends StateBenefitService {

  def getStateBenefits(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[AllStateBenefitsData] = {
    implicit val updatedHc: HeaderCarrier = hc.withInternalId(ctx.mtditid.value)

    EitherT(connector.getStateBenefits(ctx.nino, ctx.taxYear)(updatedHc))
      .leftMap(err => err.toServiceError)
      .map(_.getOrElse(AllStateBenefitsData.empty))
  }

  def upsertStateBenefits(ctx: JourneyContextWithNino, answers: IncomeFromPensionsStatePensionAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    implicit val updatedHc: HeaderCarrier = hc.withInternalId(ctx.mtditid.value)

    val updatedAnswers = answers.removeEmptyAmounts

    for {
      _ <- runDeleteIfRequired(updatedAnswers, ctx)(updatedHc)
      _ <- runSaveIfRequired(updatedAnswers, ctx)(updatedHc)
    } yield ()
  }

  private def runSaveIfRequired(answers: IncomeFromPensionsStatePensionAnswers, ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val lastUpdated      = ZonedDateTime.now(ZoneOffset.UTC)
    val answerBenefitIds = StateBenefitsUserData.fromJourneyAnswers(ctx, answers, lastUpdated)

    val response = answerBenefitIds.traverse { benefit =>
      EitherT(connector.saveClaim(ctx.nino, benefit)).leftMap(err => err.toServiceError)
    }

    response.void
  }

  private def runDeleteIfRequired(answers: IncomeFromPensionsStatePensionAnswers, ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      priorBenefitIds <- obtainExistingBenefitIds(ctx)
      answerBenefitIds = obtainAnswersBenefitIds(answers)
      ids              = priorBenefitIds.diff(answerBenefitIds).toNel
      uuids            = ids.map(_.toList).getOrElse(Nil)
      _ <- doDelete(uuids, ctx.nino, ctx.taxYear)
    } yield ()

  private def obtainAnswersBenefitIds(answers: IncomeFromPensionsStatePensionAnswers) = {
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
