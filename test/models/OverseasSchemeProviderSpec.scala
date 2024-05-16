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

import testdata.transfersIntoOverseasPensions.{
  nonUkOverseasSchemeProvider,
  transferPensionSchemeNonUK,
  transferPensionSchemeUK,
  ukOverseasSchemeProvider
}
import utils.TestUtils

class OverseasSchemeProviderSpec extends TestUtils {

  "toTransferPensionScheme" should {
    "transform the OverseasSchemeProvider into a TransferPensionScheme" when {
      "it is a UK scheme" in {
        assert(ukOverseasSchemeProvider.toTransferPensionScheme == transferPensionSchemeUK)
      }
      "it is a non-UK scheme" in {
        assert(nonUkOverseasSchemeProvider.toTransferPensionScheme == transferPensionSchemeNonUK)
      }
    }
  }

}
