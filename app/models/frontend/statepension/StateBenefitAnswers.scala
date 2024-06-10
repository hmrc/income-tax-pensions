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

package models.frontend.statepension

import models.StateBenefit
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate
import java.util.UUID

case class StateBenefitAnswers(
    benefitId: Option[UUID],
    startDateQuestion: Option[Boolean],
    startDate: Option[LocalDate],
    amountPaidQuestion: Option[Boolean],
    amount: Option[BigDecimal],
    taxPaidQuestion: Option[Boolean] = None,
    taxPaid: Option[BigDecimal]
)

object StateBenefitAnswers {
  implicit val format: OFormat[StateBenefitAnswers] = Json.format[StateBenefitAnswers]

  def empty: StateBenefitAnswers = StateBenefitAnswers(None, None, None, None, None, None, None)

  def fromStateBenefit(benefit: StateBenefit): StateBenefitAnswers =
    StateBenefitAnswers(
      benefitId = Some(benefit.benefitId),
      startDateQuestion = Some(true),
      startDate = Some(benefit.startDate),
      amountPaidQuestion = Some(benefit.amount.isDefined),
      amount = benefit.amount,
      taxPaidQuestion = Some(benefit.taxPaid.isDefined),
      taxPaid = benefit.taxPaid
    )
}
