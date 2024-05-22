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

import cats.implicits.catsSyntaxOptionId
import com.codahale.metrics.SharedMetricRegistries
import play.api.libs.json.{JsObject, Json}
import testdata.connector.getPensionIncomeModel._
import testdata.connector.getPensionReliefsModel._
import testdata.paymentsIntoOverseasPensions.{paymentsIntoOverseasPensionsAnswers, piopStorageAnswers}
import testdata.paymentsIntoPensions.{paymentsIntoPensionsAnswers, paymentsIntoPensionsStorageAnswers}
import utils.TestUtils

class GetPensionReliefsModelSpec extends TestUtils {
  SharedMetricRegistries.clear()

  val jsonModel: JsObject = Json.obj(
    "submittedOn" -> "2020-01-04T05:01:01Z",
    "deletedOn"   -> "2020-01-04T05:01:01Z",
    "pensionReliefs" -> Json.obj(
      "regularPensionContributions"         -> 100.01,
      "oneOffPensionContributionsPaid"      -> 100.01,
      "retirementAnnuityPayments"           -> 100.01,
      "paymentToEmployersSchemeNoTaxRelief" -> 100.01,
      "overseasPensionSchemeContributions"  -> 100.01
    )
  )

  "GetPensionReliefsModel with all values" should {

    "parse to Json" in {
      Json.toJson(
        GetPensionReliefsModel(
          "2020-01-04T05:01:01Z",
          Some("2020-01-04T05:01:01Z"),
          PensionReliefs(
            Some(100.01),
            Some(100.01),
            Some(100.01),
            Some(100.01),
            Some(100.01)
          )
        )) mustBe jsonModel
    }

    "parse from Json" in {
      jsonModel.as[PensionReliefs]
    }
  }

  "toPaymentsIntoPensions" should {
    "convert model to a PaymentsIntoPensionsAnswers when supplied valid Reliefs and Database answers" in {
      val result = getPensionReliefsModel.toPaymentsIntoPensions(paymentsIntoPensionsStorageAnswers.some)

      assert(result == paymentsIntoPensionsAnswers.some)
    }
    "return None when Database answers are empty and reliefs has no PaymentIntoPension answers" in {
      val emptyGetPensionReliefsModel = getPensionReliefsModel.copy(pensionReliefs = PensionReliefs.empty)
      val result                      = emptyGetPensionReliefsModel.toPaymentsIntoPensions(None)

      assert(result == None)
    }
  }

  "toPaymentsIntoOverseasPensionsAnswers" should {
    "convert model to a PaymentsIntoOverseasPensionsAnswers when supplied valid Relief, Income and Database answers" in {
      val result = getPensionReliefsModel.toPaymentsIntoOverseasPensionsAnswers(getPensionIncomeModel.some, piopStorageAnswers.some)

      assert(result == paymentsIntoOverseasPensionsAnswers.some)
    }
    "return None when Database answers are empty and there is no amount returned from the API" in {
      val emptyGetPensionReliefsModel = getPensionReliefsModel.copy(pensionReliefs = PensionReliefs.empty)
      val result                      = emptyGetPensionReliefsModel.toPaymentsIntoOverseasPensionsAnswers(getPensionIncomeModel.some, None)

      assert(result == None)
    }
  }

}
