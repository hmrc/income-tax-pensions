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

import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.wordspec.AnyWordSpecLike

class CreateUpdatePensionChargesRequestModelSpec extends AnyWordSpecLike {

  "nonEmpty" should {
    val empty = CreateUpdatePensionChargesRequestModel.empty

    val cases = Table(
      ("model", "expected"),
      (empty, false),
      (
        empty.copy(pensionSchemeOverseasTransfers =
          Some(PensionSchemeOverseasTransfers.empty.copy(overseasSchemeProvider = List(OverseasSchemeProvider("str1", "str2", "str3", None, None))))),
        true),
      (
        empty.copy(pensionSchemeUnauthorisedPayments =
          Some(PensionSchemeUnauthorisedPayments.empty.copy(pensionSchemeTaxReference = Some(List("some value"))))),
        true),
      (empty.copy(pensionContributions = Some(PensionContributions.empty.copy(pensionSchemeTaxReference = List("some value")))), true),
      (
        empty.copy(overseasPensionContributions =
          Some(OverseasPensionContributions.empty.copy(overseasSchemeProvider = List(OverseasSchemeProvider("str1", "str2", "str3", None, None))))),
        true)
    )

    forAll(cases) { (model, expected) =>
      s"return $expected for $model" in {
        assert(model.nonEmpty == expected)
      }
    }
  }
}
