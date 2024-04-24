import models.domain.ApiResultT
import models.error.ServiceError
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results._

import scala.concurrent.{ExecutionContext, Future}

package object controllers {

  def handleOptionalApiResult[A: Writes](result: ApiResultT[Option[A]])(implicit ec: ExecutionContext, logger: Logger): Future[Result] = {
    val resultT = result.map(r => r.fold(NoContent)(_ => Ok(Json.toJson(r))))
    handleResultT(resultT)
  }

  def handleApiResultT[A: Writes](result: ApiResultT[A])(implicit ec: ExecutionContext, logger: Logger): Future[Result] = {
    val resultT = result.map(r => Ok(Json.toJson(r)))
    handleResultT(resultT)
  }

  def handleApiUnitResultT(result: ApiResultT[Unit])(implicit ec: ExecutionContext, logger: Logger): Future[Result] = {
    val resultT = result.map(_ => NoContent)
    handleResultT(resultT)
  }

  def handleResultT(result: ApiResultT[Result])(implicit ec: ExecutionContext, logger: Logger): Future[Result] =
    result.leftMap { error =>
      handleError(error)
    }.merge

  private def handleError(error: ServiceError)(implicit logger: Logger) = {
    logger.error(s"HttpError encountered: ${error.errorMessage}")
    InternalServerError(error.errorMessage)
  }
}
