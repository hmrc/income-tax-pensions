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

import org.scalatest.wordspec.AnyWordSpecLike
import testdata.transfersIntoOverseasPensions._

class TransferPensionSchemeSpec extends AnyWordSpecLike {

  "toOverseasSchemeProvider" should {
    "convert a TransferPensionScheme to a valid OverseasSchemeProvider" which {
      "has a PSTR value when it is a UK scheme" in {
        val result = transferPensionSchemeUK.toOverseasSchemeProvider

        assert(result == ukOverseasSchemeProvider)
        assert(result.pensionSchemeTaxReference == Some(Seq(pstrReference)))
        assert(result.qualifyingRecognisedOverseasPensionScheme == None)
      }
      "has a QOPS value when it is a non-UK scheme" in {
        val result = transferPensionSchemeNonUK.toOverseasSchemeProvider

        assert(result == nonUkOverseasSchemeProvider)
        assert(result.pensionSchemeTaxReference == None)
        assert(result.qualifyingRecognisedOverseasPensionScheme == Some(Seq(qopsReferenceWithPrefix)))
      }
    }
  }
}
