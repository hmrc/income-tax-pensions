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

package models

import models.common.Journey._
import models.common.JourneyStatus._
import models.common.{Journey, JourneyStatus, TaxYear}
import models.domain.AllJourneys
import models.frontend.JourneyFrontend

package object commonTaskList {

  private def getHref(frontendUrl: String, taxYear: TaxYear, journey: Journey, status: Option[JourneyStatus]): String = {
    val journeyUrlSuffix = JourneyFrontend(journey, status).urlSuffix
    s"$frontendUrl/pensions/$taxYear/$journeyUrlSuffix"
  }

  private def getStatus(maybeStatus: Option[JourneyStatus]): TaskStatus =
    maybeStatus
      .map {
        case CheckOurRecords  => TaskStatus.CheckNow()
        case NotStarted       => TaskStatus.InProgress()
        case InProgress       => TaskStatus.InProgress()
        case Completed        => TaskStatus.Completed()
        case UnderMaintenance => TaskStatus.Completed() // TODO Waiting for Common task list to support the new status
      }
      .getOrElse(TaskStatus.CheckNow())

  private def getTaskTitle(journey: Journey): TaskTitle =
    journey match {
      case StatePension                 => TaskTitle.pensionsTitles.StatePension()
      case UkPensionIncome              => TaskTitle.pensionsTitles.OtherUkPensions()
      case UnauthorisedPayments         => TaskTitle.pensionsTitles.UnauthorisedPayments()
      case ShortServiceRefunds          => TaskTitle.pensionsTitles.ShortServiceRefunds()
      case IncomeFromOverseasPensions   => TaskTitle.pensionsTitles.IncomeFromOverseas()
      case PaymentsIntoPensions         => TaskTitle.paymentsIntoPensionsTitles.PaymentsIntoUk()
      case AnnualAllowances             => TaskTitle.paymentsIntoPensionsTitles.AnnualAllowances()
      case PaymentsIntoOverseasPensions => TaskTitle.paymentsIntoPensionsTitles.PaymentsIntoOverseas()
      case TransferIntoOverseasPensions => TaskTitle.paymentsIntoPensionsTitles.OverseasTransfer()
    }

  def fromAllJourneys(allJourneys: AllJourneys, frontendUrl: String, taxYear: TaxYear): TaskListModel = {
    def createJourneySection(journey: Journey) = {
      val maybeStatus = allJourneys.getStatus(journey)
      TaskListSectionItem(
        getTaskTitle(journey),
        getStatus(maybeStatus),
        Some(getHref(frontendUrl, taxYear, journey, maybeStatus))
      )
    }

    val paymentsIntoPensions         = createJourneySection(Journey.PaymentsIntoPensions)
    val ukPensionIncome              = createJourneySection(Journey.UkPensionIncome)
    val statePension                 = createJourneySection(Journey.StatePension)
    val annualAllowances             = createJourneySection(Journey.AnnualAllowances)
    val unauthorisedPayments         = createJourneySection(Journey.UnauthorisedPayments)
    val incomeFromOverseasPensions   = createJourneySection(Journey.IncomeFromOverseasPensions)
    val paymentsIntoOverseasPensions = createJourneySection(Journey.PaymentsIntoOverseasPensions)
    val transferIntoOverseasPensions = createJourneySection(Journey.TransferIntoOverseasPensions)
    val shortServiceRefunds          = createJourneySection(Journey.ShortServiceRefunds)

    val pensionsSectionSection = TaskListSection(
      SectionTitle.PensionsTitle(),
      Some(
        List(
          statePension,
          ukPensionIncome,
          unauthorisedPayments,
          shortServiceRefunds,
          incomeFromOverseasPensions
        )
      )
    )

    val paymentsIntoPensionsSection = TaskListSection(
      SectionTitle.PaymentsIntoPensionsTitle(),
      Some(
        List(
          paymentsIntoPensions,
          annualAllowances,
          paymentsIntoOverseasPensions,
          transferIntoOverseasPensions
        )
      )
    )

    val taskList = List(
      pensionsSectionSection,
      paymentsIntoPensionsSection
    )

    TaskListModel(taskList)
  }

}
