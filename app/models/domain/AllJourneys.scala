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

import models.common.{Journey, JourneyNameAndStatus, JourneyStatus}
import models.frontend._

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
    statuses.find(_.name == journey).map(_.journeyStatus)
}
