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

import cats.data.{EitherT, NonEmptyList}
import cats.implicits._
import config.AppConfig
import connectors._
import models._
import models.charges.{CreateUpdatePensionChargesRequestModel, GetPensionChargesRequestModel}
import models.common._
import models.commonTaskList.{TaskListModel, fromAllJourneys}
import models.database._
import models.domain.{AllJourneys, ApiResult, ApiResultT}
import models.error.ServiceError
import models.error.ServiceError.DownstreamError
import models.frontend._
import models.frontend.statepension.IncomeFromPensionsStatePensionAnswers
import models.frontend.ukpensionincome.UkPensionIncomeAnswers
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.HeaderCarrierUtils.HeaderCarrierOps
import utils.Logging

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait PensionsService {
  def getPaymentsIntoPensions(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[PaymentsIntoPensionsAnswers]]
  def upsertPaymentsIntoPensions(ctx: JourneyContextWithNino, answers: PaymentsIntoPensionsAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]

  def getUkPensionIncome(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[UkPensionIncomeAnswers]]
  def upsertUkPensionIncome(ctx: JourneyContextWithNino, answers: UkPensionIncomeAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]

  def getStatePension(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[IncomeFromPensionsStatePensionAnswers]]
  def upsertStatePension(ctx: JourneyContextWithNino, answers: IncomeFromPensionsStatePensionAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]

  def getAnnualAllowances(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[AnnualAllowancesAnswers]]
  def upsertAnnualAllowances(ctx: JourneyContextWithNino, answers: AnnualAllowancesAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]

  def getUnauthorisedPaymentsFromPensions(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[UnauthorisedPaymentsAnswers]]
  def upsertUnauthorisedPaymentsFromPensions(ctx: JourneyContextWithNino, answers: UnauthorisedPaymentsAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit]

  def getPaymentsIntoOverseasPensions(ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Option[PaymentsIntoOverseasPensionsAnswers]]
  def upsertPaymentsIntoOverseasPensions(ctx: JourneyContextWithNino, answers: PaymentsIntoOverseasPensionsAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit]

  def getIncomeFromOverseasPensions(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[IncomeFromOverseasPensionsAnswers]]
  def upsertIncomeFromOverseasPensions(ctx: JourneyContextWithNino, answers: IncomeFromOverseasPensionsAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit]

  def getTransfersIntoOverseasPensions(ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Option[TransfersIntoOverseasPensionsAnswers]]
  def upsertTransfersIntoOverseasPensions(ctx: JourneyContextWithNino, answers: TransfersIntoOverseasPensionsAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit]

  def getShortServiceRefunds(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ShortServiceRefundsAnswers]]
  def upsertShortServiceRefunds(ctx: JourneyContextWithNino, answers: ShortServiceRefundsAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]

  def getCommonTaskList(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[TaskListModel]
  def getAllPensionsData(nino: String, taxYear: Int, mtditid: String)(implicit hc: HeaderCarrier): Future[Either[ServiceErrorModel, AllPensionsData]]
}

