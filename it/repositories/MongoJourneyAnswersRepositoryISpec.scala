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
import models.common.Journey.PaymentsIntoPensions
import models.common.JourneyStatus._
import models.common.{Journey, JourneyContext}
import models.database.JourneyAnswers
import models.error.ServiceError
import org.scalatest.EitherValues._
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import support.MongoTestSupport
import utils.EitherTTestOps._
import utils.TestUtils._

import java.time.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class MongoJourneyAnswersRepositoryISpec extends MongoSpec with MongoTestSupport[JourneyAnswers] {
  private val now   = mkNow()
  private val clock = mkClock(now)

  override val repository = new MongoJourneyAnswersRepository(mongoComponent, clock)

  override def beforeEach(): Unit = {
    clock.reset(now)
    await(removeAll(repository.collection))
  }

  "setStatus" should {
    "set a journey status" in {
      val result = (for {
        _      <- repository.setStatus(paymentsIntoPensionsCtx, InProgress)
        answer <- repository.get(paymentsIntoPensionsCtx)
      } yield answer).rightValue

      result.value shouldBe JourneyAnswers(
        paymentsIntoPensionsCtx.mtditid,
        paymentsIntoPensionsCtx.taxYear,
        PaymentsIntoPensions,
        InProgress,
        JsObject.empty,
        result.value.expireAt,
        result.value.createdAt,
        result.value.updatedAt
      )
    }
  }

  "upsertData + get" should {
    "insert a new journey answers in in-progress status and calculate dates" in {
      val result = (for {
        _        <- repository.upsertAnswers(paymentsIntoPensionsCtx, Json.obj("field" -> "value"))
        inserted <- repository.get(paymentsIntoPensionsCtx)
      } yield inserted.value).rightValue

      val expectedExpireAt = ExpireAtCalculator.calculateExpireAt(now)
      result shouldBe JourneyAnswers(
        mtditid,
        currTaxYear,
        Journey.PaymentsIntoPensions,
        NotStarted,
        Json.obj("field" -> "value"),
        expectedExpireAt,
        now,
        now)
    }

    "update already existing answers (values, updateAt)" in {
      val result = (for {
        _ <- repository.upsertAnswers(paymentsIntoPensionsCtx, Json.obj("field" -> "value"))
        _ = clock.advanceBy(1.day)
        _       <- repository.upsertAnswers(paymentsIntoPensionsCtx, Json.obj("field" -> "updated"))
        updated <- repository.get(paymentsIntoPensionsCtx)
      } yield updated.value).rightValue

      val expectedExpireAt = ExpireAtCalculator.calculateExpireAt(now)
      result shouldBe JourneyAnswers(
        mtditid,
        currTaxYear,
        Journey.PaymentsIntoPensions,
        NotStarted,
        Json.obj("field" -> "updated"),
        expectedExpireAt,
        now,
        now.plus(Duration.ofDays(1))
      )
    }
  }

  "updateStatus + get" should {
    "update status to a new one" in {
      val result = (for {
        _ <- repository.upsertAnswers(paymentsIntoPensionsCtx, Json.obj("field" -> "value"))
        _ = clock.advanceBy(2.day)
        _        <- repository.setStatus(paymentsIntoPensionsCtx, Completed)
        inserted <- repository.get(paymentsIntoPensionsCtx)
      } yield inserted.value).rightValue

      result.status shouldBe Completed
      result.updatedAt shouldBe now.plus(Duration.ofDays(2))
    }
  }

  "testOnlyClearAllData" should {
    "clear all the data" in {
      val res = (for {
        _        <- repository.upsertAnswers(paymentsIntoPensionsCtx, Json.obj("field" -> "value"))
        _        <- repository.testOnlyClearAllData()
        inserted <- repository.get(paymentsIntoPensionsCtx)
      } yield inserted).rightValue

      res shouldBe None
    }
  }

  "upsertStatus" should {
    "return correct UpdateResult for insert and update" in {
      val ctx = JourneyContext(currTaxYear, mtditid, Journey.PaymentsIntoPensions)
      val result = (for {
        beginning     <- repository.get(ctx)
        createdResult <- EitherT.right[ServiceError](repository.upsertStatus(ctx, InProgress))
        created       <- repository.get(ctx)
        updatedResult <- EitherT.right[ServiceError](repository.upsertStatus(ctx, Completed))
        updated       <- repository.get(ctx)
      } yield (beginning, createdResult, created, updatedResult, updated)).value

      val (beginning, createdResult, created, updatedResult, updated) = result.futureValue.value
      assert(beginning === None)

      assert(createdResult.getModifiedCount == 0)
      assert(createdResult.getMatchedCount == 0)
      assert(Option(createdResult.getUpsertedId) !== None)
      assert(created.value.status === InProgress)

      assert(updatedResult.getModifiedCount == 1)
      assert(updatedResult.getMatchedCount == 1)
      assert(Option(updatedResult.getUpsertedId) === None)
      assert(updated.value.status === Completed)
    }

  }
}
