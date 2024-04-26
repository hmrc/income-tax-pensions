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

package services

import cats.data.EitherT
import connectors.EmploymentConnector
import models.submission.EmploymentPensions
import services.journeyAnswers.ServiceOutcome
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.ExecutionContext

trait LoadSubmittedDataService {
  def loadEmployment(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): ServiceOutcome[EmploymentPensions]
}

class LoadSubmittedDataServiceImpl @Inject() (connector: EmploymentConnector)(implicit ec: ExecutionContext) extends LoadSubmittedDataService {

  override def loadEmployment(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): ServiceOutcome[EmploymentPensions] =
    EitherT(connector.loadEmployments(nino, taxYear)).map {
      case Some(e) => EmploymentPensions.fromEmploymentResponse(e)
      case None    => EmploymentPensions.empty
    }.value

}
