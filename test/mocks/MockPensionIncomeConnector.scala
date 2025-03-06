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
import connectors.PensionIncomeConnector
import models.common._
import models.domain.ApiResultT
import models.error.ServiceError
import models.{CreateUpdatePensionIncomeModel, GetPensionIncomeModel}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockPensionIncomeConnector {
  val mockIncomeConnector: PensionIncomeConnector = mock[PensionIncomeConnector]

  def mockGetPensionIncomeT(expectedResult: Either[ServiceError, Option[GetPensionIncomeModel]]): OngoingStubbing[ApiResultT[Option[GetPensionIncomeModel]]] =
    when(mockIncomeConnector
      .getPensionIncomeT(anyNino, anyTaxYear)(any[HeaderCarrier]))
      .thenReturn(EitherT.fromEither[Future](expectedResult))

  def mockCreateOrAmendPensionIncomeT(expectedResult: Either[ServiceError, Unit], expectedModel: CreateUpdatePensionIncomeModel)
  : OngoingStubbing[ApiResultT[Unit]] =
    when(mockIncomeConnector
      .createOrAmendPensionIncomeT(any[JourneyContextWithNino], meq(expectedModel))(any[HeaderCarrier]))
      .thenReturn(EitherT.fromEither[Future](expectedResult))
}
