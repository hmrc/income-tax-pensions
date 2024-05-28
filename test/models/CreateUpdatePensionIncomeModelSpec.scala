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

import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.wordspec.AnyWordSpecLike

class CreateUpdatePensionIncomeModelSpec extends AnyWordSpecLike {
  "nonEmpty" should {
    val cases = Table(
      ("foreignPension", "overseasContribution", "expected"),
      (None, None, false),
      (Some(Nil), None, false),
      (None, Some(Nil), false),
      (Some(Nil), Some(Nil), false),
      (Some(List(ForeignPension("", 1.0, None, None, None, None))), Some(Nil), true),
      (Some(Nil), Some(List(OverseasPensionContribution(Some("str"), 1.0, None, None, None, None, None, None))), true)
    )

    forAll(cases) { (foreignPension, overseasContribution, expected) =>
      s"return $expected for $foreignPension and $overseasContribution" in {
        val model = CreateUpdatePensionIncomeModel(foreignPension, overseasContribution)
        assert(model.nonEmpty == expected)
      }
    }

  }
}
