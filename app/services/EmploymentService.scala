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

import cats.Functor
import cats.data.EitherT
import cats.implicits._
import connectors.{DownstreamErrorOr, EmploymentConnector}
import models.common.JourneyContextWithNino
import models.domain.ApiResultT
import models.employment.CreateUpdateEmploymentRequest
import models.error.ServiceError
import models.frontend.ukpensionincome.UkPensionIncomeAnswers
import models.submission.EmploymentPensions
import uk.gov.hmrc.http.HeaderCarrier
import utils.EitherTOps.EitherTExtensions
import utils.FutureUtils.FutureOps
import utils.HeaderCarrierUtils.HeaderCarrierOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait EmploymentService {
  def getEmployment(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[EmploymentPensions]
  def upsertUkPensionIncome(ctx: JourneyContextWithNino, answers: UkPensionIncomeAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
}

class EmploymentServiceImpl @Inject() (connector: EmploymentConnector)(implicit ec: ExecutionContext) extends EmploymentService {

  def getEmployment(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[EmploymentPensions] = {
    implicit val updatedHc: HeaderCarrier = hc.withInternalId(ctx.mtditid.value)

    EitherT(connector.getEmployments(ctx.nino, ctx.taxYear)(updatedHc))
      .map {
        case Some(e) => EmploymentPensions.fromEmploymentResponse(e)
        case None    => EmploymentPensions.empty
      }
      .leftMap(err => err.toServiceError)
  }

  def upsertUkPensionIncome(ctx: JourneyContextWithNino, answers: UkPensionIncomeAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    implicit val updatedHc: HeaderCarrier = hc.withInternalId(ctx.mtditid.value)

    for {
      existingEmployment <- getEmployment(ctx)(updatedHc)
      priorIds = existingEmployment.employmentData.map(_.employmentId)
      _ <- runDeleteIfRequired(priorIds, answers, ctx)(updatedHc)
      _ <- runSaveIfRequired(ctx, answers)(updatedHc)
    } yield ()
  }

  private def runDeleteIfRequired(priorIds: List[String], answers: UkPensionIncomeAnswers, ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    (priorIds.toNel, answers.uKPensionIncomes.traverse(_.employmentId))
      .mapN { (pIds, sIds) =>
        val idsToDelete = pIds.toList.diff(sIds)
        if (idsToDelete.nonEmpty) doDelete(idsToDelete, ctx).leftAs[ServiceError]
        else EitherT.rightT[Future, ServiceError](())
      }
      .getOrElse(EitherT(().asRight.toFuture))

  private def runSaveIfRequired(ctx: JourneyContextWithNino, answers: UkPensionIncomeAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val isClaimingEmployment = answers.uKPensionIncomes.nonEmpty
    if (isClaimingEmployment) doSave(answers, ctx).leftAs[ServiceError]
    else EitherT(().asRight.toFuture)
  }

  private def doDelete(employmentIds: Seq[String], ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT(
      employmentIds
        .traverse[Future, DownstreamErrorOr[Unit]] { id =>
          connector.deleteEmployment(ctx.nino, ctx.taxYear, id)(hc, ec)
        }
        .map(sequence)).collapse
      .leftMap(err => err.toServiceError)

  // TODO Moved from FE, we need to simplify this
  private def sequence[A, B](s: Seq[Either[A, B]]): Either[A, Seq[B]] =
    s.foldRight(Right(Nil): Either[A, Seq[B]]) { (e, acc) =>
      for {
        xs <- acc
        x  <- e
      } yield xs :+ x
    }

  // TODO Moved from FE, we need to simplify this
  private implicit class FCollapser[F[_]: Functor](outcome: F[Seq[Unit]]) {
    def collapse: F[Unit] =
      outcome.map(_.reduce(_ |+| _))
  }

  private def doSave(answers: UkPensionIncomeAnswers, ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): ApiResultT[Unit] =
    EitherT(
      answers.uKPensionIncomes
        .traverse[Future, DownstreamErrorOr[Unit]] { answers =>
          val request = CreateUpdateEmploymentRequest.fromAnswers(answers)
          connector.saveEmployment(ctx.nino, ctx.taxYear, request)
        }
        .map(sequence)).collapse
      .leftMap(err => err.toServiceError)

}
