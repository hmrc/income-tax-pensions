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

import cats.implicits.catsSyntaxOptionId
import models.common.Journey._
import models.common.JourneyStatus.CheckOurRecords
import models.common.{Journey, JourneyNameAndStatus, JourneyStatus}
import models.frontend._
import models.frontend.statepension.IncomeFromPensionsStatePensionAnswers

final case class AllJourneys(
    paymentsIntoPensions: Option[PaymentsIntoPensionsAnswers],
    ukPensionIncome: Option[UkPensionIncomeAnswers],
    annualAllowances: Option[AnnualAllowancesAnswers],
    authorisedPaymentsFromPensions: Option[UnauthorisedPaymentsAnswers],
    incomeFromOverseasPensions: Option[IncomeFromOverseasPensionsAnswers],
    paymentsIntoOverseasPensions: Option[PaymentsIntoOverseasPensionsAnswers],
    transfersIntoOverseasPensions: Option[TransfersIntoOverseasPensionsAnswers],
    shortServiceRefunds: Option[ShortServiceRefundsAnswers],
    statuses: List[JourneyNameAndStatus]
) {
  def getStatus(journey: Journey): Option[JourneyStatus] =
    AllJourneys.getStatus(statuses, journey)

  def updateStatus(journey: Journey, maybeNewStatus: Option[JourneyStatus]): AllJourneys = {
    val newStatuses = maybeNewStatus
      .map { newStatus =>
        if (statuses.map(_.name).contains(journey)) {
          val newStatuses = statuses.map { status =>
            if (status.name == journey) status.copy(journeyStatus = newStatus)
            else status
          }
          newStatuses
        } else {
          JourneyNameAndStatus(journey, newStatus) :: statuses
        }
      }
      .getOrElse(statuses)

    copy(statuses = newStatuses)
  }
}

object AllJourneys {
  def empty: AllJourneys = new AllJourneys(None, None, None, None, None, None, None, None, Nil)

  def getStatus(statuses: List[JourneyNameAndStatus], journey: Journey): Option[JourneyStatus] =
    statuses.find(_.name == journey).map(_.journeyStatus)

  private def allUnderMaintenance: AllJourneys = new AllJourneys(
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    None,
    List(
      JourneyNameAndStatus(Journey.PaymentsIntoPensions, JourneyStatus.UnderMaintenance),
      JourneyNameAndStatus(Journey.UkPensionIncome, JourneyStatus.UnderMaintenance),
      JourneyNameAndStatus(Journey.StatePension, JourneyStatus.UnderMaintenance),
      JourneyNameAndStatus(Journey.AnnualAllowances, JourneyStatus.UnderMaintenance),
      JourneyNameAndStatus(Journey.UnauthorisedPayments, JourneyStatus.UnderMaintenance),
      JourneyNameAndStatus(Journey.PaymentsIntoOverseasPensions, JourneyStatus.UnderMaintenance),
      JourneyNameAndStatus(Journey.IncomeFromOverseasPensions, JourneyStatus.UnderMaintenance),
      JourneyNameAndStatus(Journey.TransferIntoOverseasPensions, JourneyStatus.UnderMaintenance),
      JourneyNameAndStatus(Journey.ShortServiceRefunds, JourneyStatus.UnderMaintenance)
    )
  )

  def fromAnswersAndStatuses(
      paymentsIntoPensions: ApiResult[Option[PaymentsIntoPensionsAnswers]],
      ukPensionIncome: ApiResult[Option[UkPensionIncomeAnswers]],
      statePension: ApiResult[Option[IncomeFromPensionsStatePensionAnswers]],
      annualAllowances: ApiResult[Option[AnnualAllowancesAnswers]],
      unauthorisedPaymentsFromPensions: ApiResult[Option[UnauthorisedPaymentsAnswers]],
      incomeFromOverseasPensions: ApiResult[Option[IncomeFromOverseasPensionsAnswers]],
      paymentsIntoOverseasPensions: ApiResult[Option[PaymentsIntoOverseasPensionsAnswers]],
      transfersIntoOverseasPensions: ApiResult[Option[TransfersIntoOverseasPensionsAnswers]],
      shortServiceRefunds: ApiResult[Option[ShortServiceRefundsAnswers]],
      maybeStatuses: ApiResult[List[JourneyNameAndStatus]]
  ): AllJourneys = {
    def toUnderMaintenanceOnError(statuses: List[JourneyNameAndStatus], journey: Journey): JourneyStatus = journey match {
      case PaymentsIntoPensions         => toCommonTaskListStatus(paymentsIntoPensions, getStatus(statuses, journey))
      case UkPensionIncome              => toCommonTaskListStatus(ukPensionIncome, getStatus(statuses, journey))
      case StatePension                 => toCommonTaskListStatus(statePension, getStatus(statuses, journey))
      case AnnualAllowances             => toCommonTaskListStatus(annualAllowances, getStatus(statuses, journey))
      case UnauthorisedPayments         => toCommonTaskListStatus(unauthorisedPaymentsFromPensions, getStatus(statuses, journey))
      case PaymentsIntoOverseasPensions => toCommonTaskListStatus(paymentsIntoOverseasPensions, getStatus(statuses, journey))
      case IncomeFromOverseasPensions   => toCommonTaskListStatus(incomeFromOverseasPensions, getStatus(statuses, journey))
      case TransferIntoOverseasPensions => toCommonTaskListStatus(transfersIntoOverseasPensions, getStatus(statuses, journey))
      case ShortServiceRefunds          => toCommonTaskListStatus(shortServiceRefunds, getStatus(statuses, journey))
    }

    val allJourneys = maybeStatuses
      .map(statuses => empty.copy(statuses = statuses))
      .getOrElse(allUnderMaintenance)

    val allJourneysWithProperStatuses = Journey.values.foldLeft(allJourneys) { case (updated, journey) =>
      updated.updateStatus(journey, toUnderMaintenanceOnError(updated.statuses, journey).some)
    }

    allJourneysWithProperStatuses
  }

  private def toCommonTaskListStatus(result: ApiResult[_], currentStatus: Option[JourneyStatus]): JourneyStatus = {
    val maybeUnderMaintenance = result.left.toOption.map(_ => JourneyStatus.UnderMaintenance)
    val maybeStatus           = maybeUnderMaintenance.orElse(currentStatus)
    val status                = maybeStatus.getOrElse(CheckOurRecords)

    status
  }

}
