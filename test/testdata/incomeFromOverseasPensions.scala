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

package testdata

import models.ForeignPension
import models.database.{IncomeFromOverseasPensionsStorageAnswers, PensionSchemeStorageAnswers}
import models.frontend.{IncomeFromOverseasPensionsAnswers, PensionScheme}

object incomeFromOverseasPensions {

  val pensionScheme: PensionScheme =
    PensionScheme(
      alphaThreeCode = Some("FRA"),
      alphaTwoCode = Some("FR"),
      pensionPaymentAmount = Some(1999.99),
      pensionPaymentTaxPaid = Some(1999.99),
      specialWithholdingTaxQuestion = Some(true),
      specialWithholdingTaxAmount = Some(1999.99),
      foreignTaxCreditReliefQuestion = Some(true),
      taxableAmount = Some(1999.99)
    )

  val incomeFromOverseasPensionsAnswers: IncomeFromOverseasPensionsAnswers = IncomeFromOverseasPensionsAnswers(
    paymentsFromOverseasPensionsQuestion = Some(true),
    overseasIncomePensionSchemes = Seq(pensionScheme)
  )

  val incomeFromOverseasPensionsStorageAnswers: IncomeFromOverseasPensionsStorageAnswers =
    IncomeFromOverseasPensionsStorageAnswers(
      Some(true),
      Seq(
        PensionSchemeStorageAnswers(
          Some(true),
          Some(true)
        )))

  val foreignPension: ForeignPension =
    ForeignPension(
      countryCode = "FRA",
      taxableAmount = 1999.99,
      amountBeforeTax = Some(1999.99),
      taxTakenOff = Some(1999.99),
      specialWithholdingTax = Some(1999.99),
      foreignTaxCreditRelief = Some(true)
    )
}
