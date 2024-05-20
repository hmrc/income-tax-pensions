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

import models.frontend.UnauthorisedPaymentsAnswers
import org.scalatest.wordspec.AnyWordSpecLike
import testdata.connector.charges.{sampleCharges, sampleChargesWithNones}
import testdata.database.unauthorisedPaymentsStorageAnswers.sampleStorageAnswers

class PensionSchemeUnauthorisedPaymentsSpec extends AnyWordSpecLike {
  "toUnauthorisedPayments" should {
    "create UnauthorisedPaymentsAnswers" in {
      val result = sampleCharges.toUnauthorisedPayments(Some(sampleStorageAnswers))
      assert(
        result === UnauthorisedPaymentsAnswers(
          surchargeQuestion = Some(true),
          noSurchargeQuestion = Some(true),
          surchargeAmount = Some(1.0),
          surchargeTaxAmountQuestion = Some(true),
          surchargeTaxAmount = Some(2.0),
          noSurchargeAmount = Some(3.0),
          noSurchargeTaxAmountQuestion = Some(true),
          noSurchargeTaxAmount = Some(4.0),
          ukPensionSchemesQuestion = Some(true),
          pensionSchemeTaxReference = Some(List("00123456RA"))
        ))
    }

    "take db answers if downstream answers not defined" in {
      val result = sampleChargesWithNones.toUnauthorisedPayments(Some(sampleStorageAnswers))
      assert(
        result === UnauthorisedPaymentsAnswers(
          surchargeQuestion = Some(true),
          noSurchargeQuestion = Some(true),
          surchargeAmount = None,
          surchargeTaxAmountQuestion = Some(true),
          surchargeTaxAmount = None,
          noSurchargeAmount = None,
          noSurchargeTaxAmountQuestion = Some(true),
          noSurchargeTaxAmount = None,
          ukPensionSchemesQuestion = Some(true),
          pensionSchemeTaxReference = Some(List("00123456RA"))
        ))

    }
  }
}
