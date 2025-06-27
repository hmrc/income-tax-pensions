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
import org.scalamock.handlers.{CallHandler3, CallHandler5}
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

trait MockEmploymentConnector extends MockFactory {
  self: TestSuite =>
  val mockEmploymentConnector: EmploymentConnector = mock[EmploymentConnector]

  object MockEmploymentConnector {

    def getEmployments(nino: Nino,
                       taxYear: TaxYear)
                      (returnValue: DownstreamOutcome[Option[AllEmploymentData]]): CallHandler3[Nino, TaxYear, HeaderCarrier, DownstreamOutcome[Option[AllEmploymentData]]] =
      (mockEmploymentConnector
        .getEmployments(_: Nino, _: TaxYear)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(returnValue)

    def saveEmployment(nino: Nino,
                       taxYear: TaxYear,
                       model: CreateUpdateEmploymentRequest)
                      (returnValue: DownstreamOutcome[Unit]): CallHandler5[Nino, TaxYear, CreateUpdateEmploymentRequest, HeaderCarrier, ExecutionContext, DownstreamOutcome[Unit]] =
      (mockEmploymentConnector
        .saveEmployment(_: Nino, _: TaxYear, _:CreateUpdateEmploymentRequest)(_: HeaderCarrier, _: ExecutionContext))
        .expects(nino, taxYear, model, *, *)
        .returning(returnValue)

    def deleteEmployment(nino: Nino,
                         taxYear: TaxYear,
                         employmentId: String)
                        (returnValue: DownstreamOutcome[Unit]): CallHandler5[Nino, TaxYear, String, HeaderCarrier, ExecutionContext, DownstreamOutcome[Unit]] =
      (mockEmploymentConnector
        .deleteEmployment(_: Nino, _: TaxYear, _: String)(_: HeaderCarrier, _: ExecutionContext))
        .expects(nino, taxYear, employmentId, *, *)
        .returning(returnValue)

  }

}
