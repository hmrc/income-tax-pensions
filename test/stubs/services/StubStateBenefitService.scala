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
import models.domain.ApiResultT
import models.error.ServiceError
import models.frontend.statepension.IncomeFromPensionsStatePensionAnswers
import services.StateBenefitService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class StubStateBenefitService(
    getStateBenefitsResult: Either[ServiceError, Option[AllStateBenefitsData]] = Right(None),
    var stateBenefits: List[IncomeFromPensionsStatePensionAnswers] = Nil
) extends StateBenefitService {

  def getStateBenefits(ctx: JourneyContextWithNino)(implicit hc: HeaderCarrier): ApiResultT[Option[AllStateBenefitsData]] =
    EitherT.fromEither[Future](getStateBenefitsResult)

  def upsertStateBenefits(ctx: JourneyContextWithNino, answers: IncomeFromPensionsStatePensionAnswers)(implicit
      hc: HeaderCarrier): ApiResultT[Unit] = {
    stateBenefits ::= answers
    EitherT.rightT(())
  }

}
