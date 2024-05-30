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
import connectors._
import models._
import models.charges.{CreateUpdatePensionChargesRequestModel, GetPensionChargesRequestModel}
import models.common.{Journey, JourneyContextWithNino}
import models.database._
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend._
import models.submission.EmploymentPensions
import play.api.libs.json.Json
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.HeaderCarrierUtils.HeaderCarrierOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait PensionsService {
  def getPaymentsIntoPensions(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[PaymentsIntoPensionsAnswers]]
  def upsertPaymentsIntoPensions(ctx: JourneyContextWithNino, answers: PaymentsIntoPensionsAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
  def getUkPensionIncome(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[UkPensionIncomeAnswers]]
  def upsertUkPensionIncome(ctx: JourneyContextWithNino, answers: UkPensionIncomeAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit]
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
  def getAllPensionsData(nino: String, taxYear: Int, mtditid: String)(implicit hc: HeaderCarrier): Future[Either[ServiceErrorModel, AllPensionsData]]
}

class PensionsServiceImpl @Inject() (reliefsConnector: PensionReliefsConnector,
                                     chargesConnector: PensionChargesConnector,
                                     stateBenefitsConnector: GetStateBenefitsConnector,
                                     pensionIncomeConnector: PensionIncomeConnector,
                                     employmentService: EmploymentService,
                                     repository: JourneyAnswersRepository)(implicit ec: ExecutionContext)
    extends PensionsService {

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

  def getPaymentsIntoPensions(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[PaymentsIntoPensionsAnswers]] =
    for {
      maybeReliefs   <- reliefsConnector.getPensionReliefsT(ctx.nino, ctx.taxYear)
      maybeDbAnswers <- repository.getAnswers[PaymentsIntoPensionsStorageAnswers](ctx.toJourneyContext(Journey.PaymentsIntoPensions))
      paymentsIntoPensionsAnswers = maybeReliefs
        .getOrElse(GetPensionReliefsModel("", None, PensionReliefs.empty))
        .toPaymentsIntoPensions(maybeDbAnswers)
    } yield paymentsIntoPensionsAnswers

  def upsertPaymentsIntoPensions(ctx: JourneyContextWithNino, answers: PaymentsIntoPensionsAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val storageAnswers = PaymentsIntoPensionsStorageAnswers.fromJourneyAnswers(answers)
    val journeyCtx     = ctx.toJourneyContext(Journey.PaymentsIntoPensions)

    for {
      existingRelief <- reliefsConnector.getPensionReliefsT(ctx.nino, ctx.taxYear)
      maybeOverseasPensionSchemeContributions = existingRelief.flatMap(_.pensionReliefs.overseasPensionSchemeContributions)
      updatedReliefs: PensionReliefs          = answers.toPensionReliefs(maybeOverseasPensionSchemeContributions)
      _ <- createOrDeleteReliefsWhenEmpty(ctx, updatedReliefs, existingRelief)
      _ <- repository.upsertAnswers(journeyCtx, Json.toJson(storageAnswers))
    } yield ()
  }

  def getUkPensionIncome(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[UkPensionIncomeAnswers]] =
    for {
      employment     <- employmentService.getEmployment(ctx)
      maybeDbAnswers <- repository.getAnswers[UkPensionIncomeStorageAnswers](ctx.toJourneyContext(Journey.UkPensionIncome))
    } yield employment.toUkPensionIncomeAnswers(maybeDbAnswers)

  def upsertUkPensionIncome(ctx: JourneyContextWithNino, answers: UkPensionIncomeAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val storageAnswers = UkPensionIncomeStorageAnswers.fromJourneyAnswers(answers)
    val journeyCtx     = ctx.toJourneyContext(Journey.UkPensionIncome)

    for {
      _ <- employmentService.upsertUkPensionIncome(ctx, answers)
      _ <- repository.upsertAnswers(journeyCtx, Json.toJson(storageAnswers))
    } yield ()
  }

  def getAnnualAllowances(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[AnnualAllowancesAnswers]] =
    for {
      maybeCharges   <- chargesConnector.getPensionChargesT(ctx.nino, ctx.taxYear)
      maybeDbAnswers <- repository.getAnswers[AnnualAllowancesStorageAnswers](ctx.toJourneyContext(Journey.AnnualAllowances))
      annualAllowancesAnswers = maybeCharges.flatMap(_.pensionContributions.flatMap(_.toAnnualAllowances(maybeDbAnswers)))
    } yield annualAllowancesAnswers

  def upsertAnnualAllowances(ctx: JourneyContextWithNino, answers: AnnualAllowancesAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val storageAnswers = AnnualAllowancesStorageAnswers.fromJourneyAnswers(answers)
    val journeyCtx     = ctx.toJourneyContext(Journey.AnnualAllowances)

    for {
      getCharges <- chargesConnector.getPensionChargesT(ctx.nino, ctx.taxYear)
      existingCharges      = getCharges.map(_.toCreateUpdatePensionChargesRequestModel).getOrElse(CreateUpdatePensionChargesRequestModel.empty)
      updatedContributions = answers.toPensionChargesContributions.some
      updatedCharges       = existingCharges.copy(pensionContributions = updatedContributions)
      _ <- chargesConnector.createUpdatePensionChargesT(ctx, updatedCharges)
      _ <- repository.upsertAnswers(journeyCtx, Json.toJson(storageAnswers))
    } yield ()
  }

  def getUnauthorisedPaymentsFromPensions(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[UnauthorisedPaymentsAnswers]] =
    for {
      maybeCharges   <- chargesConnector.getPensionChargesT(ctx.nino, ctx.taxYear)
      maybeDbAnswers <- repository.getAnswers[UnauthorisedPaymentsStorageAnswers](ctx.toJourneyContext(Journey.UnauthorisedPayments))
      annualAllowancesAnswers = maybeCharges.flatMap(_.pensionSchemeUnauthorisedPayments.map(_.toUnauthorisedPayments(maybeDbAnswers)))
    } yield annualAllowancesAnswers

  def upsertUnauthorisedPaymentsFromPensions(ctx: JourneyContextWithNino, answers: UnauthorisedPaymentsAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val storageAnswers = UnauthorisedPaymentsStorageAnswers.fromJourneyAnswers(answers)
    val journeyCtx     = ctx.toJourneyContext(Journey.UnauthorisedPayments)

    for {
      getCharges <- chargesConnector.getPensionChargesT(ctx.nino, ctx.taxYear)
      existingCharges = getCharges.map(_.toCreateUpdatePensionChargesRequestModel).getOrElse(CreateUpdatePensionChargesRequestModel.empty)
      pensionSchemeUnauthorisedPayments = answers.toPensionCharges.some
      updatedCharges                    = existingCharges.copy(pensionSchemeUnauthorisedPayments = pensionSchemeUnauthorisedPayments)
      _ <- chargesConnector.createUpdatePensionChargesT(ctx, updatedCharges)
      _ <- repository.upsertAnswers(journeyCtx, Json.toJson(storageAnswers))
    } yield ()
  }

  def getIncomeFromOverseasPensions(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[IncomeFromOverseasPensionsAnswers]] =
    for {
      maybeIncome    <- pensionIncomeConnector.getPensionIncomeT(ctx.nino, ctx.taxYear)
      maybeDbAnswers <- repository.getAnswers[IncomeFromOverseasPensionsStorageAnswers](ctx.toJourneyContext(Journey.IncomeFromOverseasPensions))
      incomeFromOverseasPensions = maybeIncome.getOrElse(GetPensionIncomeModel.empty).toIncomeFromOverseasPensions(maybeDbAnswers)
    } yield incomeFromOverseasPensions

  def upsertIncomeFromOverseasPensions(ctx: JourneyContextWithNino, answers: IncomeFromOverseasPensionsAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val storageAnswers = IncomeFromOverseasPensionsStorageAnswers.fromJourneyAnswers(answers)
    val journeyCtx     = ctx.toJourneyContext(Journey.IncomeFromOverseasPensions)

    for {
      incomeResponse <- pensionIncomeConnector.getPensionIncomeT(ctx.nino, ctx.taxYear)
      existingIncome                    = incomeResponse.map(_.toCreateUpdatePensionIncomeModel)
      updatedIncomeFromOverseasPensions = answers.toForeignPension.map(_.toList)
      updatedIncome = existingIncome.getOrElse(CreateUpdatePensionIncomeModel.empty).copy(foreignPension = updatedIncomeFromOverseasPensions)
      _ <- createOrDeleteIncomesWhenEmpty(ctx, updatedIncome, existingIncome)
      _ <- repository.upsertAnswers(journeyCtx, Json.toJson(storageAnswers))
    } yield ()
  }

  def getPaymentsIntoOverseasPensions(ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Option[PaymentsIntoOverseasPensionsAnswers]] =
    for {
      maybeReliefs   <- reliefsConnector.getPensionReliefsT(ctx.nino, ctx.taxYear)
      maybeIncomes   <- pensionIncomeConnector.getPensionIncomeT(ctx.nino, ctx.taxYear)
      maybeDbAnswers <- repository.getAnswers[PaymentsIntoOverseasPensionsStorageAnswers](ctx.toJourneyContext(Journey.PaymentsIntoOverseasPensions))
      paymentsIntoOverseasPensionsAnswers: Option[PaymentsIntoOverseasPensionsAnswers] = maybeDbAnswers.flatMap(
        _.toPaymentsIntoOverseasPensionsAnswers(maybeIncomes, maybeReliefs))
    } yield paymentsIntoOverseasPensionsAnswers

  def upsertPaymentsIntoOverseasPensions(ctx: JourneyContextWithNino, answers: PaymentsIntoOverseasPensionsAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val storageAnswers = answers.toStorageAnswers
    val journeyCtx     = ctx.toJourneyContext(Journey.PaymentsIntoOverseasPensions)

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
      _ <- repository.upsertAnswers(journeyCtx, Json.toJson(storageAnswers))
    } yield ()
  }

  def getTransfersIntoOverseasPensions(ctx: JourneyContextWithNino)(implicit
      hc: HeaderCarrier): ApiResultT[Option[TransfersIntoOverseasPensionsAnswers]] =
    for {
      maybeCharges   <- chargesConnector.getPensionChargesT(ctx.nino, ctx.taxYear)
      maybeDbAnswers <- repository.getAnswers[TransfersIntoOverseasPensionsStorageAnswers](ctx.toJourneyContext(Journey.TransferIntoOverseasPensions))
      transfersIntoOverseasPensionsAnswers = maybeDbAnswers.flatMap(_.toTransfersIntoOverseasPensions(maybeCharges))
    } yield transfersIntoOverseasPensionsAnswers

  def upsertTransfersIntoOverseasPensions(ctx: JourneyContextWithNino, answers: TransfersIntoOverseasPensionsAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    val storageAnswers = TransfersIntoOverseasPensionsStorageAnswers.fromJourneyAnswers(answers)
    val journeyCtx     = ctx.toJourneyContext(Journey.TransferIntoOverseasPensions)

    for {
      getCharges <- chargesConnector.getPensionChargesT(ctx.nino, ctx.taxYear)
      existingCharges      = getCharges.map(_.toCreateUpdatePensionChargesRequestModel).getOrElse(CreateUpdatePensionChargesRequestModel.empty)
      updatedContributions = answers.toPensionSchemeOverseasTransfers
      updatedCharges       = existingCharges.copy(pensionSchemeOverseasTransfers = updatedContributions)
      _ <- createOrDeleteChargesWhenEmpty(ctx, updatedCharges, existingCharges)
      _ <- repository.upsertAnswers(journeyCtx, Json.toJson(storageAnswers))
    } yield ()
  }

  def getShortServiceRefunds(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[ShortServiceRefundsAnswers]] =
    for {
      maybeCharges   <- chargesConnector.getPensionChargesT(ctx.nino, ctx.taxYear)
      maybeDbAnswers <- repository.getAnswers[ShortServiceRefundsStorageAnswers](ctx.toJourneyContext(Journey.ShortServiceRefunds))
      shortServiceRefundsAnswers = maybeCharges.flatMap(
        _.overseasPensionContributions.flatMap(
          _.toShortServiceRefundsAnswers(maybeDbAnswers)
        ))
    } yield shortServiceRefundsAnswers

  def upsertShortServiceRefunds(ctx: JourneyContextWithNino, answers: ShortServiceRefundsAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    val storageAnswers = ShortServiceRefundsStorageAnswers.fromJourneyAnswers(answers)
    val journeyCtx     = ctx.toJourneyContext(Journey.ShortServiceRefunds)

    for {
      getCharges <- chargesConnector.getPensionChargesT(ctx.nino, ctx.taxYear)
      existingCharges = getCharges.map(_.toCreateUpdatePensionChargesRequestModel).getOrElse(CreateUpdatePensionChargesRequestModel.empty)
      overseasPensionContributions = answers.toOverseasPensions.some
      updatedCharges               = existingCharges.copy(overseasPensionContributions = overseasPensionContributions)
      _ <- createOrDeleteChargesWhenEmpty(ctx, updatedCharges, existingCharges)
      _ <- repository.upsertAnswers(journeyCtx, Json.toJson(storageAnswers))
    } yield ()
  }

  // TODO: Decide whether loading employments and state benefits through pensions is what we want. The submissions service
  //       (aka "the cache") already loads employments and state benefits so adding the calls to load through pensions
  //       duplicates the data in the cache.
  def getAllPensionsData(nino: String, taxYear: Int, mtditid: String)(implicit
      hc: HeaderCarrier): Future[Either[ServiceErrorModel, AllPensionsData]] =
    (for {
      reliefsData       <- EitherT(getReliefs(nino, taxYear))
      chargesData       <- EitherT(getCharges(nino, taxYear))
      stateBenefitsData <- EitherT(getStateBenefits(nino, taxYear, mtditid))
      pensionIncomeData <- EitherT(getPensionIncome(nino, taxYear, mtditid))
//      employmentData    <- EitherT(getEmployments(nino, taxYear, mtditid))
      employmentData = None // TODO It's broken for 2025, fix in https://jira.tools.tax.service.gov.uk/browse/SASS-8136
    } yield AllPensionsData(
      pensionReliefs = reliefsData,
      pensionCharges = chargesData,
      stateBenefits = stateBenefitsData,
      employmentPensions = employmentData.fold(none[EmploymentPensions])(EmploymentPensions.fromEmploymentResponse(_).some),
      pensionIncome = pensionIncomeData
    )).value

  private def getReliefs(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): DownstreamOutcome[Option[GetPensionReliefsModel]] =
    reliefsConnector.getPensionReliefs(nino, taxYear)

  private def getCharges(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): DownstreamOutcome[Option[GetPensionChargesRequestModel]] =
    chargesConnector.getPensionCharges(nino, taxYear)

  private def getStateBenefits(nino: String, taxYear: Int, mtditid: String)(implicit
      hc: HeaderCarrier): DownstreamOutcome[Option[AllStateBenefitsData]] =
    stateBenefitsConnector.getStateBenefits(nino, taxYear)(hc.withInternalId(mtditid))

  private def getPensionIncome(nino: String, taxYear: Int, mtditid: String)(implicit
      hc: HeaderCarrier): DownstreamOutcome[Option[GetPensionIncomeModel]] =
    pensionIncomeConnector.getPensionIncome(nino, taxYear)(hc.withInternalId(mtditid))

}
