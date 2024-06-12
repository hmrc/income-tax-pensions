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

package models.database

import models.AllStateBenefitsData
import models.frontend.statepension.{IncomeFromPensionsStatePensionAnswers, StateBenefitAnswers}
import play.api.libs.json.{Json, OFormat}

final case class IncomeFromPensionsStatePensionStorageAnswers(statePension: Option[Boolean], statePensionLumpSum: Option[Boolean]) {

  def toIncomeFromPensionsStatePensionAnswers(sessionId: Option[String],
                                              maybeStateBenefits: Option[AllStateBenefitsData]): IncomeFromPensionsStatePensionAnswers = {

    val maybeStatePension: Option[StateBenefitAnswers] =
      maybeStateBenefits.flatMap(_.stateBenefitsData.flatMap(_.statePension)).map(StateBenefitAnswers.fromStateBenefit)
    val maybeStatePensionLumpSum: Option[StateBenefitAnswers] =
      maybeStateBenefits.flatMap(_.stateBenefitsData.flatMap(_.statePensionLumpSum)).map(StateBenefitAnswers.fromStateBenefit)

    IncomeFromPensionsStatePensionAnswers(
      statePension = maybeStatePension,
      statePensionLumpSum = maybeStatePensionLumpSum,
      sessionId = sessionId
    )
  }
}

object IncomeFromPensionsStatePensionStorageAnswers {
  implicit val format: OFormat[IncomeFromPensionsStatePensionStorageAnswers] = Json.format[IncomeFromPensionsStatePensionStorageAnswers]

  def fromJourneyAnswers(answers: IncomeFromPensionsStatePensionAnswers): IncomeFromPensionsStatePensionStorageAnswers =
    IncomeFromPensionsStatePensionStorageAnswers(
      answers.statePension.flatMap(_.amountPaidQuestion),
      answers.statePensionLumpSum.flatMap(_.amountPaidQuestion)
    )
}
