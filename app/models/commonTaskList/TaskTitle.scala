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

package models.commonTaskList

import models.commonTaskList.taskItemTitles._

trait TaskTitle extends Enumerable.Implicits

object TaskTitle extends TaskTitle {

  val pensionsTitles: PensionsTitles.type                         = PensionsTitles
  val paymentsIntoPensionsTitles: PaymentsIntoPensionsTitles.type = PaymentsIntoPensionsTitles

  val values: Seq[TaskTitle] = Seq(
    pensionsTitles.StatePension(),
    pensionsTitles.OtherUkPensions(),
    pensionsTitles.IncomeFromOverseas(),
    pensionsTitles.UnauthorisedPayments(),
    pensionsTitles.ShortServiceRefunds(),
    paymentsIntoPensionsTitles.PaymentsIntoUk(),
    paymentsIntoPensionsTitles.PaymentsIntoOverseas(),
    paymentsIntoPensionsTitles.AnnualAllowances(),
    paymentsIntoPensionsTitles.OverseasTransfer()
  )

  implicit val enumerable: Enumerable[TaskTitle] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
