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
import models.database.TransfersIntoOverseasPensionsStorageAnswers
import testdata.pensionSchemeOverseasTransfers._
import testdata.transfersIntoOverseasPensions.{transfersIntoOverseasPensionsAnswers, transfersIntoOverseasPensionsStorageAnswers}
import utils.TestUtils

class PensionSchemeOverseasTransfersSpec extends TestUtils {
  SharedMetricRegistries.clear()

  "toTransfersIntoOverseasPensions" should {
    "return None when there are no DB answers" in {
      assert(pensionSchemeOverseasTransfers.toTransfersIntoOverseasPensions(None) == None)
    }
    "return full journey answers, combining API and DB answers" in {
      assert(
        pensionSchemeOverseasTransfers.toTransfersIntoOverseasPensions(
          transfersIntoOverseasPensionsStorageAnswers.some) == transfersIntoOverseasPensionsAnswers.some)
    }
    "return full journey answers, overruling DB answers when API answers are different" in {
      assert(
        pensionSchemeOverseasTransfers.toTransfersIntoOverseasPensions(
          TransfersIntoOverseasPensionsStorageAnswers(Some(false), Some(false), Some(false)).some) == transfersIntoOverseasPensionsAnswers.some)
    }
  }

}
