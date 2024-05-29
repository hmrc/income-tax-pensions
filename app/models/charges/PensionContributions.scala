/*
 * Copyright 2023 HM Revenue & Customs
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

package models.charges

import cats.implicits.catsSyntaxOptionId
import models.database.AnnualAllowancesStorageAnswers
import models.frontend.AnnualAllowancesAnswers
import play.api.libs.json.{Json, OFormat}

case class PensionContributions(pensionSchemeTaxReference: Seq[String],
                                inExcessOfTheAnnualAllowance: BigDecimal,
                                annualAllowanceTaxPaid: BigDecimal,
                                isAnnualAllowanceReduced: Option[Boolean],
                                taperedAnnualAllowance: Option[Boolean],
                                moneyPurchasedAllowance: Option[Boolean]) {

  def toAnnualAllowances(maybeDbAnswers: Option[AnnualAllowancesStorageAnswers]): Option[AnnualAllowancesAnswers] =
    maybeDbAnswers.map { dbAnswers =>
      val aboveAllowanceGateway: Boolean = inExcessOfTheAnnualAllowance != 0
      val taxPaidGateway: Boolean        = annualAllowanceTaxPaid != 0
      AnnualAllowancesAnswers(
        reducedAnnualAllowanceQuestion = isAnnualAllowanceReduced,
        moneyPurchaseAnnualAllowance = moneyPurchasedAllowance,
        taperedAnnualAllowance = taperedAnnualAllowance,
        aboveAnnualAllowanceQuestion = if (aboveAllowanceGateway) true.some else dbAnswers.aboveAnnualAllowanceQuestion,
        aboveAnnualAllowance = if (aboveAllowanceGateway) inExcessOfTheAnnualAllowance.some else None,
        pensionProvidePaidAnnualAllowanceQuestion = if (taxPaidGateway) true.some else dbAnswers.pensionProvidePaidAnnualAllowanceQuestion,
        taxPaidByPensionProvider = if (taxPaidGateway) annualAllowanceTaxPaid.some else None,
        pensionSchemeTaxReferences = if (pensionSchemeTaxReference.isEmpty) None else pensionSchemeTaxReference.some
      )
    }

  def nonEmpty: Boolean =
    pensionSchemeTaxReference.nonEmpty || isAnnualAllowanceReduced.contains(true) || taperedAnnualAllowance.contains(true) ||
      moneyPurchasedAllowance.contains(true)
}

object PensionContributions {
  implicit val format: OFormat[PensionContributions] = Json.format[PensionContributions]

  def empty: PensionContributions = PensionContributions(Nil, 0.0, 0.0, None, None, None)
}
