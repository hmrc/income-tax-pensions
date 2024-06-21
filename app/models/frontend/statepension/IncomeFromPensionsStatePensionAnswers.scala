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

package models.frontend.statepension

import models.AllStateBenefitsData
import models.database.IncomeFromPensionsStatePensionStorageAnswers
import models.domain.PensionAnswers
import play.api.libs.json.{Json, OFormat}

final case class IncomeFromPensionsStatePensionAnswers(
    statePension: Option[StateBenefitAnswers],
    statePensionLumpSum: Option[StateBenefitAnswers],
    sessionId: Option[String]
) extends PensionAnswers {
  def isFinished: Boolean =
    statePension.exists(_.isFinished) || statePensionLumpSum.exists(_.isFinished)

  def removeEmptyAmounts: IncomeFromPensionsStatePensionAnswers =
    copy(
      statePension = if (statePension.exists(_.amount.isEmpty)) None else statePension,
      statePensionLumpSum = if (statePensionLumpSum.exists(_.amount.isEmpty)) None else statePensionLumpSum
    )
}

object IncomeFromPensionsStatePensionAnswers {
  implicit val format: OFormat[IncomeFromPensionsStatePensionAnswers] = Json.format[IncomeFromPensionsStatePensionAnswers]

  val empty: IncomeFromPensionsStatePensionAnswers = IncomeFromPensionsStatePensionAnswers(None, None, None)

  def mkAnswers(downstreamAnswers: Option[AllStateBenefitsData],
                maybeDbAnswers: Option[IncomeFromPensionsStatePensionStorageAnswers]): Option[IncomeFromPensionsStatePensionAnswers] =
    downstreamAnswers
      .map { answers: AllStateBenefitsData =>
        val maybeStatePension        = answers.stateBenefitsData.flatMap(_.statePension).map(StateBenefitAnswers.fromStateBenefit)
        val maybeStatePensionLumpSum = answers.stateBenefitsData.flatMap(_.statePensionLumpSum).map(StateBenefitAnswers.fromStateBenefit)

        IncomeFromPensionsStatePensionAnswers(
          statePension = maybeStatePension,
          statePensionLumpSum = maybeStatePensionLumpSum,
          sessionId = None
        )
      }
      .orElse(
        maybeDbAnswers.map(_.toIncomeFromPensionsStatePensionAnswers)
      )
}
