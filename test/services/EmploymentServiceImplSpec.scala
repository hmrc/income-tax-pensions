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
import models.employment.CreateUpdateEmploymentRequest.{CreateUpdateEmployment, CreateUpdateEmploymentData, PayModel}
import models.employment.{AllEmploymentData, CreateUpdateEmploymentRequest, EmploymentSource}
import models.error.ServiceError.DownstreamError
import models.frontend.ukpensionincome.UkPensionIncomeAnswers
import models.submission.EmploymentPensions
import org.scalatest.EitherValues._
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import testdata.ukpensionincome.sampleSingleUkPensionIncome
import utils.AllEmploymentsDataBuilder.allEmploymentsData
import utils.EitherTTestOps.convertScalaFuture
import utils.FutureUtils.FutureOps
import utils.TestUtils

import scala.concurrent.Future

class EmploymentServiceImplSpec extends TestUtils with MockEmploymentConnector {

  val service: EmploymentServiceImpl = new EmploymentServiceImpl(mockEmploymentConnector)

  "loading employment data" when {
    "connector returns a success result with an employment data model" must {
      "convert the model to an EmploymentPensions and return it" in {
        MockEmploymentConnector
          .getEmployments(validNino, currentTaxYear)
          .returns(allEmploymentsData.some.asRight.toFuture)

        val result: Either[ServiceErrorModel, EmploymentPensions] =
          service
            .getEmployment(testContext)
            .value
            .futureValue

        val expectedEmploymentModel: EmploymentPensions =
          EmploymentPensions.fromEmploymentResponse(allEmploymentsData)

        result shouldBe expectedEmploymentModel.asRight
      }
    }
    "connector returns a success result but with no EmploymentPensions" must {
      "generate an empty EmploymentPensions model" in {
        MockEmploymentConnector
          .getEmployments(validNino, currentTaxYear)
          .returns(none[AllEmploymentData].asRight.toFuture)

        val result: Either[ServiceErrorModel, EmploymentPensions] =
          service
            .getEmployment(testContext)
            .value
            .futureValue

        result shouldBe EmploymentPensions.empty.asRight
      }
    }
    "connector returns an unsuccessful result" must {
      "propagate the error" in {
        MockEmploymentConnector
          .getEmployments(validNino, currentTaxYear)
          .returns(someServiceError.asLeft.toFuture)

        val result: Either[ServiceErrorModel, EmploymentPensions] =
          service
            .getEmployment(testContext)
            .value
            .futureValue

        result.left.value shouldBe a[DownstreamError]
      }
    }
  }

  "upsertUkPensionIncome" must {
    val createRequest = CreateUpdateEmploymentRequest(
      Some("some_id"),
      Some(
        CreateUpdateEmployment(
          Some("some_ref"),
          "some name",
          "2020-01-01",
          Some("2021-01-01"),
          Some("some_id")
        )),
      Some(CreateUpdateEmploymentData(PayModel(0.0, 0.0))),
      Some(false)
    )

    val answers = UkPensionIncomeAnswers(
      uKPensionIncomesQuestion = true,
      List(sampleSingleUkPensionIncome)
    )

    "save an employment if there is nothing existing already" in {
      MockEmploymentConnector
        .getEmployments(validNino, currentTaxYear)
        .returns(none[AllEmploymentData].asRight.toFuture)
      MockEmploymentConnector
        .saveEmployment(validNino, currentTaxYear, createRequest)
        .returns(Future.successful(Right(())))

      val result: Either[ServiceErrorModel, Unit] =
        service
          .upsertUkPensionIncome(testContext, answers)
          .value
          .futureValue

      result shouldBe ().asRight
    }

    "delete existing, and create a new one if the employment id already exists" in {
      MockEmploymentConnector
        .getEmployments(validNino, currentTaxYear)
        .returns(
          Some(
            AllEmploymentData(
              Nil,
              None,
              customerEmploymentData = List(
                EmploymentSource("id1", "some name", Some("ref"), None, None, None, None, None, None, None, None)
              ),
              None,
              None)).asRight.toFuture)
      MockEmploymentConnector
        .saveEmployment(validNino, currentTaxYear, createRequest)
        .returns(Future.successful(Right(())))
      MockEmploymentConnector
        .deleteEmployment(validNino, currentTaxYear, "id1")
        .returns(Future.successful(Right(())))

      val result: Either[ServiceErrorModel, Unit] =
        service
          .upsertUkPensionIncome(testContext, answers)
          .value
          .futureValue

      result shouldBe ().asRight
    }
  }

}
