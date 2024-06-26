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

package models.incomes

import models.charges.{OverseasPensionContributions, OverseasSchemeProvider}
import org.scalatest.prop.TableDrivenPropertyChecks
import testdata.paymentsIntoOverseasPensions._
import utils.TestUtils

class OverseasPensionContributionSpec extends TestUtils with TableDrivenPropertyChecks {

  "toOverseasPensionScheme" should {
    val cases = Table(
      ("overseasPensionContribution", "expectedOPS"),
      (mmrOverseasPensionContribution, mmrOverseasPensionScheme),
      (dblOverseasPensionContribution, dblOverseasPensionScheme),
      (tcrOverseasPensionContribution, tcrOverseasPensionScheme)
    )

    "convert to a valid OverseasPensionScheme" when forAll(cases) { (opc, expectedResult) =>
      s"OPC is of type ${opc.getReliefType}" in {
        assert(opc.toOverseasPensionScheme == expectedResult)
      }
    }
  }

  "nonEmpty" should {
    val cases = Table(
      ("model", "expected"),
      (OverseasPensionContributions.empty, false),
      (OverseasPensionContributions.empty.copy(overseasSchemeProvider = List(OverseasSchemeProvider("str", "str2", "str3", None, None))), true)
    )

    forAll(cases) { (model, expected) =>
      s"return $expected for $model" in {
        assert(model.nonEmpty == expected)
      }
    }
  }
}
