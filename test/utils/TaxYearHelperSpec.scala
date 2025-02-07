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

package utils

class TaxYearHelperSpec extends TestUtils {

  "TaxYearHelper" should {

    "return a string containing the last year and the last two digits of this year" in {
      val taxYear = 2020
      val result  = TaxYearHelper.taxYearConverter(taxYear)
      result mustBe "2019-20"
    }

    "return a string containing the last year and the last two digits of this year for a tax year ending in 00" in {
      val taxYear = 2100
      val result  = TaxYearHelper.taxYearConverter(taxYear)
      result mustBe "2099-00"
    }

    "return a TYS String for the last 2 digits of both the tax year -1 and tax tear" in {
      val taxYear = 2024
      TaxYearHelper.ifTysTaxYearConverter(taxYear) mustBe "23-24"
    }

    "return a DES API Path for a non-TYS api" in {
      val (taxYear, apiNum) = (2023, "1611")
      TaxYearHelper.apiPath("NINO", taxYear, apiNum) mustBe "NINO/2022-23"
    }

    "return an IF API Path for a TYS api" in {
      val (taxYear, apiNum) = (2024, "1611")
      TaxYearHelper.apiPath("NINO", taxYear, apiNum) mustBe "23-24/NINO"
    }
    "return the current TYS api from a past year" when { // scalastyle:off magic.number
      val tysMap: Map[(String, Int), String] = Map(
        ("baseApi1", 2024) -> "tysApi2024",
        ("baseApi1", 2026) -> "tysApi2026",
        ("baseApi2", 2027) -> "tysApi2027"
      ).toSeq.sortWith(_._1._2 > _._1._2).toMap

      "a TYS api from a past year can still active" in {
        TaxYearHelper.apiVersion(2025, "baseApi1", tysMap) mustBe
          TaxYearHelper.apiVersion(2024, "baseApi1", tysMap)
      }
      "a TYS api is superseded by a future year" in {
        TaxYearHelper.apiVersion(2026, "baseApi1", tysMap) mustNot
          be(TaxYearHelper.apiVersion(2025, "baseApi1", tysMap))
      }
      "for 2 TYS apis of different base APIs" in {
        TaxYearHelper.apiVersion(2028, "baseApi1", tysMap) mustBe "tysApi2026"
        TaxYearHelper.apiVersion(2028, "baseApi2", tysMap) mustBe "tysApi2027"
      }
    }
  }
}
