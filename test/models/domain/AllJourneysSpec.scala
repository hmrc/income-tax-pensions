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

import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId}
import models.common.JourneyStatus.{CheckOurRecords, InProgress}
import models.common.{Journey, JourneyNameAndStatus, JourneyStatus}
import models.error.ServiceError
import models.error.ServiceError.DownstreamError
import models.frontend._
import models.frontend.statepension.IncomeFromPensionsStatePensionAnswers
import org.scalatest.wordspec.AnyWordSpecLike
import testdata.annualAllowances
import testdata.frontend.incomeFromPensionsStatePensionAnswers

class AllJourneysSpec extends AnyWordSpecLike {

  "fromAnswersAndStatuses" should {
    val error: ServiceError = DownstreamError("some error")

    val statuses = List(
      JourneyNameAndStatus(Journey.PaymentsIntoPensions, JourneyStatus.NotStarted),
      JourneyNameAndStatus(Journey.UkPensionIncome, JourneyStatus.NotStarted),
      JourneyNameAndStatus(Journey.StatePension, JourneyStatus.NotStarted),
      JourneyNameAndStatus(Journey.AnnualAllowances, JourneyStatus.NotStarted),
      JourneyNameAndStatus(Journey.UnauthorisedPayments, JourneyStatus.NotStarted),
      JourneyNameAndStatus(Journey.PaymentsIntoOverseasPensions, JourneyStatus.NotStarted),
      JourneyNameAndStatus(Journey.IncomeFromOverseasPensions, JourneyStatus.NotStarted),
      JourneyNameAndStatus(Journey.TransferIntoOverseasPensions, JourneyStatus.NotStarted),
      JourneyNameAndStatus(Journey.ShortServiceRefunds, JourneyStatus.NotStarted)
    )

    "create journeys with under maintenance status when some of the answers fail to be fetched" in {

      val allJourneys = AllJourneys.fromAnswersAndStatuses(
        error.asLeft[Option[PaymentsIntoPensionsAnswers]],
        error.asLeft[Option[UkPensionIncomeAnswers]],
        error.asLeft[Option[IncomeFromPensionsStatePensionAnswers]],
        error.asLeft[Option[AnnualAllowancesAnswers]],
        error.asLeft[Option[UnauthorisedPaymentsAnswers]],
        error.asLeft[Option[IncomeFromOverseasPensionsAnswers]],
        error.asLeft[Option[PaymentsIntoOverseasPensionsAnswers]],
        error.asLeft[Option[TransfersIntoOverseasPensionsAnswers]],
        error.asLeft[Option[ShortServiceRefundsAnswers]],
        Right(statuses)
      )

      assert(
        allJourneys.statuses === List(
          JourneyNameAndStatus(Journey.PaymentsIntoPensions, JourneyStatus.UnderMaintenance),
          JourneyNameAndStatus(Journey.UkPensionIncome, JourneyStatus.UnderMaintenance),
          JourneyNameAndStatus(Journey.StatePension, JourneyStatus.UnderMaintenance),
          JourneyNameAndStatus(Journey.AnnualAllowances, JourneyStatus.UnderMaintenance),
          JourneyNameAndStatus(Journey.UnauthorisedPayments, JourneyStatus.UnderMaintenance),
          JourneyNameAndStatus(Journey.PaymentsIntoOverseasPensions, JourneyStatus.UnderMaintenance),
          JourneyNameAndStatus(Journey.IncomeFromOverseasPensions, JourneyStatus.UnderMaintenance),
          JourneyNameAndStatus(Journey.TransferIntoOverseasPensions, JourneyStatus.UnderMaintenance),
          JourneyNameAndStatus(Journey.ShortServiceRefunds, JourneyStatus.UnderMaintenance)
        ))
    }

    "return mix statuses" in {
      val allJourneys = AllJourneys.fromAnswersAndStatuses(
        error.asLeft[Option[PaymentsIntoPensionsAnswers]],
        None.asRight,
        incomeFromPensionsStatePensionAnswers.sample.some.asRight,
        annualAllowances.annualAllowancesAnswers.some.asRight,
        error.asLeft[Option[UnauthorisedPaymentsAnswers]],
        error.asLeft[Option[IncomeFromOverseasPensionsAnswers]],
        error.asLeft[Option[PaymentsIntoOverseasPensionsAnswers]],
        error.asLeft[Option[TransfersIntoOverseasPensionsAnswers]],
        error.asLeft[Option[ShortServiceRefundsAnswers]],
        Right(statuses.map { s =>
          s.name match {
            case Journey.UkPensionIncome  => s.copy(journeyStatus = CheckOurRecords)
            case Journey.AnnualAllowances => s.copy(journeyStatus = InProgress)
            case _                        => s
          }
        })
      )

      assert(
        allJourneys.statuses === List(
          JourneyNameAndStatus(Journey.PaymentsIntoPensions, JourneyStatus.UnderMaintenance),
          JourneyNameAndStatus(Journey.UkPensionIncome, JourneyStatus.CheckOurRecords),
          JourneyNameAndStatus(Journey.StatePension, JourneyStatus.NotStarted),
          JourneyNameAndStatus(Journey.AnnualAllowances, JourneyStatus.InProgress),
          JourneyNameAndStatus(Journey.UnauthorisedPayments, JourneyStatus.UnderMaintenance),
          JourneyNameAndStatus(Journey.PaymentsIntoOverseasPensions, JourneyStatus.UnderMaintenance),
          JourneyNameAndStatus(Journey.IncomeFromOverseasPensions, JourneyStatus.UnderMaintenance),
          JourneyNameAndStatus(Journey.TransferIntoOverseasPensions, JourneyStatus.UnderMaintenance),
          JourneyNameAndStatus(Journey.ShortServiceRefunds, JourneyStatus.UnderMaintenance)
        ))
    }
  }
}
