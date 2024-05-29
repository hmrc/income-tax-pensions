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

import models.charges.PensionContributions
import models.database.AnnualAllowancesStorageAnswers
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.wordspec.AnyWordSpecLike
import testdata.annualAllowances.{annualAllowancesAnswers, pensionContributions}

class PensionContributionsSpec extends AnyWordSpecLike {

  "toAnnualAllowances" should {
    "create AnnualAllowancesAnswers when database answers exist" which {
      "merges api answers with database answers" in {
        val storageAnswers = AnnualAllowancesStorageAnswers(Some(true), Some(true))
        assert(pensionContributions.toAnnualAllowances(maybeDbAnswers = Some(storageAnswers)) === Some(annualAllowancesAnswers))
      }
      "overrides database answers if api answers are different" in {
        val storageAnswers = AnnualAllowancesStorageAnswers(Some(false), None)
        assert(pensionContributions.toAnnualAllowances(maybeDbAnswers = Some(storageAnswers)) === Some(annualAllowancesAnswers))
      }
    }
    "return None when there are no database answers" in {
      assert(pensionContributions.toAnnualAllowances(maybeDbAnswers = None) === None)
    }
  }

  "nonEmpty" should {
    val cases = Table(
      ("model", "expected"),
      (PensionContributions.empty, false),
      (PensionContributions.empty.copy(pensionSchemeTaxReference = List("str")), true),
      (PensionContributions.empty.copy(isAnnualAllowanceReduced = Some(true)), true),
      (PensionContributions.empty.copy(isAnnualAllowanceReduced = Some(false)), false),
      (PensionContributions.empty.copy(taperedAnnualAllowance = Some(true)), true),
      (PensionContributions.empty.copy(taperedAnnualAllowance = Some(false)), false),
      (PensionContributions.empty.copy(moneyPurchasedAllowance = Some(true)), true),
      (PensionContributions.empty.copy(moneyPurchasedAllowance = Some(false)), false)
    )

    forAll(cases) { (model, expected) =>
      s"return $expected for $model" in {
        assert(model.nonEmpty == expected)
      }
    }
  }
}
