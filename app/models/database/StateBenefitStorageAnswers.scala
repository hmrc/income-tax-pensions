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

import play.api.libs.json.{Json, OFormat}

final case class StateBenefitStorageAnswers(
    startDateQuestion: Option[Boolean],
    endDateQuestion: Option[Boolean],
    submittedOnQuestion: Option[Boolean],
    dateIgnoredQuestion: Option[Boolean],
    amountPaidQuestion: Option[Boolean],
    taxPaidQuestion: Option[Boolean]
)

object StateBenefitStorageAnswers {
  implicit val format: OFormat[StateBenefitStorageAnswers] = Json.format[StateBenefitStorageAnswers]

  def fromJourneyAnswers(answers: models.frontend.statepension.StateBenefitAnswers): StateBenefitStorageAnswers =
    StateBenefitStorageAnswers(
      answers.startDateQuestion,
      answers.endDateQuestion,
      answers.submittedOnQuestion,
      answers.dateIgnoredQuestion,
      answers.amountPaidQuestion,
      answers.taxPaidQuestion
    )
}
