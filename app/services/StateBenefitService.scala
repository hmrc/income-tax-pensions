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

import cats.implicits.{catsSyntaxList, toFunctorOps, toTraverseOps}
import connectors.StateBenefitsConnector
import models.AllStateBenefitsData
import models.common.{JourneyContextWithNino, Nino, TaxYear}
import models.domain.ApiResultT
import models.frontend.statepension.IncomeFromPensionsStatePensionAnswers
import models.statebenefit.StateBenefitsUserData
import uk.gov.hmrc.http.HeaderCarrier
import utils.HeaderCarrierUtils.HeaderCarrierOps

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

trait StateBenefitService {
  def getStateBenefits(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[AllStateBenefitsData]]
  def upsertStateBenefits(ctx: JourneyContextWithNino, answers: IncomeFromPensionsStatePensionAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
}

@Singleton
class StateBenefitServiceImpl @Inject() (connector: StateBenefitsConnector)(implicit ec: ExecutionContext) extends StateBenefitService {

  def getStateBenefits(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[AllStateBenefitsData]] = {
    implicit val updatedHc: HeaderCarrier = hc.withInternalId(ctx.mtditid.value)

    connector
      .getStateBenefits(ctx.nino, ctx.taxYear)(updatedHc)
      .leftMap(err => err.toServiceError)
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

  private[services] def now: Instant = Instant.ofEpochMilli(Instant.now().toEpochMilli)

  private def runSaveIfRequired(answers: IncomeFromPensionsStatePensionAnswers, ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val stateBenefits = StateBenefitsUserData.fromJourneyAnswers(ctx, answers, now)

    val response = stateBenefits.traverse { benefit =>
      connector.saveClaim(ctx.nino, benefit).leftMap(err => err.toServiceError)
    }

    response.void
  }

  private def runDeleteIfRequired(answers: IncomeFromPensionsStatePensionAnswers, ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    for {
      priorBenefitIds <- obtainExistingBenefitIds(ctx)
      answerBenefitIds = obtainAnswersBenefitIds(answers)
      ids              = priorBenefitIds.diff(answerBenefitIds).toNel
      removedUUIDs     = ids.map(_.toList).getOrElse(Nil)
      _ <- doDelete(removedUUIDs, ctx.nino, ctx.taxYear)
    } yield ()

  private def obtainAnswersBenefitIds(answers: IncomeFromPensionsStatePensionAnswers) = {
    val maybeSpId        = answers.statePension.flatMap(_.benefitId)
    val maybeSpLumpSumId = answers.statePensionLumpSum.flatMap(_.benefitId)

    List(maybeSpId, maybeSpLumpSumId).flatten
  }

  private def doDelete(ids: List[UUID], nino: Nino, taxYear: TaxYear)(implicit hc: HeaderCarrier, ec: ExecutionContext): ApiResultT[Unit] =
    ids
      .traverse(id => connector.deleteClaim(nino, taxYear, id).leftMap(_.toServiceError))
      .void

  private def obtainExistingBenefitIds(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[List[UUID]] =
    for {
      stateBenefitData <- getStateBenefits(ctx)
      maybeExistingStateBenefits     = stateBenefitData.map(_.stateBenefitsData)
      maybeSpId: Option[UUID]        = maybeExistingStateBenefits.flatMap(_.flatMap(_.statePension.map(_.benefitId)))
      maybeSpLumpSumId: Option[UUID] = maybeExistingStateBenefits.flatMap(_.flatMap(_.statePensionLumpSum.map(_.benefitId)))
    } yield List(maybeSpId, maybeSpLumpSumId).flatten
}
