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
