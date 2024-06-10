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
import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{Instant, LocalDate}
import java.util.UUID

final case class StateBenefitsUserData(
    benefitType: String,
    sessionDataId: Option[UUID],
    sessionId: String,
    mtdItId: String,
    nino: String,
    taxYear: Int,
    benefitDataType: String,
    claim: Option[ClaimCYAModel],
    lastUpdated: Instant
)

object StateBenefitsUserData {
  implicit val localDateTimeFormats: Format[Instant] =
    MongoJavatimeFormats.instantFormat // Very important, state-benefit expect a special format for this date field
  implicit val format: OFormat[StateBenefitsUserData] = Json.format[StateBenefitsUserData]

  def fromJourneyAnswers(ctx: JourneyContextWithNino,
                         answers: IncomeFromPensionsStatePensionAnswers,
                         lastUpdated: Instant): List[StateBenefitsUserData] = {
    val benefits = BenefitType.values.flatMap { benefitType =>
      val maybeStateBenefit = benefitType match {
        case StatePension        => answers.statePension
        case StatePensionLumpSum => answers.statePensionLumpSum
      }

      maybeStateBenefit.map { stateBenefit =>
        val claimModel = ClaimCYAModel(
          benefitId = stateBenefit.benefitId,
          startDate = stateBenefit.startDate.getOrElse(LocalDate.now()),
          amount = stateBenefit.amount,
          taxPaid = stateBenefit.taxPaid
        )

        StateBenefitsUserData(
          benefitType = benefitType.value,
          sessionDataId = None,
          sessionId = answers.sessionId.getOrElse(""),
          mtdItId = ctx.mtditid.value,
          nino = ctx.nino.value,
          taxYear = ctx.taxYear.endYear,
          benefitDataType = if (claimModel.benefitId.isEmpty) "customerAdded" else "customerOverride",
          claim = Some(claimModel),
          lastUpdated = lastUpdated
        )
      }

    }

    benefits
  }
}
