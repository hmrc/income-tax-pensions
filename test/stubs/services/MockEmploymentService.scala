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

package stubs.services

import cats.data.EitherT
import cats.implicits._
import models.common.JourneyContextWithNino
import models.error.ServiceError
import models.frontend.ukpensionincome.UkPensionIncomeAnswers
import models.submission.EmploymentPensions
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import services.EmploymentService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockEmploymentService extends MockFactory {
  self: TestSuite =>

  val mockEmploymentService: EmploymentService = mock[EmploymentService]

  def mockGetEmployment(
                         ctx: JourneyContextWithNino
                       )(result: Either[ServiceError, EmploymentPensions]): Unit =
    (mockEmploymentService
      .getEmployment(_: JourneyContextWithNino)(_: HeaderCarrier))
      .expects(*, *)
      .returning(EitherT.fromEither[Future](result))

  def mockUpsertUkPensionIncome(
                                 ctx: JourneyContextWithNino,
                                 answers: UkPensionIncomeAnswers
                               ): Unit =
    (mockEmploymentService
      .upsertUkPensionIncome(_: JourneyContextWithNino, _: UkPensionIncomeAnswers)(_: HeaderCarrier))
      .expects(ctx, answers, *)
      .returning(EitherT.rightT[Future, ServiceError](()))
}
