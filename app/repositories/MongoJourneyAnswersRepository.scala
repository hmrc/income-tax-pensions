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
import models.common._
import models.database.JourneyAnswers
import models.domain.ApiResultT
import models.error.ServiceError
import org.mongodb.scala._
import org.mongodb.scala.bson._
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Projections.exclude
import org.mongodb.scala.model._
import org.mongodb.scala.result.UpdateResult
import play.api.Logger
import play.api.libs.json.{JsValue, Json, Reads}
import services.journeyAnswers.getPersistedAnswers
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import utils.Logging

import java.time.{Clock, Instant, ZoneOffset}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

trait JourneyAnswersRepository {
  def get(ctx: JourneyContext): ApiResultT[Option[JourneyAnswers]]
  def getAnswers[A: Reads](ctx: JourneyContext)(implicit ct: ClassTag[A]): ApiResultT[Option[A]]
  def upsertAnswers(ctx: JourneyContext, newData: JsValue): ApiResultT[Unit]
  def getAllJourneyStatuses(taxYear: TaxYear, mtditid: Mtditid): ApiResultT[List[JourneyNameAndStatus]]
  def getJourneyStatus(ctx: JourneyContext): ApiResultT[List[JourneyNameAndStatus]]
  def setStatus(ctx: JourneyContext, status: JourneyStatus): ApiResultT[Unit]
  def testOnlyClearAllData(): ApiResultT[Unit]
}

@Singleton
class MongoJourneyAnswersRepository @Inject() (mongo: MongoComponent, clock: Clock, appConfig: AppConfig)(implicit ec: ExecutionContext)
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

  private def filterJourney(ctx: JourneyContext) = Filters.and(
    Filters.eq("mtditid", ctx.mtditid.value),
    Filters.eq("taxYear", ctx.taxYear.endYear),
    Filters.eq("journey", ctx.journey.entryName)
  )

  def get(ctx: JourneyContext): ApiResultT[Option[JourneyAnswers]] = {
    val filter = filterJourney(ctx)
    EitherT.right[ServiceError](
      collection
        .withReadPreference(ReadPreference.primaryPreferred()) // TODO Why? Cannot we just use standard?
        .find(filter)
        .headOption())
  }

  def getAnswers[A: Reads](ctx: JourneyContext)(implicit ct: ClassTag[A]): ApiResultT[Option[A]] =
    for {
      maybeDbAnswers        <- get(ctx)
      maybeJourneyDbAnswers <- getPersistedAnswers[A](maybeDbAnswers)
    } yield maybeJourneyDbAnswers

  def upsertAnswers(ctx: JourneyContext, newData: JsValue): ApiResultT[Unit] = {
    logger.info(s"Repository ctx=${ctx.toString} persisting answers:\n===Repository===\n${Json.prettyPrint(newData)}\n===")

    val filter  = filterJourney(ctx)
    val bson    = BsonDocument(Json.stringify(newData))
    val update  = createUpsert(ctx)("data", bson, JourneyStatus.NotStarted)
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

  private def filterAllJourneys(taxYear: TaxYear, mtditid: Mtditid) = Filters.and(
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

  private def createUpsert(ctx: JourneyContext)(fieldName: String, value: BsonValue, statusOnInsert: JourneyStatus) = {
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

  private def createUpsertStatus(ctx: JourneyContext)(status: JourneyStatus) = {
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
    def insertedSuccessfully(result: UpdateResult) =
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
}
