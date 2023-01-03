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

import connectors.PensionReliefsConnector
import connectors.httpParsers.CreateOrAmendPensionReliefsHttpParser.CreateOrAmendPensionReliefsResponse
import connectors.httpParsers.DeletePensionReliefsHttpParser.DeletePensionReliefsResponse
import connectors.httpParsers.GetPensionReliefsHttpParser.GetPensionReliefsResponse
import models.CreateOrUpdatePensionReliefsModel
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

class PensionReliefsService @Inject()(connector: PensionReliefsConnector) {

  def getPensionReliefs(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[GetPensionReliefsResponse] =
    connector.getPensionReliefs(nino, taxYear)

  def createOrAmendPensionReliefs(nino: String, taxYear: Int,
                                  pensionReliefs: CreateOrUpdatePensionReliefsModel)
                                 (implicit hc: HeaderCarrier): Future[CreateOrAmendPensionReliefsResponse] = {

    connector.createOrAmendPensionReliefs(nino, taxYear, pensionReliefs)
  }

  def deletePensionReliefs(nino: String, taxYear: Int)(implicit hc: HeaderCarrier): Future[DeletePensionReliefsResponse] =
    connector.deletePensionReliefs(nino, taxYear)
}
