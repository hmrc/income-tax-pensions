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
import connectors.PensionChargesConnector
import models.charges.{CreateUpdatePensionChargesRequestModel, GetPensionChargesRequestModel}
import models.common._
import models.domain.ApiResultT
import models.error.ServiceError
import org.scalamock.handlers.CallHandler3
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockPensionChargesConnector extends MockFactory {
  self: TestSuite =>
  val mockChargesConnector: PensionChargesConnector = mock[PensionChargesConnector]

  def mockGetPensionChargesT(
    expectedResult: Either[ServiceError, Option[GetPensionChargesRequestModel]]
  ): CallHandler3[Nino, TaxYear, HeaderCarrier, ApiResultT[Option[GetPensionChargesRequestModel]]] =
    (mockChargesConnector
      .getPensionChargesT(_: Nino, _: TaxYear)(_: HeaderCarrier))
      .expects(*, *, *)
      .anyNumberOfTimes()
      .returning(EitherT.fromEither[Future](expectedResult))

  def mockCreateOrAmendPensionChargesT(expectedResult: Either[ServiceError, Unit])
                                      (expectedModel: CreateUpdatePensionChargesRequestModel): CallHandler3[JourneyContextWithNino, CreateUpdatePensionChargesRequestModel, HeaderCarrier, ApiResultT[Unit]] =
    (mockChargesConnector
      .createUpdatePensionChargesT(_: JourneyContextWithNino, _: CreateUpdatePensionChargesRequestModel)(_: HeaderCarrier))
      .expects(*, expectedModel, *)
      .returning(EitherT.fromEither[Future](expectedResult))
}
