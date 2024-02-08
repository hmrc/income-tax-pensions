/*
 * Copyright 2023 HM Revenue & Customs
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

import connectors.{PensionChargesConnector, SubmissionConnector}
import connectors.httpParsers.CreateUpdatePensionChargesHttpParser.CreateUpdatePensionChargesResponse
import connectors.httpParsers.DeletePensionChargesHttpParser.DeletePensionChargesResponse
import connectors.httpParsers.GetPensionChargesHttpParser.GetPensionChargesResponse
import models.{CreateUpdatePensionChargesRequestModel, ServiceErrorModel}
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureEitherOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PensionChargesService @Inject() (chargesConnector: PensionChargesConnector, submissionConnector: SubmissionConnector) {

  def getPensionCharges(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionChargesResponse] =
    chargesConnector.getPensionCharges(nino, taxYear)

  def saveUserPensionChargesData(nino: String, mtditid: String, taxYear: Int, userData: CreateUpdatePensionChargesRequestModel)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Either[ServiceErrorModel, Unit]] =
    (for {
      _      <- FutureEitherOps[ServiceErrorModel, Unit](chargesConnector.createUpdatePensionCharges(nino, taxYear, userData))
      result <- FutureEitherOps[ServiceErrorModel, Unit](submissionConnector.refreshPensionsResponse(nino, mtditid, taxYear))
    } yield result).value

  def deleteUserPensionChargesData(nino: String, mtditid: String, taxYear: Int)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Either[ServiceErrorModel, Unit]] =
    (for {
      _      <- FutureEitherOps[ServiceErrorModel, Unit](chargesConnector.deletePensionCharges(nino, taxYear))
      result <- FutureEitherOps[ServiceErrorModel, Unit](submissionConnector.refreshPensionsResponse(nino, mtditid, taxYear))
    } yield result).value

}
