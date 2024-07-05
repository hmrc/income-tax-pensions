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
import models.common.JourneyStatus._
import models.common.{Journey, JourneyContext, JourneyNameAndStatus}
import models.database._
import models.database.JourneyAnswers
import models.database.{JourneyAnswers, PaymentsIntoPensionsStorageAnswers}
import models.error.ServiceError
import org.mockito.Mockito.when
import org.scalatest.EitherValues._
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.TableFor1
import org.scalatest.prop.Tables.Table
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import stubs.services.StubEncryptionService
import support.MongoTestSupport
import testdata._
import utils.EitherTTestOps._
import utils.TestUtils._

import java.time.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class MongoJourneyAnswersRepositoryISpec extends MongoSpec with MongoTestSupport[JourneyAnswers] {
  private val now       = mkNow()
  private val clock     = mkClock(now)
  private val sampleCtx = JourneyContext(currTaxYear, mtditid, Journey.PaymentsIntoPensions)

  private val mockAppConfig = mock[AppConfig]
  when(mockAppConfig.mongoTTL) thenReturn 28
  private val TTLinSeconds = mockAppConfig.mongoTTL * 3600 * 24

  override val repository = new MongoJourneyAnswersRepository(mongoComponent, clock, mockAppConfig)

  override def beforeEach(): Unit = {
    clock.reset(now)
    await(repository.testOnlyClearAllData().value)
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

      val expectedExpireAt = now.plusSeconds(TTLinSeconds)
      result shouldBe JourneyAnswers(
        mtditid,
        currTaxYear,
        Journey.PaymentsIntoPensions,
        InProgress,
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

      val expectedExpireAt = now.plusSeconds(TTLinSeconds)
      result shouldBe JourneyAnswers(
        mtditid,
        currTaxYear,
        Journey.PaymentsIntoPensions,
        InProgress,
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

  "getAllJourneyStatuses" should {
    "return the completed status of any journey answers" in {
      val result = (for {
        _        <- repository.upsertAnswers(paymentsIntoPensionsCtx, Json.obj("field" -> "value"))
        _        <- repository.upsertAnswers(unauthorisedPaymentsCtx, Json.obj("field" -> "value"))
        _        <- repository.setStatus(paymentsIntoPensionsCtx, Completed)
        _        <- repository.setStatus(unauthorisedPaymentsCtx, InProgress)
        statuses <- repository.getAllJourneyStatuses(taxYear, mtditid)
      } yield statuses).rightValue

      result shouldBe List(JourneyNameAndStatus(PaymentsIntoPensions, Completed), JourneyNameAndStatus(UnauthorisedPayments, InProgress))
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
      val result = (for {
        beginning     <- repository.get(sampleCtx)
        createdResult <- EitherT.right[ServiceError](repository.upsertStatus(sampleCtx, InProgress))
        created       <- repository.get(sampleCtx)
        updatedResult <- EitherT.right[ServiceError](repository.upsertStatus(sampleCtx, Completed))
        updated       <- repository.get(sampleCtx)
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

  "getAnswers" should {
    "return None if no answers" in {
      val maybeAnswers = repository.getAnswers[PaymentsIntoPensionsStorageAnswers](sampleCtx).value.futureValue
      assert(maybeAnswers.value === None)
    }

    "return answers" in {
      val answers = PaymentsIntoPensionsStorageAnswers(true, Some(true), true, Some(false), Some(false))
      val maybeAnswers = (for {
        _                <- repository.upsertAnswers(sampleCtx, Json.toJson(answers))
        persistedAnswers <- repository.getAnswers[PaymentsIntoPensionsStorageAnswers](sampleCtx)
      } yield persistedAnswers).value.futureValue

      assert(maybeAnswers.value.value === answers)
    }
  }

  "upsert and get answers" should {
    val cases: TableFor1[StorageAnswers[_]] = Table(
      "answers",
      paymentsIntoPensions.paymentsIntoPensionsStorageAnswers,
      ukpensionincome.storageAnswers,
      statePension.storageAnswers,
      annualAllowances.annualAllowancesStorageAnswers,
      unauthorisedPayments.storageAnswers,
      paymentsIntoOverseasPensions.piopStorageAnswers,
      incomeFromOverseasPensions.incomeFromOverseasPensionsStorageAnswers,
      transfersIntoOverseasPensions.transfersIntoOverseasPensionsStorageAnswers,
      shortServiceRefunds.shortServiceRefundsCtxStorageAnswers
    )

    "save and load" in forAll(cases) { answers =>
      val actual = upsertAndGet(answers)
      assert(actual === answers)
    }
  }

  private def upsertAndGet(ans: StorageAnswers[_]) = {
    val actual = ans match {
      case ans: PaymentsIntoPensionsStorageAnswers =>
        for {
          _             <- repository.upsertPaymentsIntoPensions(journeyCtxWithNino, ans)
          actualAnswers <- repository.getPaymentsIntoPensions(journeyCtxWithNino)
        } yield actualAnswers
      case ans: UkPensionIncomeStorageAnswers =>
        for {
          _             <- repository.upsertUkPensionIncome(journeyCtxWithNino, ans)
          actualAnswers <- repository.getUkPensionIncome(journeyCtxWithNino)
        } yield actualAnswers
      case ans: IncomeFromPensionsStatePensionStorageAnswers =>
        for {
          _             <- repository.upsertStatePension(journeyCtxWithNino, ans)
          actualAnswers <- repository.getStatePension(journeyCtxWithNino)
        } yield actualAnswers
      case ans: AnnualAllowancesStorageAnswers =>
        for {
          _             <- repository.upsertAnnualAllowances(journeyCtxWithNino, ans)
          actualAnswers <- repository.getAnnualAllowances(journeyCtxWithNino)
        } yield actualAnswers
      case ans: UnauthorisedPaymentsStorageAnswers =>
        for {
          _             <- repository.upsertUnauthorisedPayments(journeyCtxWithNino, ans)
          actualAnswers <- repository.getUnauthorisedPayments(journeyCtxWithNino)
        } yield actualAnswers
      case ans: PaymentsIntoOverseasPensionsStorageAnswers =>
        for {
          _             <- repository.upsertPaymentsIntoOverseasPensions(journeyCtxWithNino, ans)
          actualAnswers <- repository.getPaymentsIntoOverseasPensions(journeyCtxWithNino)
        } yield actualAnswers
      case ans: IncomeFromOverseasPensionsStorageAnswers =>
        for {
          _             <- repository.upsertIncomeFromOverseasPensions(journeyCtxWithNino, ans)
          actualAnswers <- repository.getIncomeFromOverseasPensions(journeyCtxWithNino)
        } yield actualAnswers
      case ans: TransfersIntoOverseasPensionsStorageAnswers =>
        for {
          _             <- repository.upsertTransferIntoOverseasPensions(journeyCtxWithNino, ans)
          actualAnswers <- repository.getTransferIntoOverseasPensions(journeyCtxWithNino)
        } yield actualAnswers
      case ans: ShortServiceRefundsStorageAnswers =>
        for {
          _             <- repository.upsertShortServiceRefunds(journeyCtxWithNino, ans)
          actualAnswers <- repository.getShortServiceRefunds(journeyCtxWithNino)
        } yield actualAnswers
      case _ => fail("Wrong setup, missing type")
    }

    actual.value.futureValue.value.value
  }

}
