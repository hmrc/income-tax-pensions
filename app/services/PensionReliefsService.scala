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
import connectors.PensionReliefsConnector
import connectors.httpParsers.GetPensionReliefsHttpParser.GetPensionReliefsResponse
import models.{CreateOrUpdatePensionReliefsModel, ServiceErrorModel}
import repositories.JourneyAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureEitherOps

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

trait PensionReliefsService {
  def getPensionReliefs(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionReliefsResponse]
  def saveUserPensionReliefsData(nino: String, mtditid: String, taxYear: Int, userData: CreateOrUpdatePensionReliefsModel)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Either[ServiceErrorModel, Unit]]
  def deleteUserPensionReliefsData(nino: String, mtditid: String, taxYear: Int)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Either[ServiceErrorModel, Unit]]
}

@Singleton
class PensionReliefsServiceImpl @Inject() (reliefsConnector: PensionReliefsConnector, repository: JourneyAnswersRepository)
    extends PensionReliefsService {

  def getPensionReliefs(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionReliefsResponse] =
    reliefsConnector.getPensionReliefs(nino, taxYear)

  def saveUserPensionReliefsData(nino: String, mtditid: String, taxYear: Int, userData: CreateOrUpdatePensionReliefsModel)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Either[ServiceErrorModel, Unit]] =
//    val ctx = JourneyContext(TaxYear(taxYear), Mtditid(mtditid), Journey.PaymentsIntoPensions)
    // TODO Uncomment when implementing FE (pass journey name somehow, in the URL?)
    (for {
      _ <- EitherT(reliefsConnector.createOrAmendPensionReliefs(nino, taxYear, userData))
//      _      <- repository.upsertAnswers(ctx, Json.obj())
      // TODO Uncomment when implementing FE (pass journey name somehow, in the URL?)
    } yield ()).value

  def deleteUserPensionReliefsData(nino: String, mtditid: String, taxYear: Int)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext): Future[Either[ServiceErrorModel, Unit]] =
    FutureEitherOps[ServiceErrorModel, Unit](reliefsConnector.deletePensionReliefs(nino, taxYear)).value
}
