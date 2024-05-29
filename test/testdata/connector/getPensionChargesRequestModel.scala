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

package testdata.connector

import models.charges.{GetPensionChargesRequestModel, PensionSchemeOverseasTransfers}
import testdata.transfersIntoOverseasPensions.{nonUkOverseasSchemeProvider, ukOverseasSchemeProvider}

object getPensionChargesRequestModel {

  val getPensionChargesRequestModel: GetPensionChargesRequestModel = GetPensionChargesRequestModel(
    submittedOn = "2020-07-27T17:00:19Z",
    pensionSavingsTaxCharges = None, // TODO replace Nones with valid data when needed
    pensionSchemeOverseasTransfers = Some(
      PensionSchemeOverseasTransfers(
        overseasSchemeProvider = Seq(ukOverseasSchemeProvider, nonUkOverseasSchemeProvider),
        transferCharge = 1,
        transferChargeTaxPaid = 2
      )),
    pensionSchemeUnauthorisedPayments = None,
    pensionContributions = None,
    overseasPensionContributions = None
  )

}
