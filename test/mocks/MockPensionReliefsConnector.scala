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

package mocks

import cats.data.EitherT
import connectors.PensionReliefsConnector
import models.{CreateOrUpdatePensionReliefsModel, GetPensionReliefsModel}
import models.common._
import models.domain.ApiResultT
import models.error.ServiceError
import org.scalamock.handlers.CallHandler3
import org.scalamock.matchers.MockParameter
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockPensionReliefsConnector extends MockFactory {
  val mockReliefsConnector: PensionReliefsConnector = mock[PensionReliefsConnector]

  def mockGetPensionReliefsT(expectedResult: Either[ServiceError, Option[GetPensionReliefsModel]])
      : CallHandler3[Nino, TaxYear, HeaderCarrier, ApiResultT[Option[GetPensionReliefsModel]]] =
    (mockReliefsConnector
      .getPensionReliefsT(_: Nino, _: TaxYear)(_: HeaderCarrier))
      .expects(*, *, *)
      .returns(EitherT.fromEither[Future](expectedResult))
      .anyNumberOfTimes()

  def mockCreateOrAmendPensionReliefsT(expectedResult: Either[ServiceError, Unit],
                                       expectedModel: MockParameter[CreateOrUpdatePensionReliefsModel] = *)
      : CallHandler3[JourneyContextWithNino, CreateOrUpdatePensionReliefsModel, HeaderCarrier, ApiResultT[Unit]] =
    (mockReliefsConnector
      .createOrAmendPensionReliefsT(_: JourneyContextWithNino, _: CreateOrUpdatePensionReliefsModel)(_: HeaderCarrier))
      .expects(*, expectedModel, *)
      .returns(EitherT.fromEither[Future](expectedResult))
      .anyNumberOfTimes()
}
