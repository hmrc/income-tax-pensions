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

package models

import com.codahale.metrics.SharedMetricRegistries
import models.charges.{OverseasSchemeProvider, PensionSchemeOverseasTransfers}
import testdata.pensionSchemeOverseasTransfers._
import utils.TestUtils

class PensionSchemeOverseasTransfersSpec extends TestUtils {
  SharedMetricRegistries.clear()

  "nonEmpty" should {
    "return true when there are transfers" in {
      assert(pensionSchemeOverseasTransfers.nonEmpty)
    }

    "return false when there are no transfers" in {
      assert(PensionSchemeOverseasTransfers(List(OverseasSchemeProvider("str1", "str2", "str3", None, None)), 0, 0).nonEmpty === true)
    }
  }

}
