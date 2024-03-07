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
import connectors.{PensionIncomeConnector, SubmissionConnector}
import connectors.httpParsers.GetPensionIncomeHttpParser.GetPensionIncomeResponse
import models.{CreateUpdatePensionIncomeModel, ServiceErrorModel}
import uk.gov.hmrc.http.HeaderCarrier
import cats.implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PensionIncomeService @Inject() (connector: PensionIncomeConnector, submissionConnector: SubmissionConnector)(implicit ec: ExecutionContext) {

  def getPensionIncome(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionIncomeResponse] =
    connector.getPensionIncome(nino, taxYear)

  def savePensionIncomeSessionData(nino: String, taxYear: Int, mtditid: String, pensionIncome: CreateUpdatePensionIncomeModel)(implicit
      hc: HeaderCarrier): Future[Either[ServiceErrorModel, Unit]] =
    (for {
      _   <- EitherT(connector.createOrAmendPensionIncome(nino, taxYear, pensionIncome))
      res <- EitherT(submissionConnector.refreshPensionsResponse(nino, mtditid, taxYear))
    } yield res).value

  def deletePensionIncomeSessionData(nino: String, taxYear: Int, mtditid: String)(implicit
      hc: HeaderCarrier): Future[Either[ServiceErrorModel, Unit]] =
    (for {
      _   <- EitherT(connector.deletePensionIncome(nino, taxYear))
      res <- EitherT(submissionConnector.refreshPensionsResponse(nino, mtditid, taxYear))
    } yield res).value
}
