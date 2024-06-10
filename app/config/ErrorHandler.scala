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

package config

import models.logging.HeaderCarrierExtensions.CorrelationIdHeaderKey
import org.slf4j.MDC
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import play.api.{Configuration, Logger}
import uk.gov.hmrc.auth.core.AuthorisationException
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.backend.http.{ErrorResponse, JsonErrorHandler}
import uk.gov.hmrc.play.bootstrap.config.HttpAuditEvent

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ErrorHandler @Inject() (
    auditConnector: AuditConnector,
    httpAuditEvent: HttpAuditEvent,
    configuration: Configuration
)(implicit ec: ExecutionContext)
    extends JsonErrorHandler(auditConnector, httpAuditEvent, configuration) {

  private val logger = Logger(getClass)

  import httpAuditEvent.dataEvent

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    val clientErrorLogMessage =
      s"[$CorrelationIdHeaderKey=$mdcCorrelationId] Client error occurred: $statusCode for ${request.method} [${request.uri}] - $message"

    statusCode match {
      case NOT_FOUND =>
        logger.info(clientErrorLogMessage) // May be lots of Not found errors (broken links etc. Don't want to clutter logging)
      case _ =>
        logger.error(clientErrorLogMessage)
    }

    super.onClientError(request, statusCode, message)
  }

  // Method copied from: original JsonErrorHandler to add correlationId capability
  // scalastyle:off
  override def onServerError(request: RequestHeader, ex: Throwable): Future[Result] = {
    implicit val headerCarrier: HeaderCarrier = hc(request)

    val eventType = ex match {
      case _: NotFoundException      => "ResourceNotFound"
      case _: AuthorisationException => "ClientError"
      case _: JsValidationException  => "ServerValidationError"
      case _                         => "ServerInternalError"
    }

    val errorResponse = ex match {
      case e: AuthorisationException =>
        logger.error(s"! Internal server error, [$CorrelationIdHeaderKey=$mdcCorrelationId], for (${request.method}) [${request.uri}] -> ", e)
        // message is not suppressed here since needs to be forwarded
        ErrorResponse(401, e.getMessage)

      case e: HttpException =>
        logException(e, e.responseCode)
        // message is not suppressed here since HttpException exists to define returned message
        ErrorResponse(e.responseCode, e.getMessage)

      case e: UpstreamErrorResponse =>
        logException(e, e.statusCode)
        val msg =
          if (suppress5xxErrorMessages) s"UpstreamErrorResponse: ${e.statusCode}"
          else e.getMessage
        ErrorResponse(e.reportAs, msg)

      case e: Throwable =>
        logger.error(s"! Internal server error, [$CorrelationIdHeaderKey=$mdcCorrelationId], for (${request.method}) [${request.uri}] -> ", e)
        val msg =
          if (suppress5xxErrorMessages) "Other error"
          else e.getMessage
        ErrorResponse(INTERNAL_SERVER_ERROR, msg)
    }

    auditConnector.sendEvent(
      dataEvent(
        eventType = eventType,
        transactionName = "Unexpected error",
        request = request,
        detail = Map("transactionFailureReason" -> ex.getMessage)
      )
    )
    Future.successful(new Status(errorResponse.statusCode)(Json.toJson(errorResponse)))
  }
  // scalastyle:on

  private def logException(exception: Exception, responseCode: Int): Unit = {
    val errorMessage = s"[$CorrelationIdHeaderKey=$mdcCorrelationId], exception: " + exception.getMessage

    if (upstreamWarnStatuses.contains(responseCode))
      logger.warn(errorMessage, exception)
    else
      logger.error(errorMessage, exception)
  }

  private def mdcCorrelationId: String =
    Option(MDC.get(CorrelationIdHeaderKey)).getOrElse("Unknown")
}
