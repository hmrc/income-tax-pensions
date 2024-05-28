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

package models.charges

import org.scalatest.wordspec.AnyWordSpecLike

class PensionSavingsTaxChargesSpec extends AnyWordSpecLike {

  "nonEmpty" should {
    "return false when all fields are empty" in {
      assert(PensionSavingsTaxCharges.empty.nonEmpty === false)
    }

    "return true when pensionSchemeTaxReference defined" in {
      assert(PensionSavingsTaxCharges.empty.copy(pensionSchemeTaxReference = Some(List("str"))).nonEmpty === true)
    }

    "return true when lumpSumBenefitTakenInExcessOfLifetimeAllowance defined" in {
      assert(
        PensionSavingsTaxCharges.empty.copy(lumpSumBenefitTakenInExcessOfLifetimeAllowance = Some(LifetimeAllowance(1.0, 2.0))).nonEmpty === true)
    }

    "return true when benefitInExcessOfLifetimeAllowance defined" in {
      assert(PensionSavingsTaxCharges.empty.copy(benefitInExcessOfLifetimeAllowance = Some(LifetimeAllowance(1.0, 2.0))).nonEmpty === true)
    }

  }
}
