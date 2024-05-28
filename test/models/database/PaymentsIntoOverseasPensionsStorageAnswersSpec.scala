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

package models.database

import cats.implicits.catsSyntaxOptionId
import org.scalatest.wordspec.AnyWordSpecLike
import testdata.connector.getPensionIncomeModel._
import testdata.connector.getPensionReliefsModel.getPensionReliefsModel
import testdata.paymentsIntoOverseasPensions.{emptyPiopStorageAnswers, paymentsIntoOverseasPensionsAnswers, piopStorageAnswers}

class PaymentsIntoOverseasPensionsStorageAnswersSpec extends AnyWordSpecLike {

  "toPaymentsIntoOverseasPensionsAnswers" should {
    "convert model to a PaymentsIntoOverseasPensionsAnswers" when {
      "supplied valid Relief, Income and Database answers" in {
        val result = piopStorageAnswers.toPaymentsIntoOverseasPensionsAnswers(getPensionIncomeModel.some, getPensionReliefsModel.some)

        assert(result == paymentsIntoOverseasPensionsAnswers.some)
      }
      "supplied valid Relief, Income answers overwrite the existing Database answers" in {
        val oppositeStorageAnswers = PaymentsIntoOverseasPensionsStorageAnswers(Some(false), Some(false), Some(true))
        val result = oppositeStorageAnswers.toPaymentsIntoOverseasPensionsAnswers(getPensionIncomeModel.some, getPensionReliefsModel.some)

        assert(result == paymentsIntoOverseasPensionsAnswers.some)
      }
    }
    "return None when Database answers and Incomes API response are empty" in {
      val result = emptyPiopStorageAnswers.toPaymentsIntoOverseasPensionsAnswers(None, None)

      assert(result == None)
    }
  }
}
