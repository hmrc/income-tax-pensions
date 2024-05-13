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

package services

import cats.implicits._
import models.common.Journey.PaymentsIntoPensions
import models.common.JourneyStatus.Completed
import models.common.{Journey, JourneyContext, JourneyNameAndStatus, JourneyStatus}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import stubs.repositories.StubJourneyAnswersRepository
import utils.EitherTTestOps.convertScalaFuture
import utils.TestUtils.{hc, mtditid, taxYear}

import java.time.Instant

class JourneyStatusServiceImplSpec extends AnyWordSpecLike with Matchers {

  val repository = StubJourneyAnswersRepository()
  val now        = Instant.now()

  val underTest = new JourneyStatusServiceImpl(repository)

  "getAllStatuses" should {
    "return an empty list if no answers exist" in {
      val result = underTest.getAllStatuses(taxYear, mtditid)
      result.value.futureValue shouldBe List.empty.asRight
    }

    "return a List of JourneyNameAndStatus" in {
      val statusList = List(
        JourneyNameAndStatus(Journey.PaymentsIntoPensions, JourneyStatus.Completed),
        JourneyNameAndStatus(Journey.UnauthorisedPayments, JourneyStatus.InProgress)
      )
      val underTest = new JourneyStatusServiceImpl(repository.copy(getAllJourneyStatuses = statusList))

      val result = underTest.getAllStatuses(taxYear, mtditid)
      result.value.futureValue shouldBe statusList.asRight
    }
  }

  "getJourneyStatus" should {
    "return an empty list if no answers exist" in {
      val ctx    = JourneyContext(taxYear, mtditid, PaymentsIntoPensions)
      val result = underTest.getJourneyStatus(ctx)

      result.value.futureValue shouldBe List.empty.asRight
    }

    "return a List of JourneyNameAndStatus" in {
      val ctx = JourneyContext(taxYear, mtditid, PaymentsIntoPensions)

      val statusList = List(JourneyNameAndStatus(Journey.PaymentsIntoPensions, JourneyStatus.Completed))

      val underTest = new JourneyStatusServiceImpl(repository.copy(getJourneyStatus = statusList))

      val result = underTest.getJourneyStatus(ctx)
      result.value.futureValue shouldBe statusList.asRight
    }
  }

  "saveJourneyStatus" should {
    "return Unit if journey is updated successfully" in {
      val ctx = JourneyContext(taxYear, mtditid, PaymentsIntoPensions)

      val underTest = new JourneyStatusServiceImpl(repository.copy(saveJourneyStatus = ()))

      val result = underTest.saveJourneyStatus(ctx, Completed)

      result.value.futureValue shouldBe ().asRight
    }
  }
}
