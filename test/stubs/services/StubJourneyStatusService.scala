package stubs.services

import cats.data.EitherT
import models.common._
import models.domain.ApiResultT
import models.error.ServiceError
import services.JourneyStatusService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class StubJourneyStatusService() extends JourneyStatusService {

  def getAllStatuses(taxYear: TaxYear, mtditid: Mtditid)(implicit hc: HeaderCarrier): ApiResultT[List[JourneyNameAndStatus]] =
    EitherT.rightT[Future, ServiceError](List.empty[JourneyNameAndStatus])

  def getJourneyStatus(ctx: JourneyContext): ApiResultT[List[JourneyNameAndStatus]] =
    EitherT.rightT[Future, ServiceError(List.empty[JourneyNameAndStatus])

  def saveJourneyStatus(ctx: JourneyContext, journeyStatus: JourneyStatus): ApiResultT[Unit] =
    EitherT.rightT[Future, ServiceError(())
}
