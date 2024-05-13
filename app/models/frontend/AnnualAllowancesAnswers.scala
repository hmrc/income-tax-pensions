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

import models.PensionContributions
import play.api.libs.json.{Json, OFormat}

final case class AnnualAllowancesAnswers(reducedAnnualAllowanceQuestion: Option[Boolean],
                                         moneyPurchaseAnnualAllowance: Option[Boolean],
                                         taperedAnnualAllowance: Option[Boolean],
                                         aboveAnnualAllowanceQuestion: Option[Boolean],
                                         aboveAnnualAllowance: Option[BigDecimal],
                                         pensionProvidePaidAnnualAllowanceQuestion: Option[Boolean],
                                         taxPaidByPensionProvider: Option[BigDecimal],
                                         pensionSchemeTaxReferences: Option[Seq[String]]) {
  // TODO in the API_1868 schema pensionSchemeTaxReferences requires a minimum of one entry, this is causing an error if any gateway answers are 'false'
  def toPensionChargesContributions: PensionContributions = PensionContributions(
    pensionSchemeTaxReference = pensionSchemeTaxReferences.getOrElse(Nil),
    inExcessOfTheAnnualAllowance = aboveAnnualAllowance.getOrElse(0.0),
    annualAllowanceTaxPaid = taxPaidByPensionProvider.getOrElse(0),
    isAnnualAllowanceReduced = reducedAnnualAllowanceQuestion,
    taperedAnnualAllowance = taperedAnnualAllowance,
    moneyPurchasedAllowance = moneyPurchaseAnnualAllowance
  )
}

object AnnualAllowancesAnswers {
  implicit val format: OFormat[AnnualAllowancesAnswers] = Json.format[AnnualAllowancesAnswers]
}
