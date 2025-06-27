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
import models.GetPensionIncomeModel
import models.common._
import models.domain.ApiResultT
import models.error.ServiceError
import org.scalamock.handlers.CallHandler3
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockPensionIncomesConnector extends MockFactory {
  self: TestSuite =>
  val mockIncomesConnector: PensionIncomeConnector = mock[PensionIncomeConnector]

  def mockGetPensionIncomesT(
      expectedResult: Either[ServiceError, Option[GetPensionIncomeModel]]
                            ): CallHandler3[Nino, TaxYear, HeaderCarrier, ApiResultT[Option[GetPensionIncomeModel]]] =
    (mockIncomesConnector
      .getPensionIncomeT(_: Nino, _:TaxYear)(_: HeaderCarrier))
      .expects(*, *, *)
      .anyNumberOfTimes()
      .returning(EitherT.fromEither[Future](expectedResult))

}
