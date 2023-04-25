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

import connectors.httpParsers.GetPensionChargesHttpParser.GetPensionChargesResponse
import connectors.httpParsers.GetPensionIncomeHttpParser.GetPensionIncomeResponse
import connectors.httpParsers.GetPensionReliefsHttpParser.GetPensionReliefsResponse
import connectors.httpParsers.GetStateBenefitsHttpParser.GetStateBenefitsResponse
import connectors._
import models._
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureEitherOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PensionsService @Inject()(reliefsConnector: PensionReliefsConnector,
                                chargesConnector: PensionChargesConnector,
                                stateBenefitsConnector: GetStateBenefitsConnector,
                                pensionIncomeConnector: PensionIncomeConnector
                               ) {

  val mtditidHeader = "mtditid"

  def getAllPensionsData(nino: String, taxYear: Int, mtditid: String)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[DesErrorModel, AllPensionsData]] = {
    (for {
      reliefsData <- FutureEitherOps[DesErrorModel, Option[GetPensionReliefsModel]](getReliefs(nino, taxYear)(hc))
      pensionData <- FutureEitherOps[DesErrorModel, Option[GetPensionChargesRequestModel]](getCharges(nino, taxYear)(hc))
      stateBenefitsData <- FutureEitherOps[DesErrorModel, Option[AllStateBenefitsData]](getStateBenefits(nino, taxYear, mtditid)(hc))
      pensionIncomeData <- FutureEitherOps[DesErrorModel, Option[GetPensionIncomeModel]](getPensionIncome(nino, taxYear, mtditid)(hc))

    } yield {
      AllPensionsData(
        pensionReliefs = reliefsData,
        pensionCharges = pensionData,
        stateBenefits = stateBenefitsData,
        pensionIncome = pensionIncomeData
      )
    }).value
  }

  private def getReliefs(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionReliefsResponse] =
    reliefsConnector.getPensionReliefs(nino, taxYear)

  private def getCharges(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionChargesResponse] =
    chargesConnector.getPensionCharges(nino, taxYear)

  private def getStateBenefits(nino: String, taxYear: Int, mtditid: String)(implicit hc: HeaderCarrier): Future[GetStateBenefitsResponse] =
    stateBenefitsConnector.getStateBenefits(nino, taxYear)(hc.withExtraHeaders(mtditidHeader -> mtditid))

  private def getPensionIncome(nino: String, taxYear: Int, mtditid: String)(implicit hc: HeaderCarrier): Future[GetPensionIncomeResponse] =
    pensionIncomeConnector.getPensionIncome(nino, taxYear)(hc.withExtraHeaders(mtditidHeader -> mtditid))

  
}
