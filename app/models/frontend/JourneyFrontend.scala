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

package models.frontend

import models.common.{Journey, JourneyStatus, TaxYear}
import models.common.Journey._

final case class JourneyFrontend(journey: Journey, status: Option[JourneyStatus]) {

  /** If we have status in DB, it must have been submitted (therefore it will be CYA.)
    *
    * TODO It won't work when we introduce TTL=28 days
    */
  private val isCya: Boolean = status.isDefined

  def urlSuffix: String = journey match {
    case PaymentsIntoPensions =>
      "payments-into-pensions/" + (if (isCya) "check-payments-into-pensions" else "relief-at-source")
    case UkPensionIncome =>
      "pension-income/" + (if (isCya) "check-uk-pension-income" else "uk-pension-income")
    case StatePension =>
      "pension-income/" + (if (isCya) "check-state-pension" else "state-pension")
    case UnauthorisedPayments =>
      "unauthorised-payments-from-pensions/" + (if (isCya) "check-unauthorised-payments" else "unauthorised-payments")
    case AnnualAllowances =>
      "annual-allowance/" + (if (isCya) "check-annual-allowance" else "reduced-annual-allowance")
    case PaymentsIntoOverseasPensions =>
      "overseas-pensions/payments-into-overseas-pensions/" + (if (isCya) "check-overseas-pension-details" else "payments-into-schemes")
    case IncomeFromOverseasPensions =>
      "overseas-pensions/income-from-overseas-pensions/" + (if (isCya) "check-overseas-pension-income" else "pension-overseas-income-status")
    case TransferIntoOverseasPensions =>
      "overseas-pensions/overseas-transfer-charges/" + (if (isCya) "transfer-charges/check-transfer-charges-details" else "transfer-pension-savings")
    case ShortServiceRefunds =>
      "overseas-pensions/short-service-refunds/" + (if (isCya) "check-short-service-refund-details" else "taxable-short-service-refunds")
  }
}
