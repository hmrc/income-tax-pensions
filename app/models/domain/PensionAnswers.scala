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

trait PensionAnswers {
  def isFinished: Boolean

  def getStatus(persistedStatus: Option[JourneyStatus]): JourneyStatus = {
    val calculatedStatus = persistedStatus.map {
      case CheckOurRecords =>
        // If we don't have anything in the database, but we are able to fetch the data
        // we want to still go to CYA page, so we mark this status as NotStarted which means the answers are submitted
        // but not in InProgress/Completed status
        if (isFinished) NotStarted else CheckOurRecords
      case s @ (NotStarted | InProgress | Completed) =>
        if (isFinished) s else CheckOurRecords
      case UnderMaintenance => UnderMaintenance
    }

    calculatedStatus.getOrElse {
      if (isFinished) NotStarted else CheckOurRecords
    }
  }

}
