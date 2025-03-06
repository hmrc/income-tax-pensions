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

import connectors.{DownstreamOutcome, EmploymentConnector}
import models.common.{Nino, TaxYear}
import models.employment.{AllEmploymentData, CreateUpdateEmploymentRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

trait MockEmploymentConnector {
  val mockEmploymentConnector: EmploymentConnector = mock[EmploymentConnector]

  object MockEmploymentConnector {

    def getEmployments(
        nino: Nino,
        taxYear: TaxYear,
        returnValue: DownstreamOutcome[Option[AllEmploymentData]]): OngoingStubbing[DownstreamOutcome[Option[AllEmploymentData]]] =
      when(
        mockEmploymentConnector
          .getEmployments(anyNino, anyTaxYear)(any[HeaderCarrier]))
        .thenReturn(returnValue)

    def saveEmployment(nino: Nino,
                       taxYear: TaxYear,
                       model: CreateUpdateEmploymentRequest,
                       returnValue: DownstreamOutcome[Unit]): OngoingStubbing[DownstreamOutcome[Unit]] =
      when(
        mockEmploymentConnector
          .saveEmployment(anyNino, anyTaxYear, any[CreateUpdateEmploymentRequest])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(returnValue)

    def deleteEmployment(nino: Nino,
                         taxYear: TaxYear,
                         employmentId: String,
                         returnValue: DownstreamOutcome[Unit]): OngoingStubbing[DownstreamOutcome[Unit]] =
      when(
        mockEmploymentConnector
          .deleteEmployment(anyNino, anyTaxYear, any[String])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(returnValue)

  }

}
