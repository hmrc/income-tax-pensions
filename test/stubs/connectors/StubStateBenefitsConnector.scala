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

package stubs.connectors

import cats.data.EitherT
import connectors.StateBenefitsConnector
import models.AllStateBenefitsData
import models.common.{Nino, TaxYear}
import models.domain.ApiResultT
import models.error.ServiceError
import models.statebenefit.StateBenefitsUserData
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class StubStateBenefitsConnector(
    stateBenefitsResults: Option[AllStateBenefitsData] = None,
    var claims: List[StateBenefitsUserData] = Nil
) extends StateBenefitsConnector {

  def getStateBenefits(nino: Nino, taxYear: TaxYear)(implicit hc: HeaderCarrier): ApiResultT[Option[AllStateBenefitsData]] =
    EitherT.rightT[Future, ServiceError](stateBenefitsResults)

  def saveClaim(nino: Nino, model: StateBenefitsUserData)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    claims = model :: claims
    EitherT.rightT[Future, ServiceError](())
  }

  def deleteClaim(nino: Nino, taxYear: TaxYear, benefitId: UUID)(implicit hc: HeaderCarrier): ApiResultT[Unit] = {
    claims = claims.filterNot(c => c.claim.exists(_.benefitId.contains(benefitId)))
    EitherT.rightT[Future, ServiceError](())
  }
}