// TODO: Add test
class PensionsServiceImpl @Inject() (appConfig: AppConfig,
                                     reliefsConnector: PensionReliefsConnector,
                                     chargesConnector: PensionChargesConnector,
                                     stateBenfitService: StateBenefitService,
                                     pensionIncomeConnector: PensionIncomeConnector,
                                     employmentService: EmploymentService,
                                     statusService: JourneyStatusService,
                                     repository: JourneyAnswersRepository)(implicit ec: ExecutionContext)
    extends PensionsService
    with Logging {

  def getPaymentsIntoPensions(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[PaymentsIntoPensionsAnswers]] =
    for {
      maybeReliefs   <- reliefsConnector.getPensionReliefsT(ctx.nino, ctx.taxYear)
      maybeDbAnswers <- repository.getPaymentsIntoPensions(ctx)
    } yield PaymentsIntoPensionsAnswers.mkAnswers(maybeReliefs, maybeDbAnswers)

  def upsertPaymentsIntoPensions(ctx: JourneyContextWithNino, answers: PaymentsIntoPensionsAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val storageAnswers = PaymentsIntoPensionsStorageAnswers.fromJourneyAnswers(answers)

    for {
      existingRelief <- reliefsConnector.getPensionReliefsT(ctx.nino, ctx.taxYear)
      maybeOverseasPensionSchemeContributions = existingRelief.flatMap(_.pensionReliefs.overseasPensionSchemeContributions)
      updatedReliefs: PensionReliefs          = answers.toPensionReliefs(maybeOverseasPensionSchemeContributions)
      _ <- createOrDeleteReliefsWhenEmpty(ctx, updatedReliefs, existingRelief)
      _ <- repository.upsertPaymentsIntoPensions(ctx, storageAnswers)
    } yield ()
  }

  def getUkPensionIncome(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[UkPensionIncomeAnswers]] =
    for {
      employment     <- employmentService.getEmployment(ctx)
      maybeDbAnswers <- repository.getUkPensionIncome(ctx)
    } yield UkPensionIncomeAnswers.mkAnswers(employment, maybeDbAnswers)

  def upsertUkPensionIncome(ctx: JourneyContextWithNino, answers: UkPensionIncomeAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val storageAnswers = UkPensionIncomeStorageAnswers.fromJourneyAnswers(answers)

    for {
      _ <- employmentService.upsertUkPensionIncome(ctx, answers)
      _ <- repository.upsertUkPensionIncome(ctx, storageAnswers)
    } yield ()
  }

  def getStatePension(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[IncomeFromPensionsStatePensionAnswers]] =
    for {
      stateBenefits  <- stateBenfitService.getStateBenefits(ctx)
      maybeDbAnswers <- repository.getStatePension(ctx)
    } yield IncomeFromPensionsStatePensionAnswers.mkAnswers(stateBenefits, maybeDbAnswers)

  def upsertStatePension(ctx: JourneyContextWithNino, answers: IncomeFromPensionsStatePensionAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val storageAnswers = IncomeFromPensionsStatePensionStorageAnswers.fromJourneyAnswers(answers)

    for {
      _ <- stateBenfitService.upsertStateBenefits(ctx, answers)
      _ <- repository.upsertStatePension(ctx, storageAnswers)
    } yield ()
  }

  def getAnnualAllowances(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[AnnualAllowancesAnswers]] =
    for {
      maybeCharges   <- chargesConnector.getPensionChargesT(ctx.nino, ctx.taxYear)
      maybeDbAnswers <- repository.getAnnualAllowances(ctx)
    } yield AnnualAllowancesAnswers.mkAnswers(maybeCharges, maybeDbAnswers)

  def upsertAnnualAllowances(ctx: JourneyContextWithNino, answers: AnnualAllowancesAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val storageAnswers = AnnualAllowancesStorageAnswers.fromJourneyAnswers(answers)

    for {
      getCharges <- chargesConnector.getPensionChargesT(ctx.nino, ctx.taxYear)
      existingCharges      = getCharges.map(_.toCreateUpdatePensionChargesRequestModel).getOrElse(CreateUpdatePensionChargesRequestModel.empty)
      updatedContributions = answers.toPensionChargesContributions.some
      updatedCharges       = existingCharges.copy(pensionContributions = updatedContributions)
      _ <- chargesConnector.createUpdatePensionChargesT(ctx, updatedCharges)
      _ <- repository.upsertAnnualAllowances(ctx, storageAnswers)
    } yield ()
  }

  def getUnauthorisedPaymentsFromPensions(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[UnauthorisedPaymentsAnswers]] =
    for {
      maybeCharges   <- chargesConnector.getPensionChargesT(ctx.nino, ctx.taxYear)
      maybeDbAnswers <- repository.getUnauthorisedPayments(ctx)
    } yield UnauthorisedPaymentsAnswers.mkAnswers(maybeCharges, maybeDbAnswers)

  def upsertUnauthorisedPaymentsFromPensions(ctx: JourneyContextWithNino, answers: UnauthorisedPaymentsAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val storageAnswers = UnauthorisedPaymentsStorageAnswers.fromJourneyAnswers(answers)

    for {
      getCharges <- chargesConnector.getPensionChargesT(ctx.nino, ctx.taxYear)
      existingCharges = getCharges.map(_.toCreateUpdatePensionChargesRequestModel).getOrElse(CreateUpdatePensionChargesRequestModel.empty)
      pensionSchemeUnauthorisedPayments = answers.toPensionCharges.some
      updatedCharges                    = existingCharges.copy(pensionSchemeUnauthorisedPayments = pensionSchemeUnauthorisedPayments)
      _ <- chargesConnector.createUpdatePensionChargesT(ctx, updatedCharges)
      _ <- repository.upsertUnauthorisedPayments(ctx, storageAnswers)
    } yield ()
  }

  def getIncomeFromOverseasPensions(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[IncomeFromOverseasPensionsAnswers]] =
    for {
      maybeIncome    <- pensionIncomeConnector.getPensionIncomeT(ctx.nino, ctx.taxYear)
      maybeDbAnswers <- repository.getIncomeFromOverseasPensions(ctx)
    } yield IncomeFromOverseasPensionsAnswers.mkAnswers(maybeIncome, maybeDbAnswers)

  def upsertIncomeFromOverseasPensions(ctx: JourneyContextWithNino, answers: IncomeFromOverseasPensionsAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val storageAnswers = IncomeFromOverseasPensionsStorageAnswers.fromJourneyAnswers(answers)

    for {
      incomeResponse <- pensionIncomeConnector.getPensionIncomeT(ctx.nino, ctx.taxYear)
      existingIncome                    = incomeResponse.map(_.toCreateUpdatePensionIncomeModel)
      updatedIncomeFromOverseasPensions = answers.toForeignPension.map(_.toList)
      updatedIncome = existingIncome.getOrElse(CreateUpdatePensionIncomeModel.empty).copy(foreignPension = updatedIncomeFromOverseasPensions)
      _ <- createOrDeleteIncomesWhenEmpty(ctx, updatedIncome, existingIncome)
      _ <- repository.upsertIncomeFromOverseasPensions(ctx, storageAnswers)
    } yield ()
  }

  def getPaymentsIntoOverseasPensions(ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Option[PaymentsIntoOverseasPensionsAnswers]] =
    for {
      maybeReliefs   <- reliefsConnector.getPensionReliefsT(ctx.nino, ctx.taxYear)
      maybeIncomes   <- pensionIncomeConnector.getPensionIncomeT(ctx.nino, ctx.taxYear)
      maybeDbAnswers <- repository.getPaymentsIntoOverseasPensions(ctx)
    } yield PaymentsIntoOverseasPensionsAnswers.mkAnswers(maybeReliefs, maybeIncomes, maybeDbAnswers)

  def upsertPaymentsIntoOverseasPensions(ctx: JourneyContextWithNino, answers: PaymentsIntoOverseasPensionsAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val storageAnswers = answers.toStorageAnswers

    for {
      existingRelief <- reliefsConnector.getPensionReliefsT(ctx.nino, ctx.taxYear)
      updatedReliefs = existingRelief
        .getOrElse(GetPensionReliefsModel.empty)
        .pensionReliefs
        .copy(overseasPensionSchemeContributions = answers.paymentsIntoOverseasPensionsAmount)
      existingIncomes <- pensionIncomeConnector.getPensionIncomeT(ctx.nino, ctx.taxYear)
      updatedIncomes = CreateUpdatePensionIncomeModel(
        foreignPension = existingIncomes.flatMap(_.foreignPension),
        overseasPensionContribution = NonEmptyList.fromList(answers.schemes.map(_.toOverseasPensionsContributions)).map(_.toList)
      )
      _ <- createOrDeleteReliefsWhenEmpty(ctx, updatedReliefs, existingRelief)
      _ <- createOrDeleteIncomesWhenEmpty(ctx, updatedIncomes, existingIncomes.map(_.toCreateUpdatePensionIncomeModel))
      _ <- repository.upsertPaymentsIntoOverseasPensions(ctx, storageAnswers)
    } yield ()
  }

  def getTransfersIntoOverseasPensions(ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Option[TransfersIntoOverseasPensionsAnswers]] =
    for {
      maybeCharges   <- chargesConnector.getPensionChargesT(ctx.nino, ctx.taxYear)
      maybeDbAnswers <- repository.getTransferIntoOverseasPensions(ctx)
    } yield TransfersIntoOverseasPensionsAnswers.mkAnswers(maybeCharges, maybeDbAnswers)

  def upsertTransfersIntoOverseasPensions(ctx: JourneyContextWithNino, answers: TransfersIntoOverseasPensionsAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val storageAnswers = TransfersIntoOverseasPensionsStorageAnswers.fromJourneyAnswers(answers)

    for {
      getCharges <- chargesConnector.getPensionChargesT(ctx.nino, ctx.taxYear)
      existingCharges      = getCharges.map(_.toCreateUpdatePensionChargesRequestModel).getOrElse(CreateUpdatePensionChargesRequestModel.empty)
      updatedContributions = answers.toPensionSchemeOverseasTransfers
      updatedCharges       = existingCharges.copy(pensionSchemeOverseasTransfers = updatedContributions)
      _ <- createOrDeleteChargesWhenEmpty(ctx, updatedCharges, existingCharges)
      _ <- repository.upsertTransferIntoOverseasPensions(ctx, storageAnswers)
    } yield ()
  }

  def getShortServiceRefunds(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ShortServiceRefundsAnswers]] =
    for {
      maybeCharges   <- chargesConnector.getPensionChargesT(ctx.nino, ctx.taxYear)
      maybeDbAnswers <- repository.getShortServiceRefunds(ctx)
    } yield ShortServiceRefundsAnswers.mkAnswers(maybeCharges, maybeDbAnswers)

  def upsertShortServiceRefunds(ctx: JourneyContextWithNino, answers: ShortServiceRefundsAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val storageAnswers = ShortServiceRefundsStorageAnswers.fromJourneyAnswers(answers)

    for {
      getCharges <- chargesConnector.getPensionChargesT(ctx.nino, ctx.taxYear)
      existingCharges = getCharges.map(_.toCreateUpdatePensionChargesRequestModel).getOrElse(CreateUpdatePensionChargesRequestModel.empty)
      overseasPensionContributions = answers.toOverseasPensions.some
      updatedCharges               = existingCharges.copy(overseasPensionContributions = overseasPensionContributions)
      _ <- createOrDeleteChargesWhenEmpty(ctx, updatedCharges, existingCharges)
      _ <- repository.upsertShortServiceRefunds(ctx, storageAnswers)
    } yield ()
  }

  // TODO: Decide whether loading employments and state benefits through pensions is what we want. The submissions service
  //       (aka "the cache") already loads employments and state benefits so adding the calls to load through pensions
  //       duplicates the data in the cache.
  def getAllPensionsData(nino: String, taxYear: Int, mtditid: String)(implicit
      hc: HeaderCarrier): Future[Either[ServiceErrorModel, AllPensionsData]] = {
    val ctx = JourneyContextWithNino(TaxYear(taxYear), Mtditid(mtditid), Nino(nino))

    (for {
      reliefsData       <- EitherT(getReliefs(nino, taxYear))
      chargesData       <- EitherT(getCharges(nino, taxYear))
      stateBenefitsData <- stateBenfitService.getStateBenefits(ctx)
      pensionIncomeData <- EitherT(getPensionIncome(nino, taxYear, mtditid))
      employmentData    <- employmentService.getEmployment(ctx).leftMap(err => err: ServiceErrorModel)
    } yield AllPensionsData(
      pensionReliefs = reliefsData,
      pensionCharges = chargesData,
      stateBenefits = stateBenefitsData,
      employmentPensions = employmentData.some,
      pensionIncome = pensionIncomeData
    )).value
  }

  private def createOrDeleteReliefsWhenEmpty(ctx: JourneyContextWithNino,
                                             updatedReliefs: PensionReliefs,
                                             existingRelief: Option[GetPensionReliefsModel])(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    if (updatedReliefs.nonEmpty)
      reliefsConnector.createOrAmendPensionReliefsT(ctx, CreateOrUpdatePensionReliefsModel(updatedReliefs))
    else if (existingRelief.exists(_.pensionReliefs.nonEmpty))
      reliefsConnector.deletePensionReliefsT(ctx.nino, ctx.taxYear)
    else
      EitherT.rightT[Future, ServiceError](())

  /** foreignPension or overseasPensionContribution can be None or defined but empty. For all of those case we treat it as not defined, as the schema
    * requires min property 1. If we have both of them empty/not defined we call DELETE, otherwise PUT
    */
  private def createOrDeleteIncomesWhenEmpty(ctx: JourneyContextWithNino,
                                             updatedIncomes: CreateUpdatePensionIncomeModel,
                                             existingIncome: Option[CreateUpdatePensionIncomeModel])(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    if (updatedIncomes.nonEmpty)
      pensionIncomeConnector.createOrAmendPensionIncomeT(ctx, updatedIncomes)
    else if (existingIncome.exists(_.nonEmpty))
      pensionIncomeConnector.deletePensionIncomeT(ctx.nino, ctx.taxYear)
    else
      EitherT.rightT[Future, ServiceError](())

  private def createOrDeleteChargesWhenEmpty(ctx: JourneyContextWithNino,
                                             updatedCharges: CreateUpdatePensionChargesRequestModel,
                                             existingCharges: CreateUpdatePensionChargesRequestModel)(implicit hc: HeaderCarrier): ApiResultT[Unit] =
    if (updatedCharges.nonEmpty)
      chargesConnector.createUpdatePensionChargesT(ctx, updatedCharges)
    else if (existingCharges.nonEmpty)
      chargesConnector.deletePensionChargesT(ctx.nino, ctx.taxYear)
    else
      EitherT.rightT[Future, ServiceError](())

  private def getReliefs(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): DownstreamOutcome[Option[GetPensionReliefsModel]] =
    reliefsConnector.getPensionReliefs(nino, taxYear)

  private def getCharges(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): DownstreamOutcome[Option[GetPensionChargesRequestModel]] =
    chargesConnector.getPensionCharges(nino, taxYear)

  private def getPensionIncome(nino: String, taxYear: Int, mtditid: String)(implicit
      hc: HeaderCarrier): DownstreamOutcome[Option[GetPensionIncomeModel]] =
    pensionIncomeConnector.getPensionIncome(nino, taxYear)(hc.withInternalId(mtditid))

  private def handleFutureError[A](apiCall: => ApiResultT[A]): Future[ApiResult[A]] = {
    val result = apiCall.value.recover { err =>
      logger.error("Handing Future caused error", err)
      Left(DownstreamError(err.getMessage))
    }

    result
  }

  /** TODO It could be done more optimal, with fewer calls to IFS. It will be done when a proper story will be created */
  def getCommonTaskList(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[TaskListModel] = {

    val allJourneys = for {
      paymentsIntoPensons              <- handleFutureError(getPaymentsIntoPensions(ctx))
      ukPensionIncome                  <- handleFutureError(getUkPensionIncome(ctx))
      statePension                     <- handleFutureError(getStatePension(ctx))
      annualAllowances                 <- handleFutureError(getAnnualAllowances(ctx))
      unauthorisedPaymentsFromPensions <- handleFutureError(getUnauthorisedPaymentsFromPensions(ctx))
      incomeFromOverseasPensions       <- handleFutureError(getIncomeFromOverseasPensions(ctx))
      paymentsIntoOverseasPensions     <- handleFutureError(getPaymentsIntoOverseasPensions(ctx))
      transfersIntoOverseasPensions    <- handleFutureError(getTransfersIntoOverseasPensions(ctx))
      shortServiceRefunds              <- handleFutureError(getShortServiceRefunds(ctx))
      persistedStatuses                <- handleFutureError(statusService.getAllStatuses(ctx.taxYear, ctx.mtditid))
    } yield AllJourneys.fromAnswersAndStatuses(
      paymentsIntoPensons,
      ukPensionIncome,
      statePension,
      annualAllowances,
      unauthorisedPaymentsFromPensions,
      incomeFromOverseasPensions,
      paymentsIntoOverseasPensions,
      transfersIntoOverseasPensions,
      shortServiceRefunds,
      persistedStatuses
    )

    val result = allJourneys.map { all =>
      val taskListModel = fromAllJourneys(all, appConfig.incomeTaxPensionsFrontendUrl, ctx.taxYear)
      taskListModel
    }

    EitherT.right(result)
  }
}
