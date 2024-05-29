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
import models.frontend.statepension.IncomeFromPensionsStatePensionAnswers
import models.statebenefit.BenefitType._
import play.api.libs.json.{Json, OFormat}

import java.time.{Instant, LocalDate, ZonedDateTime}
import java.util.UUID

final case class StateBenefitsUserData(
    benefitType: String,
    sessionDataId: Option[UUID],
    sessionId: String,
    mtdItId: String,
    nino: String,
    taxYear: Int,
    benefitDataType: String,
    claim: Option[Claim],
    lastUpdated: Instant
)

object StateBenefitsUserData {
  implicit val format: OFormat[StateBenefitsUserData] = Json.format[StateBenefitsUserData]

  def fromJourneyAnswers(ctx: JourneyContextWithNino,
                         answers: IncomeFromPensionsStatePensionAnswers,
                         lastUpdated: ZonedDateTime): List[StateBenefitsUserData] = {
    val benefits = BenefitType.values.map { benefitType =>
      val stateBenefit = benefitType match {
        case StatePension        => answers.statePension
        case StatePensionLumpSum => answers.statePensionLumpSum
      }

      val claimModel = Claim(
        benefitId = stateBenefit.flatMap(_.benefitId),
        startDate = stateBenefit.flatMap(_.startDate).getOrElse(LocalDate.now()),
        endDateQuestion = stateBenefit.flatMap(_.endDateQuestion),
        endDate = stateBenefit.flatMap(_.endDate),
        dateIgnored = stateBenefit.flatMap(_.dateIgnored),
        submittedOn = stateBenefit.flatMap(_.submittedOn),
        amount = stateBenefit.flatMap(_.amount),
        taxPaidQuestion = stateBenefit.flatMap(_.taxPaidQuestion),
        taxPaid = stateBenefit.flatMap(_.taxPaid)
      )

      StateBenefitsUserData(
        benefitType = benefitType.value,
        sessionDataId = None,
        sessionId = answers.sessionId,
        mtdItId = ctx.mtditid.value,
        nino = ctx.nino.value,
        taxYear = ctx.taxYear.endYear,
        benefitDataType = if (claimModel.benefitId.isEmpty) "customerAdded" else "customerOverride",
        claim = Some(claimModel),
        lastUpdated = Instant.parse(lastUpdated.toLocalDateTime.toString + "Z")
      )
    }

    benefits
  }
}
