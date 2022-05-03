/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.httpParsers.GetEmploymentsHttpParser.GetEmploymentsResponse
import connectors.httpParsers.GetPensionChargesHttpParser.GetPensionChargesResponse
import connectors.httpParsers.GetPensionReliefsHttpParser.GetPensionReliefsResponse
import connectors.httpParsers.GetStateBenefitsHttpParser.GetStateBenefitsResponse
import connectors.{EmploymentConnector, GetStateBenefitsConnector, PensionChargesConnector, PensionReliefsConnector}
import models.{AllPensionsData, DesErrorModel, GetEmploymentPensionsModel, GetPensionChargesRequestModel, GetPensionReliefsModel, GetStateBenefitsModel}
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureEitherOps
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

class PensionsService @Inject()(reliefsConnector: PensionReliefsConnector,
                                chargesConnector: PensionChargesConnector,
                                stateBenefitsConnector: GetStateBenefitsConnector,
                                employmentConnector: EmploymentConnector) {

  def getAllPensionsData(nino: String, taxYear: Int, mtditid: String)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[DesErrorModel, AllPensionsData]] = {
    (for {
      reliefsData <- FutureEitherOps[DesErrorModel, Option[GetPensionReliefsModel]](getReliefs(nino, taxYear)(hc))
      pensionData <- FutureEitherOps[DesErrorModel, Option[GetPensionChargesRequestModel]](getCharges(nino, taxYear)(hc))
      stateBenefitsData <- FutureEitherOps[DesErrorModel, Option[GetStateBenefitsModel]](getStateBenefits(nino, taxYear, mtditid)(hc))
      employmentsData <- FutureEitherOps[DesErrorModel, Option[GetEmploymentPensionsModel]](getEmploymentData(nino, taxYear)(hc))
    } yield {
      AllPensionsData(
        pensionReliefs = reliefsData,
        pensionCharges = pensionData,
        stateBenefits = stateBenefitsData,
        employmentPensions = employmentsData
      )
    }).value
  }


  private def getReliefs(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionReliefsResponse] =
    reliefsConnector.getPensionReliefs(nino, taxYear)

  private def getCharges(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionChargesResponse] =
    chargesConnector.getPensionCharges(nino, taxYear)

  private def getStateBenefits(nino: String, taxYear: Int, mtditid: String)(implicit hc: HeaderCarrier): Future[GetStateBenefitsResponse] =
    stateBenefitsConnector.getStateBenefits(nino, taxYear)(hc.withExtraHeaders("mtditid" -> mtditid))

  private def getEmploymentData(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetEmploymentsResponse] =
    employmentConnector.getEmploymentPensions(nino, taxYear)
}
