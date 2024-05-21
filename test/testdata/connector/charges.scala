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

import models.charges.{Charge, PensionSchemeUnauthorisedPayments}

object charges {
  val sampleCharges = PensionSchemeUnauthorisedPayments(
    pensionSchemeTaxReference = Some(List("00123456RA")),
    surcharge = Some(
      Charge(
        amount = 1.0,
        foreignTaxPaid = 2.0
      )),
    noSurcharge = Some(
      Charge(
        amount = 3.0,
        foreignTaxPaid = 4.0
      ))
  )

  val sampleChargesWithNones = PensionSchemeUnauthorisedPayments(
    pensionSchemeTaxReference = Some(List("00123456RA")),
    surcharge = None,
    noSurcharge = None
  )

}
