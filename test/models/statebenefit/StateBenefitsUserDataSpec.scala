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

package models.statebenefit

import models.common.JourneyContextWithNino
import org.scalatest.wordspec.AnyWordSpecLike
import testdata.common
import testdata.frontend.{incomeFromPensionsStatePensionAnswers, stateBenefitAnswers}

import java.time.{Instant, LocalDate}

class StateBenefitsUserDataSpec extends AnyWordSpecLike {
  val ctx = JourneyContextWithNino(common.taxYear, common.mtditid, common.nino)

  "fromStateBenefit" should {
    "convert StateBenefit to StateBenefitAnswers" in {
      val now    = Instant.now
      val result = StateBenefitsUserData.fromJourneyAnswers(ctx, incomeFromPensionsStatePensionAnswers.sample, now)
      assert(
        result === List(
          StateBenefitsUserData(
            "statePension",
            None,
            "sessionId",
            "mtditid",
            "123456789",
            2021,
            "customerOverride",
            Some(ClaimCYAModel(Some(common.uuid), LocalDate.parse("2019-04-23"), Some(300.0), Some(400.0))),
            now
          ),
          StateBenefitsUserData(
            "statePensionLumpSum",
            None,
            "sessionId",
            "mtditid",
            "123456789",
            2021,
            "customerOverride",
            Some(ClaimCYAModel(Some(common.uuid), LocalDate.parse("2019-04-23"), Some(300.0), Some(400.0))),
            now
          )
        )
      )
    }
  }
}
