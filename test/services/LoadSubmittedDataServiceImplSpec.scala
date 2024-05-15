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

package services

import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId, none}
import mocks.MockEmploymentConnector
import models.ServiceErrorModel
import models.employment.AllEmploymentData
import models.submission.EmploymentPensions
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import utils.AllEmploymentsDataBuilder.allEmploymentsData
import utils.EitherTTestOps.convertScalaFuture
import utils.FutureUtils.FutureOps
import utils.TestUtils

class LoadSubmittedDataServiceImplSpec extends TestUtils with MockEmploymentConnector {

  val service: EmploymentServiceImpl =
    new EmploymentServiceImpl(mockEmploymentConnector)

  "loading employment data" when {
    "connector returns a success result with an employment data model" must {
      "convert the model to an EmploymentPensions and return it" in {
        MockEmploymentConnector
          .loadEmployments(nino, taxYear)
          .returns(allEmploymentsData.some.asRight.toFuture)

        val result: Either[ServiceErrorModel, EmploymentPensions] =
          service
            .getEmployment(nino, taxYear, mtditid)
            .futureValue

        val expectedEmploymentModel: EmploymentPensions =
          EmploymentPensions.fromEmploymentResponse(allEmploymentsData)

        result shouldBe expectedEmploymentModel.asRight
      }
    }
    "connector returns a success result but with no EmploymentPensions" must {
      "generate an empty EmploymentPensions model" in {
        MockEmploymentConnector
          .loadEmployments(nino, taxYear)
          .returns(none[AllEmploymentData].asRight.toFuture)

        val result: Either[ServiceErrorModel, EmploymentPensions] =
          service
            .getEmployment(nino, taxYear, mtditid)
            .futureValue

        result shouldBe EmploymentPensions.empty.asRight
      }
    }
    "connector returns an unsuccessful result" must {
      "propagate the error" in {
        MockEmploymentConnector
          .loadEmployments(nino, taxYear)
          .returns(someServiceError.asLeft.toFuture)

        val result: Either[ServiceErrorModel, EmploymentPensions] =
          service
            .getEmployment(nino, taxYear, mtditid)
            .futureValue

        result shouldBe someServiceError.asLeft
      }
    }
  }

}
