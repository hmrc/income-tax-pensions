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
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.ukpensionincome.UkPensionIncomeAnswers
import models.submission.EmploymentPensions
import services.EmploymentService
import uk.gov.hmrc.http.HeaderCarrier
import utils.EmploymentPensionsBuilder.employmentPensionsData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class StubEmploymentService(
    loadEmploymentResult: Either[ServiceError, EmploymentPensions] = employmentPensionsData.asRight[ServiceError],
    var ukPensionIncome: List[UkPensionIncomeAnswers] = Nil
) extends EmploymentService {
  def getEmployment(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[EmploymentPensions] =
    EitherT.fromEither[Future](loadEmploymentResult)

  def upsertUkPensionIncome(ctx: JourneyContextWithNino, answers: UkPensionIncomeAnswers)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    ukPensionIncome ::= answers
    EitherT.rightT(())
  }
}
