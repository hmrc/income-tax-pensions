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

import cats.data.EitherT
import cats.implicits.{catsSyntaxOptionId, none}
import connectors._
import models._
import models.employment.AllEmploymentData
import models.submission.EmploymentPensions
import uk.gov.hmrc.http.HeaderCarrier
import utils.HeaderCarrierUtils.HeaderCarrierOps

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PensionsService @Inject() (reliefsConnector: PensionReliefsConnector,
                                 chargesConnector: PensionChargesConnector,
                                 stateBenefitsConnector: GetStateBenefitsConnector,
                                 pensionIncomeConnector: PensionIncomeConnector,
                                 employmentsConnector: EmploymentsConnector) {

  // TODO: Decide whether loading employments and state benefits through pensions is what we want. The submissions service
  //       (aka "the cache") already loads employments and state benefits so adding the calls to load through pensions
  //       duplicates the data in the cache.
  def getAllPensionsData(nino: String, taxYear: Int, mtditid: String)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Either[ServiceErrorModel, AllPensionsData]] =
    (for {
      reliefsData       <- EitherT(getReliefs(nino, taxYear))
      chargesData       <- EitherT(getCharges(nino, taxYear))
      stateBenefitsData <- EitherT(getStateBenefits(nino, taxYear, mtditid))
      pensionIncomeData <- EitherT(getPensionIncome(nino, taxYear, mtditid))
      employmentData    <- EitherT(getEmployments(nino, taxYear, mtditid))
    } yield AllPensionsData(
      pensionReliefs = reliefsData,
      pensionCharges = chargesData,
      stateBenefits = stateBenefitsData,
      employmentPensions = employmentData.fold(none[EmploymentPensions])(EmploymentPensions.fromEmploymentResponse(_).some),
      pensionIncome = pensionIncomeData
    )).value

  private def getReliefs(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): DownstreamOutcome[Option[GetPensionReliefsModel]] =
    reliefsConnector.getPensionReliefs(nino, taxYear)

  private def getCharges(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): DownstreamOutcome[Option[GetPensionChargesRequestModel]] =
    chargesConnector.getPensionCharges(nino, taxYear)

  private def getStateBenefits(nino: String, taxYear: Int, mtditid: String)(implicit
      hc: HeaderCarrier): DownstreamOutcome[Option[AllStateBenefitsData]] =
    stateBenefitsConnector.getStateBenefits(nino, taxYear)(hc.withInternalId(mtditid))

  private def getPensionIncome(nino: String, taxYear: Int, mtditid: String)(implicit
      hc: HeaderCarrier): DownstreamOutcome[Option[GetPensionIncomeModel]] =
    pensionIncomeConnector.getPensionIncome(nino, taxYear)(hc.withInternalId(mtditid))

  private def getEmployments(nino: String, taxYear: Int, mtditid: String)(implicit hc: HeaderCarrier): DownstreamOutcome[Option[AllEmploymentData]] =
    employmentsConnector.getEmployments(nino, taxYear)(hc.withInternalId(mtditid))

}
