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

package models.domain

import models.common.JourneyStatus
import models.common.JourneyStatus._
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.wordspec.AnyWordSpecLike

class PensionAnswersSpec extends AnyWordSpecLike {

  "getStatus" should {
    val cases = Table[Option[JourneyStatus], Boolean, JourneyStatus](
      ("persistedStatus", "isFinished", "expected"),
      (None, false, CheckOurRecords),
      (None, true, NotStarted),
      (Some(CheckOurRecords), false, CheckOurRecords),
      (Some(CheckOurRecords), true, NotStarted),
      (Some(NotStarted), false, CheckOurRecords),
      (Some(NotStarted), true, NotStarted),
      (Some(InProgress), false, CheckOurRecords),
      (Some(InProgress), true, InProgress),
      (Some(Completed), false, CheckOurRecords),
      (Some(Completed), true, Completed),
      (Some(UnderMaintenance), false, UnderMaintenance),
      (Some(UnderMaintenance), true, UnderMaintenance)
    )

    "calculate status based on persisted status and isFinished answers" in forAll(cases) { case (persistedStatus, finished, expected) =>
      val pensionAnswers = new PensionAnswers {
        def isFinished: Boolean = finished
      }

      assert(pensionAnswers.getStatus(persistedStatus) === expected)
    }
  }
}
