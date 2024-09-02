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
import cats.implicits._
import models.common.JourneyContextWithNino
import models.commonTaskList.TaskListSection
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend._
import models.frontend.statepension.IncomeFromPensionsStatePensionAnswers
import models.frontend.ukpensionincome.UkPensionIncomeAnswers
import models.{AllPensionsData, ServiceErrorModel}
import services.PensionsService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class PensionsServiceStub(getPaymentsIntoPensionsResult: Either[ServiceError, Option[PaymentsIntoPensionsAnswers]] = Right(None),
                               getUkPensionIncomeResult: Either[ServiceError, Option[UkPensionIncomeAnswers]] = Right(None),
                               getAnnualAllowancesResult: Either[ServiceError, Option[AnnualAllowancesAnswers]] = Right(None),
                               getUnauthorisedPaymentsFromPensionsResult: Either[ServiceError, Option[UnauthorisedPaymentsAnswers]] = Right(None),
                               getPaymentsIntoOverseasPensionsResult: Either[ServiceError, Option[PaymentsIntoOverseasPensionsAnswers]] = Right(None),
                               getIncomeFromOverseasPensionsResult: Either[ServiceError, Option[IncomeFromOverseasPensionsAnswers]] = Right(None),
                               getTransfersIntoOverseasPensionsResult: Either[ServiceError, Option[TransfersIntoOverseasPensionsAnswers]] = Right(
                                 None),
                               getShortServiceRefunds: Either[ServiceError, Option[ShortServiceRefundsAnswers]] = Right(None),
                               getAllPensionsDataResult: Either[ServiceErrorModel, AllPensionsData] = Right(AllPensionsData.empty),
                               upsertPaymentsIntoPensionsResult: Either[ServiceError, Unit] = Right(()),
                               upsertUkPensionIncomeResult: Either[ServiceError, Unit] = Right(()),
                               upsertAnnualAllowancesResult: Either[ServiceError, Unit] = Right(()),
                               upsertUnauthorisedPaymentsFromPensionsResult: Either[ServiceError, Unit] = Right(()),
                               upsertIncomeFromOverseasPensionsResult: Either[ServiceError, Unit] = Right(()),
                               upsertPaymentsIntoOverseasPensionsResult: Either[ServiceError, Unit] = Right(()),
                               upsertTransfersIntoOverseasPensionsResult: Either[ServiceError, Unit] = Right(()),
                               upsertShortServiceRefunds: Either[ServiceError, Unit] = Right(()))
    extends PensionsService {
  def getPaymentsIntoPensions(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[PaymentsIntoPensionsAnswers]] =
    EitherT.fromEither(getPaymentsIntoPensionsResult)
  def upsertPaymentsIntoPensions(ctx: JourneyContextWithNino, answers: PaymentsIntoPensionsAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT.fromEither(upsertPaymentsIntoPensionsResult)
  def getUkPensionIncome(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[UkPensionIncomeAnswers]] =
    EitherT.fromEither(getUkPensionIncomeResult)
  def upsertUkPensionIncome(ctx: JourneyContextWithNino, answers: UkPensionIncomeAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT.fromEither(upsertUkPensionIncomeResult)
  def getAnnualAllowances(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[AnnualAllowancesAnswers]] =
    EitherT.fromEither(getAnnualAllowancesResult)
  def upsertAnnualAllowances(ctx: JourneyContextWithNino, answers: AnnualAllowancesAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT.fromEither(upsertAnnualAllowancesResult)
  def getUnauthorisedPaymentsFromPensions(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[UnauthorisedPaymentsAnswers]] =
    EitherT.fromEither(getUnauthorisedPaymentsFromPensionsResult)
  def upsertUnauthorisedPaymentsFromPensions(ctx: JourneyContextWithNino, answers: UnauthorisedPaymentsAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT.fromEither(upsertUnauthorisedPaymentsFromPensionsResult)
  def getPaymentsIntoOverseasPensions(ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Option[PaymentsIntoOverseasPensionsAnswers]] =
    EitherT.fromEither(getPaymentsIntoOverseasPensionsResult)
  def upsertPaymentsIntoOverseasPensions(ctx: JourneyContextWithNino, answers: PaymentsIntoOverseasPensionsAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT.fromEither(upsertPaymentsIntoOverseasPensionsResult)
  def getIncomeFromOverseasPensions(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[IncomeFromOverseasPensionsAnswers]] =
    EitherT.fromEither(getIncomeFromOverseasPensionsResult)
  def upsertIncomeFromOverseasPensions(ctx: JourneyContextWithNino, answers: IncomeFromOverseasPensionsAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT.fromEither(upsertIncomeFromOverseasPensionsResult)
  def getTransfersIntoOverseasPensions(ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Option[TransfersIntoOverseasPensionsAnswers]] =
    EitherT.fromEither(getTransfersIntoOverseasPensionsResult)
  def upsertTransfersIntoOverseasPensions(ctx: JourneyContextWithNino, answers: TransfersIntoOverseasPensionsAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT.fromEither(upsertTransfersIntoOverseasPensionsResult)
  def getShortServiceRefunds(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ShortServiceRefundsAnswers]] =
    EitherT.fromEither(getShortServiceRefunds)
  def upsertShortServiceRefunds(ctx: JourneyContextWithNino, answers: ShortServiceRefundsAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    EitherT.fromEither(upsertShortServiceRefunds)
  def getAllPensionsData(nino: String, taxYear: Int, mtditid: String)(implicit
      hc: HeaderCarrier): Future[Either[ServiceErrorModel, AllPensionsData]] = Future.successful(getAllPensionsDataResult)
  def getStatePension(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[IncomeFromPensionsStatePensionAnswers]] = ???
  def upsertStatePension(ctx: JourneyContextWithNino, answers: IncomeFromPensionsStatePensionAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    ???
  def getCommonTaskList(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Seq[TaskListSection]] = ???
}
