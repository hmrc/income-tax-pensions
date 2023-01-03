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

import connectors.PensionChargesConnector
import connectors.httpParsers.CreateUpdatePensionChargesHttpParser.CreateUpdatePensionChargesResponse
import connectors.httpParsers.DeletePensionChargesHttpParser.DeletePensionChargesResponse
import connectors.httpParsers.GetPensionChargesHttpParser.GetPensionChargesResponse
import models.CreateUpdatePensionChargesRequestModel
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

class PensionChargesService @Inject()(connector: PensionChargesConnector) {

  def getPensionCharges(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionChargesResponse] =
    connector.getPensionCharges(nino, taxYear)

  def deletePensionCharges(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[DeletePensionChargesResponse] =
    connector.deletePensionCharges(nino, taxYear)

  def createUpdatePensionCharges(nino: String, taxYear: Int, model: CreateUpdatePensionChargesRequestModel)
                                (implicit hc: HeaderCarrier): Future[CreateUpdatePensionChargesResponse] =
    connector.createUpdatePensionCharges(nino, taxYear, model)


}
