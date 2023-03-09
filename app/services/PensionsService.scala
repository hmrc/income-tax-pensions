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
                                pensionIncomeConnector: PensionIncomeConnector,
                                submissionConnector: SubmissionConnector
                               ) {

  val mtditidHeader = "mtditid"

  def getAllPensionsData(nino: String, taxYear: Int, mtditid: String)
                        (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[DesErrorModel, AllPensionsData]] = {
    (for {
      reliefsData <- FutureEitherOps[DesErrorModel, Option[GetPensionReliefsModel]](getReliefs(nino, taxYear)(hc))
      pensionData <- FutureEitherOps[DesErrorModel, Option[GetPensionChargesRequestModel]](getCharges(nino, taxYear)(hc))
//      stateBenefitsData <- FutureEitherOps[DesErrorModel, Option[GetStateBenefitsModel]](getStateBenefits(nino, taxYear, mtditid)(hc))
      pensionIncomeData <- FutureEitherOps[DesErrorModel, Option[GetPensionIncomeModel]](getPensionIncome(nino, taxYear, mtditid)(hc))

    } yield {
      AllPensionsData(
        pensionReliefs = reliefsData,
        pensionCharges = pensionData,
        stateBenefits = None, //Temporarily returning None for stateBenefitsData until migration from income-tax-benefits to income-tax-state-beneifts is completed
        pensionIncome = pensionIncomeData
      )
    }).value
  }

  def saveUserPensionChargesData(nino: String, mtditid: String, taxYear: Int, userData: CreateUpdatePensionChargesRequestModel)
                                (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ServiceErrorModel, Unit]] = {

    (for {
      pensionCharges <- FutureEitherOps[ServiceErrorModel, Option[GetPensionChargesRequestModel]](getCharges(nino, taxYear)(hc))
      mergedChanges = createUpdateRequest(pensionCharges, userData)
      _ <- FutureEitherOps[ServiceErrorModel, Unit](chargesConnector.createUpdatePensionCharges(nino, taxYear, mergedChanges))
      result <- FutureEitherOps[ServiceErrorModel, Unit](submissionConnector.refreshPensionsResponse(nino, mtditid, taxYear))
    } yield {
      result
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

  private def createUpdateRequest(currentModel: Option[GetPensionChargesRequestModel], updatedModel: CreateUpdatePensionChargesRequestModel) = {
    CreateUpdatePensionChargesRequestModel(
      pensionSavingsTaxCharges = currentOrUpdated(currentModel.flatMap(_.pensionSavingsTaxCharges),
        updatedModel.pensionSavingsTaxCharges),
      pensionSchemeOverseasTransfers = currentOrUpdated(currentModel.flatMap(_.pensionSchemeOverseasTransfers),
        updatedModel.pensionSchemeOverseasTransfers),
      pensionSchemeUnauthorisedPayments = currentOrUpdated(currentModel.flatMap(_.pensionSchemeUnauthorisedPayments),
        updatedModel.pensionSchemeUnauthorisedPayments),
      pensionContributions = currentOrUpdated(currentModel.flatMap(_.pensionContributions), updatedModel.pensionContributions),
      overseasPensionContributions = currentOrUpdated(currentModel.flatMap(_.overseasPensionContributions),
        updatedModel.overseasPensionContributions)
    )
  }

  private def currentOrUpdated[T](current: Option[T], updated: Option[T]): Option[T] = {
    updated.fold(current)(x => Some(x))
  }
}
