/*
 * Copyright 2023 HM Revenue & Customs
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

package repositories

import cats.data.EitherT
import cats.implicits._
import config.AppConfig
import models.common.Journey._
import models.common._
import models.database._
import models.domain.ApiResultT
import models.error.ServiceError
import org.mongodb.scala._
import org.mongodb.scala.bson._
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Projections.exclude
import org.mongodb.scala.model._
import org.mongodb.scala.result.UpdateResult
import play.api.Logger
import play.api.libs.json.{JsValue, Json, OFormat, Reads, Writes}
import services.{EncryptionService, getPersistedAnswers}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import utils.Logging

import java.time.{Clock, Instant, ZoneOffset}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

trait JourneyAnswersRepository {
  def getPaymentsIntoPensions(ctx: JourneyContextWithNino): ApiResultT[Option[PaymentsIntoPensionsStorageAnswers]]
  def upsertPaymentsIntoPensions(ctx: JourneyContextWithNino, storageAnswers: PaymentsIntoPensionsStorageAnswers): ApiResultT[Unit]

  def getUkPensionIncome(ctx: JourneyContextWithNino): ApiResultT[Option[UkPensionIncomeStorageAnswers]]
  def upsertUkPensionIncome(ctx: JourneyContextWithNino, storageAnswers: UkPensionIncomeStorageAnswers): ApiResultT[Unit]

  def getStatePension(ctx: JourneyContextWithNino): ApiResultT[Option[IncomeFromPensionsStatePensionStorageAnswers]]
  def upsertStatePension(ctx: JourneyContextWithNino, storageAnswers: IncomeFromPensionsStatePensionStorageAnswers): ApiResultT[Unit]

  def getAnnualAllowances(ctx: JourneyContextWithNino): ApiResultT[Option[AnnualAllowancesStorageAnswers]]
  def upsertAnnualAllowances(ctx: JourneyContextWithNino, storageAnswers: AnnualAllowancesStorageAnswers): ApiResultT[Unit]

  def getUnauthorisedPayments(ctx: JourneyContextWithNino): ApiResultT[Option[UnauthorisedPaymentsStorageAnswers]]
  def upsertUnauthorisedPayments(ctx: JourneyContextWithNino, storageAnswers: UnauthorisedPaymentsStorageAnswers): ApiResultT[Unit]

  def getPaymentsIntoOverseasPensions(ctx: JourneyContextWithNino): ApiResultT[Option[PaymentsIntoOverseasPensionsStorageAnswers]]
  def upsertPaymentsIntoOverseasPensions(ctx: JourneyContextWithNino, storageAnswers: PaymentsIntoOverseasPensionsStorageAnswers): ApiResultT[Unit]

  def getIncomeFromOverseasPensions(ctx: JourneyContextWithNino): ApiResultT[Option[IncomeFromOverseasPensionsStorageAnswers]]
  def upsertIncomeFromOverseasPensions(ctx: JourneyContextWithNino, storageAnswers: IncomeFromOverseasPensionsStorageAnswers): ApiResultT[Unit]

  def getTransferIntoOverseasPensions(ctx: JourneyContextWithNino): ApiResultT[Option[TransfersIntoOverseasPensionsStorageAnswers]]
  def upsertTransferIntoOverseasPensions(ctx: JourneyContextWithNino, storageAnswers: TransfersIntoOverseasPensionsStorageAnswers): ApiResultT[Unit]

  def getShortServiceRefunds(ctx: JourneyContextWithNino): ApiResultT[Option[ShortServiceRefundsStorageAnswers]]
  def upsertShortServiceRefunds(ctx: JourneyContextWithNino, storageAnswers: ShortServiceRefundsStorageAnswers): ApiResultT[Unit]

  def getAllJourneyStatuses(taxYear: TaxYear, mtditid: Mtditid): ApiResultT[List[JourneyNameAndStatus]]
  def getJourneyStatus(ctx: JourneyContext): ApiResultT[List[JourneyNameAndStatus]]
  def setStatus(ctx: JourneyContext, status: JourneyStatus): ApiResultT[Unit]
  def testOnlyClearAllData(): ApiResultT[Unit]
}

// TODO Add test
@Singleton
class MongoJourneyAnswersRepository @Inject() (mongo: MongoComponent, clock: Clock, encryptionService: EncryptionService, appConfig: AppConfig)(
    implicit ec: ExecutionContext)
    extends PlayMongoRepository[JourneyAnswers](
      collectionName = "journey-answers",
      mongoComponent = mongo,
      domainFormat = JourneyAnswers.formats,
      replaceIndexes = true,
      indexes = Seq(
        IndexModel(
          ascending("updatedAt"),
          IndexOptions()
            .expireAfter(appConfig.mongoTTL, TimeUnit.DAYS)
            .name("UserDataTTL")
        ),
        IndexModel(
          ascending("mtditid", "taxYear", "journey"),
          IndexOptions().name("mtditid_taxYear_journey")
        ),
        IndexModel(
          ascending("mtditid", "taxYear"),
          IndexOptions().name("mtditid_taxYear")
        )
      )
    )
    with JourneyAnswersRepository
    with Logging {

  def testOnlyClearAllData(): ApiResultT[Unit] =
    EitherT.right[ServiceError](
      collection
        .deleteMany(new org.bson.Document())
        .toFuture()
        .void)

  private def filterJourney(ctx: JourneyContext): Bson = Filters.and(
    Filters.eq("mtditid", ctx.mtditid.value),
    Filters.eq("taxYear", ctx.taxYear.endYear),
    Filters.eq("journey", ctx.journey.entryName)
  )

  def getPaymentsIntoPensions(ctx: JourneyContextWithNino): ApiResultT[Option[PaymentsIntoPensionsStorageAnswers]] =
    getDecryptedAnswers[PaymentsIntoPensionsStorageAnswers, EncryptedPaymentsIntoPensionsStorageAnswers](
      ctx.toJourneyContext(Journey.PaymentsIntoPensions))

  def upsertPaymentsIntoPensions(ctx: JourneyContextWithNino, storageAnswers: PaymentsIntoPensionsStorageAnswers): ApiResultT[Unit] = {
    implicit val format: OFormat[EncryptedStorageAnswers[PaymentsIntoPensionsStorageAnswers]] =
      EncryptedStorageAnswers.writes[PaymentsIntoPensionsStorageAnswers](PaymentsIntoPensions)

    upsertEncryptedAnswers[PaymentsIntoPensionsStorageAnswers](ctx.toJourneyContext(PaymentsIntoPensions), storageAnswers)
  }

  def getUkPensionIncome(ctx: JourneyContextWithNino): ApiResultT[Option[UkPensionIncomeStorageAnswers]] =
    getDecryptedAnswers[UkPensionIncomeStorageAnswers, EncryptedUkPensionIncomeStorageAnswers](ctx.toJourneyContext(Journey.UkPensionIncome))

  def upsertUkPensionIncome(ctx: JourneyContextWithNino, storageAnswers: UkPensionIncomeStorageAnswers): ApiResultT[Unit] = {
    implicit val format: OFormat[EncryptedStorageAnswers[UkPensionIncomeStorageAnswers]] =
      EncryptedStorageAnswers.writes[UkPensionIncomeStorageAnswers](UkPensionIncome)

    upsertEncryptedAnswers(ctx.toJourneyContext(UkPensionIncome), storageAnswers)
  }

  def getStatePension(ctx: JourneyContextWithNino): ApiResultT[Option[IncomeFromPensionsStatePensionStorageAnswers]] =
    getDecryptedAnswers[IncomeFromPensionsStatePensionStorageAnswers, EncryptedIncomeFromPensionsStatePensionStorageAnswers](
      ctx.toJourneyContext(Journey.StatePension))

  def upsertStatePension(ctx: JourneyContextWithNino, storageAnswers: IncomeFromPensionsStatePensionStorageAnswers): ApiResultT[Unit] = {
    implicit val format: OFormat[EncryptedStorageAnswers[IncomeFromPensionsStatePensionStorageAnswers]] =
      EncryptedStorageAnswers.writes[IncomeFromPensionsStatePensionStorageAnswers](StatePension)

    upsertEncryptedAnswers(ctx.toJourneyContext(StatePension), storageAnswers)
  }

  def getAnnualAllowances(ctx: JourneyContextWithNino): ApiResultT[Option[AnnualAllowancesStorageAnswers]] = {
    implicit val format: OFormat[EncryptedStorageAnswers[PaymentsIntoPensionsStorageAnswers]] =
      EncryptedStorageAnswers.writes[PaymentsIntoPensionsStorageAnswers](PaymentsIntoPensions)

    getDecryptedAnswers[AnnualAllowancesStorageAnswers, EncryptedAnnualAllowancesStorageAnswers](ctx.toJourneyContext(Journey.AnnualAllowances))
  }

  def upsertAnnualAllowances(ctx: JourneyContextWithNino, storageAnswers: AnnualAllowancesStorageAnswers): ApiResultT[Unit] = {
    implicit val format: OFormat[EncryptedStorageAnswers[AnnualAllowancesStorageAnswers]] =
      EncryptedStorageAnswers.writes[AnnualAllowancesStorageAnswers](AnnualAllowances)

    upsertEncryptedAnswers(ctx.toJourneyContext(AnnualAllowances), storageAnswers)
  }

  def getUnauthorisedPayments(ctx: JourneyContextWithNino): ApiResultT[Option[UnauthorisedPaymentsStorageAnswers]] =
    getDecryptedAnswers[UnauthorisedPaymentsStorageAnswers, EncryptedUnauthorisedPaymentsStorageAnswers](
      ctx.toJourneyContext(Journey.UnauthorisedPayments))

  def upsertUnauthorisedPayments(ctx: JourneyContextWithNino, storageAnswers: UnauthorisedPaymentsStorageAnswers): ApiResultT[Unit] = {
    implicit val format: OFormat[EncryptedStorageAnswers[UnauthorisedPaymentsStorageAnswers]] =
      EncryptedStorageAnswers.writes[UnauthorisedPaymentsStorageAnswers](UnauthorisedPayments)

    upsertEncryptedAnswers(ctx.toJourneyContext(UnauthorisedPayments), storageAnswers)
  }

  def getPaymentsIntoOverseasPensions(ctx: JourneyContextWithNino): ApiResultT[Option[PaymentsIntoOverseasPensionsStorageAnswers]] =
    getDecryptedAnswers[PaymentsIntoOverseasPensionsStorageAnswers, EncryptedPaymentsIntoOverseasPensionsStorageAnswers](
      ctx.toJourneyContext(Journey.PaymentsIntoOverseasPensions))

  def upsertPaymentsIntoOverseasPensions(ctx: JourneyContextWithNino,
                                         storageAnswers: PaymentsIntoOverseasPensionsStorageAnswers): ApiResultT[Unit] = {
    implicit val format: OFormat[EncryptedStorageAnswers[PaymentsIntoOverseasPensionsStorageAnswers]] =
      EncryptedStorageAnswers.writes[PaymentsIntoOverseasPensionsStorageAnswers](PaymentsIntoOverseasPensions)

    upsertEncryptedAnswers(ctx.toJourneyContext(PaymentsIntoOverseasPensions), storageAnswers)
  }

  def getIncomeFromOverseasPensions(ctx: JourneyContextWithNino): ApiResultT[Option[IncomeFromOverseasPensionsStorageAnswers]] =
    getDecryptedAnswers[IncomeFromOverseasPensionsStorageAnswers, EncryptedIncomeFromOverseasPensionsStorageAnswers](
      ctx.toJourneyContext(Journey.IncomeFromOverseasPensions))

  def upsertIncomeFromOverseasPensions(ctx: JourneyContextWithNino, storageAnswers: IncomeFromOverseasPensionsStorageAnswers): ApiResultT[Unit] = {
    implicit val format: OFormat[EncryptedStorageAnswers[IncomeFromOverseasPensionsStorageAnswers]] =
      EncryptedStorageAnswers.writes[IncomeFromOverseasPensionsStorageAnswers](IncomeFromOverseasPensions)

    upsertEncryptedAnswers(ctx.toJourneyContext(IncomeFromOverseasPensions), storageAnswers)
  }

  def getTransferIntoOverseasPensions(ctx: JourneyContextWithNino): ApiResultT[Option[TransfersIntoOverseasPensionsStorageAnswers]] =
    getDecryptedAnswers[TransfersIntoOverseasPensionsStorageAnswers, EncryptedTransfersIntoOverseasPensionsStorageAnswers](
      ctx.toJourneyContext(Journey.TransferIntoOverseasPensions))

  def upsertTransferIntoOverseasPensions(ctx: JourneyContextWithNino,
                                         storageAnswers: TransfersIntoOverseasPensionsStorageAnswers): ApiResultT[Unit] = {
    implicit val format: OFormat[EncryptedStorageAnswers[TransfersIntoOverseasPensionsStorageAnswers]] =
      EncryptedStorageAnswers.writes[TransfersIntoOverseasPensionsStorageAnswers](TransferIntoOverseasPensions)

    upsertEncryptedAnswers(ctx.toJourneyContext(TransferIntoOverseasPensions), storageAnswers)
  }

  def getShortServiceRefunds(ctx: JourneyContextWithNino): ApiResultT[Option[ShortServiceRefundsStorageAnswers]] =
    getDecryptedAnswers[ShortServiceRefundsStorageAnswers, EncryptedShortServiceRefundsStorageAnswers](
      ctx.toJourneyContext(Journey.ShortServiceRefunds))

  def upsertShortServiceRefunds(ctx: JourneyContextWithNino, storageAnswers: ShortServiceRefundsStorageAnswers): ApiResultT[Unit] = {
    implicit val format: OFormat[EncryptedStorageAnswers[ShortServiceRefundsStorageAnswers]] =
      EncryptedStorageAnswers.writes[ShortServiceRefundsStorageAnswers](ShortServiceRefunds)

    upsertEncryptedAnswers(ctx.toJourneyContext(ShortServiceRefunds), storageAnswers)
  }

  private def upsertEncryptedAnswers[A](ctx: JourneyContext, storageAnswers: StorageAnswers[A])(implicit
      format: OFormat[EncryptedStorageAnswers[A]]): ApiResultT[Unit] =
    for {
      encryptedAnswers <- EitherT.fromEither[Future](encryptionService.encryptUserData[A](ctx.mtditid, storageAnswers))
      _                <- upsertAnswers(ctx, Json.toJson(encryptedAnswers))
    } yield ()

  private[repositories] def upsertAnswers(ctx: JourneyContext, newData: JsValue): ApiResultT[Unit] = {
    logger.info(s"Repository ctx=${ctx.toString} persisting answers:\n===Repository===\n${Json.prettyPrint(newData)}\n===")

    val filter  = filterJourney(ctx)
    val bson    = BsonDocument(Json.stringify(newData))
    val update  = createUpsert(ctx)("data", bson, JourneyStatus.InProgress)
    val options = new UpdateOptions().upsert(true)

    handleUpdateExactlyOne(ctx, collection.updateOne(filter, update, options).toFuture())
  }

  def getAllJourneyStatuses(taxYear: TaxYear, mtditid: Mtditid): ApiResultT[List[JourneyNameAndStatus]] = {
    val filter     = filterAllJourneys(taxYear, mtditid)
    val projection = exclude("data")
    EitherT.right[ServiceError](
      collection
        .find(filter)
        .projection(projection)
        .toFuture()
        .map(_.toList.map(a => JourneyNameAndStatus(a.journey, a.status))))
  }

  def getJourneyStatus(ctx: JourneyContext): ApiResultT[List[JourneyNameAndStatus]] = {
    val filter     = filterJourney(ctx)
    val projection = exclude("data")

    EitherT.right[ServiceError](
      collection
        .find(filter)
        .projection(projection)
        .toFuture()
        .map(_.toList.map(a => JourneyNameAndStatus(a.journey, a.status))))
  }

  private def filterAllJourneys(taxYear: TaxYear, mtditid: Mtditid): Bson = Filters.and(
    Filters.eq("mtditid", mtditid.value),
    Filters.eq("taxYear", taxYear.endYear)
  )

  def setStatus(ctx: JourneyContext, status: JourneyStatus): ApiResultT[Unit] = {
    logger.info(s"Repository: ctx=${ctx.toString} persisting new status=$status")

    handleUpdateExactlyOne(ctx, upsertStatus(ctx, status))
  }

  private[repositories] def upsertStatus(ctx: JourneyContext, status: JourneyStatus): Future[UpdateResult] = {
    val filter  = filterJourney(ctx)
    val update  = createUpsertStatus(ctx)(status)
    val options = new UpdateOptions().upsert(true)

    collection.updateOne(filter, update, options).toFuture()
  }

  private def createUpsert(ctx: JourneyContext)(fieldName: String, value: BsonValue, statusOnInsert: JourneyStatus): Bson = {
    val now      = Instant.now(clock)
    val expireAt = now.atZone(ZoneOffset.UTC).plusDays(appConfig.mongoTTL).toInstant

    Updates.combine(
      Updates.set(fieldName, value),
      Updates.set("updatedAt", now),
      Updates.setOnInsert("mtditid", ctx.mtditid.value),
      Updates.setOnInsert("taxYear", ctx.taxYear.endYear),
      Updates.setOnInsert("status", statusOnInsert.entryName),
      Updates.setOnInsert("journey", ctx.journey.entryName),
      Updates.setOnInsert("createdAt", now),
      Updates.setOnInsert("expireAt", expireAt)
    )
  }

  private def createUpsertStatus(ctx: JourneyContext)(status: JourneyStatus): Bson = {
    val now      = Instant.now(clock)
    val expireAt = now.atZone(ZoneOffset.UTC).plusDays(appConfig.mongoTTL).toInstant

    Updates.combine(
      Updates.set("status", status.entryName),
      Updates.set("updatedAt", now),
      Updates.setOnInsert("data", BsonDocument()),
      Updates.setOnInsert("mtditid", ctx.mtditid.value),
      Updates.setOnInsert("taxYear", ctx.taxYear.endYear),
      Updates.setOnInsert("journey", ctx.journey.entryName),
      Updates.setOnInsert("createdAt", now),
      Updates.setOnInsert("expireAt", expireAt)
    )
  }

  private def handleUpdateExactlyOne(ctx: JourneyContext, result: Future[UpdateResult])(implicit
      logger: Logger,
      ec: ExecutionContext): ApiResultT[Unit] = {
    def insertedSuccessfully(result: UpdateResult): Boolean =
      result.getModifiedCount == 0 && result.getMatchedCount == 0 && Option(result.getUpsertedId).nonEmpty
    def updatedSuccessfully(result: UpdateResult) = result.getModifiedCount == 1

    val futResult: Future[Either[ServiceError, UpdateResult]] = result.map { r =>
      val notInsertedOne = !insertedSuccessfully(r)
      val notUpdatedOne  = !updatedSuccessfully(r)

      if (notInsertedOne && notUpdatedOne) {
        logger.warn(
          s"Upsert invalid state (this should never happened): getModifiedCount=${r.getModifiedCount}, getMatchedCount=${r.getMatchedCount}, " +
            s"getUpsertedId=${r.getUpsertedId}, notInsertedOne=$notInsertedOne, notUpdatedOne=$notUpdatedOne, for ctx=${ctx.toString}"
        ) // TODO Add Pager Duty
      }
      Right(r)
    }

    EitherT(futResult).void
  }

  private[repositories] def get(ctx: JourneyContext): ApiResultT[Option[JourneyAnswers]] = {
    val filter = filterJourney(ctx)
    EitherT.right[ServiceError](
      collection
        .withReadPreference(ReadPreference.primaryPreferred()) // TODO Why? Cannot we just use standard?
        .find(filter)
        .headOption())
  }

  private def getAnswers[A <: EncryptedStorageAnswers[_]: Reads](ctx: JourneyContext)(implicit ct: ClassTag[A]): ApiResultT[Option[A]] =
    for {
      maybeDbAnswers        <- get(ctx)
      maybeJourneyDbAnswers <- getPersistedAnswers[A](maybeDbAnswers)
    } yield maybeJourneyDbAnswers

  private def getDecryptedAnswers[DecAns, EncAns <: EncryptedStorageAnswers[DecAns]: Reads](ctx: JourneyContext)(implicit
      ct: ClassTag[EncAns]): ApiResultT[Option[DecAns]] = {
    val textAndKeyAes: TextAndKey = TextAndKey(ctx.mtditid.value, appConfig.encryptionKey)

    for {
      maybeEncryptedDbAnswers <- getAnswers(ctx)
      maybeDbAnswers          <- maybeEncryptedDbAnswers.traverse(_.decryptedT(encryptionService, textAndKeyAes))
    } yield maybeDbAnswers
  }
}
