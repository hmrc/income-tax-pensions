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

import models.charges.{OverseasPensionContributions, OverseasSchemeProvider}
import org.scalatest.wordspec.AnyWordSpecLike
import testdata.shortServiceRefunds._

class ShortServiceRefundsAnswersSpec extends AnyWordSpecLike {

  "toOverseasPensions" should {
    "convert ShortServiceRefundsAnswers to a valid OverseasPensionContributions" in {
      val result = shortServiceRefundsAnswers.toOverseasPensions

      assert(result == overseasPensionContributions)
      assert(result.shortServiceRefund == BigDecimal(1.0))
      assert(result.shortServiceRefundTaxPaid == BigDecimal(2.0))
      assert(result.overseasSchemeProvider == Seq(sSROverseasSchemeProvider))
    }

    "convert an Empty ShortServiceRefundsAnswers to a valid Empty OverseasPensionContributions" in {
      val result = ShortServiceRefundsAnswers().toOverseasPensions

      assert(result == OverseasPensionContributions(Seq[OverseasSchemeProvider](), BigDecimal(0), BigDecimal(0)))
    }
  }
}
