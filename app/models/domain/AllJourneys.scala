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

import cats.implicits.toBifunctorOps
import models.common.Journey._
import models.common.JourneyStatus.CheckOurRecords
import models.common.{Journey, JourneyNameAndStatus, JourneyStatus}
import models.frontend._
import models.frontend.statepension.IncomeFromPensionsStatePensionAnswers
import models.frontend.ukpensionincome.UkPensionIncomeAnswers

final case class AllJourneys(
    paymentsIntoPensions: Option[PaymentsIntoPensionsAnswers],
    ukPensionIncome: Option[UkPensionIncomeAnswers],
    statePension: Option[IncomeFromPensionsStatePensionAnswers],
    annualAllowances: Option[AnnualAllowancesAnswers],
    authorisedPaymentsFromPensions: Option[UnauthorisedPaymentsAnswers],
    incomeFromOverseasPensions: Option[IncomeFromOverseasPensionsAnswers],
    paymentsIntoOverseasPensions: Option[PaymentsIntoOverseasPensionsAnswers],
    transfersIntoOverseasPensions: Option[TransfersIntoOverseasPensionsAnswers],
    shortServiceRefunds: Option[ShortServiceRefundsAnswers],
    statuses: List[JourneyNameAndStatus]
) {
  def getStatus(journey: Journey): Option[JourneyStatus] = {
    val persistedStatus = statuses.find(_.name == journey).map(_.journeyStatus)
    persistedStatus
  }

  def updateStatus(journey: Journey, newStatus: JourneyStatus): AllJourneys = {
    val newStatuses = statuses.map { status =>
      if (status.name == journey) status.copy(journeyStatus = newStatus)
      else status
    }

    if (newStatuses.map(_.name).contains(journey)) copy(statuses = newStatuses)
    else copy(statuses = JourneyNameAndStatus(journey, newStatus) :: newStatuses)
  }
}

object AllJourneys {
  def empty: AllJourneys = new AllJourneys(None, None, None, None, None, None, None, None, None, Nil)

  private def allUnderMaintenance: AllJourneys = new AllJourneys(
    None,
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
      maybePersistedStatuses: ApiResult[List[JourneyNameAndStatus]]
  ): AllJourneys = {
    def updateAnswersWithStatus(allJourneys: AllJourneys, journey: Journey): AllJourneys = {
      val persistedStatus = allJourneys.getStatus(journey)

      journey match {
        case PaymentsIntoPensions =>
          val (answers, status) = toCommonTaskListStatus(paymentsIntoPensions, persistedStatus)
          allJourneys.copy(paymentsIntoPensions = answers).updateStatus(journey, status)
        case UkPensionIncome =>
          val (answers, status) = toCommonTaskListStatus(ukPensionIncome, persistedStatus)
          allJourneys.copy(ukPensionIncome = answers).updateStatus(journey, status)
        case StatePension =>
          val (answers, status) = toCommonTaskListStatus(statePension, persistedStatus)
          allJourneys.copy(statePension = answers).updateStatus(journey, status)
        case AnnualAllowances =>
          val (answers, status) = toCommonTaskListStatus(annualAllowances, persistedStatus)
          allJourneys.copy(annualAllowances = answers).updateStatus(journey, status)
        case UnauthorisedPayments =>
          val (answers, status) = toCommonTaskListStatus(unauthorisedPaymentsFromPensions, persistedStatus)
          allJourneys.copy(authorisedPaymentsFromPensions = answers).updateStatus(journey, status)
        case PaymentsIntoOverseasPensions =>
          val (answers, status) = toCommonTaskListStatus(paymentsIntoOverseasPensions, persistedStatus)
          allJourneys.copy(paymentsIntoOverseasPensions = answers).updateStatus(journey, status)
        case IncomeFromOverseasPensions =>
          val (answers, status) = toCommonTaskListStatus(incomeFromOverseasPensions, persistedStatus)
          allJourneys.copy(incomeFromOverseasPensions = answers).updateStatus(journey, status)
        case TransferIntoOverseasPensions =>
          val (answers, status) = toCommonTaskListStatus(transfersIntoOverseasPensions, persistedStatus)
          allJourneys.copy(transfersIntoOverseasPensions = answers).updateStatus(journey, status)
        case ShortServiceRefunds =>
          val (answers, status) = toCommonTaskListStatus(shortServiceRefunds, persistedStatus)
          allJourneys.copy(shortServiceRefunds = answers).updateStatus(journey, status)
      }
    }

    val allJourneys = maybePersistedStatuses
      .map(statuses => empty.copy(statuses = statuses))
      .getOrElse(allUnderMaintenance)

    val updatedAllJourneys = Journey.values.foldLeft(allJourneys) { case (updated, journey) =>
      updateAnswersWithStatus(updated, journey)
    }

    updatedAllJourneys
  }

  private def toCommonTaskListStatus[A <: PensionAnswers](result: ApiResult[Option[A]],
                                                          persistedStatus: Option[JourneyStatus]): (Option[A], JourneyStatus) = {
    val maybeStatus = result.bimap(
      _ => None -> JourneyStatus.UnderMaintenance,
      maybeAnswer =>
        maybeAnswer
          .map { answer =>
            val newStatus = answer.getStatus(persistedStatus)
            maybeAnswer -> newStatus
          }
          .getOrElse(None -> CheckOurRecords)
    )

    maybeStatus.merge
  }

}
