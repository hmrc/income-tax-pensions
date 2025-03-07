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
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockPensionChargesConnector {
  val mockChargesConnector: PensionChargesConnector = mock[PensionChargesConnector]

  def mockGetPensionChargesT(expectedResult: Either[ServiceError, Option[GetPensionChargesRequestModel]])
      : OngoingStubbing[ApiResultT[Option[GetPensionChargesRequestModel]]] =
    when(
      mockChargesConnector
        .getPensionChargesT(anyNino, anyTaxYear)(any[HeaderCarrier]))
      .thenReturn(EitherT.fromEither[Future](expectedResult))

  def mockCreateOrAmendPensionChargesT(expectedResult: Either[ServiceError, Unit],
                                       expectedModel: CreateUpdatePensionChargesRequestModel): OngoingStubbing[ApiResultT[Unit]] =
    when(
      mockChargesConnector
        .createUpdatePensionChargesT(any[JourneyContextWithNino], meq(expectedModel))(any[HeaderCarrier]))
      .thenReturn(EitherT.fromEither[Future](expectedResult))
}
