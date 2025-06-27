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
import models.AllStateBenefitsData
import models.common.JourneyContextWithNino
import models.error.ServiceError
import models.frontend.statepension.IncomeFromPensionsStatePensionAnswers
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import services.StateBenefitService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockStateBenefitService extends MockFactory {
  self: TestSuite =>

  val mockStateBenefitsService: StateBenefitService = mock[StateBenefitService]

  def mockGetStateBenefits(ctx: JourneyContextWithNino,
                           result: Either[ServiceError, Option[AllStateBenefitsData]]
  ): Unit =
    (mockStateBenefitsService.getStateBenefits(_: JourneyContextWithNino)(_: HeaderCarrier))
      .expects(*, *)
      .anyNumberOfTimes()
      .returning(EitherT.fromEither[Future](result))

  def mockUpsertStateBenefits(ctx: JourneyContextWithNino, answers: IncomeFromPensionsStatePensionAnswers): Unit =
    (mockStateBenefitsService.upsertStateBenefits(_: JourneyContextWithNino, _: IncomeFromPensionsStatePensionAnswers)(_: HeaderCarrier))
      .expects(ctx, answers, *)
      .anyNumberOfTimes()
      .returning(EitherT.rightT[Future, ServiceError](()))
}
