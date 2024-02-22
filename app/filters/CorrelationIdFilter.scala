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

package filters

import akka.stream.Materializer
import filters.CorrelationIdFilter.{addCorrelationIdToMdc, clearCorrelationIdMdc}
import models.logging.CorrelationId.{RequestHeaderOps, RequestOps, ResultOps}
import models.logging.HeaderCarrierExtensions.CorrelationIdHeaderKey
import org.slf4j.MDC
import play.api.Logging
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

class CorrelationIdFilter @Inject() (implicit val mat: Materializer, ec: ExecutionContext) extends Filter with Logging {

  def apply(nextFilter: RequestHeader => Future[Result])(originalRequestHeader: RequestHeader): Future[Result] = {
    val (updatedRequestHeader, correlationId) = originalRequestHeader.withCorrelationId()
    addCorrelationIdToMdc(correlationId)

    logger.debug(
      s"CorrelationId saved into MDC ${MDC.get(CorrelationIdHeaderKey)} and RequestHeader: ${updatedRequestHeader.headers.get(CorrelationIdHeaderKey)}")

    val futureResult = nextFilter(updatedRequestHeader).map { result =>
      result.withCorrelationId(correlationId)
    }

    futureResult.onComplete { _ =>
      clearCorrelationIdMdc()
    }

    futureResult
  }

}

object CorrelationIdFilter {
  def clearCorrelationIdMdc(): Unit =
    MDC.remove(CorrelationIdHeaderKey)

  def addCorrelationIdToMdc(correlationId: String): Unit =
    MDC.put(CorrelationIdHeaderKey, correlationId)

  def addCorrelationIdToMdc(request: Request[AnyContent]): Request[AnyContent] = {
    val (updatedRequestHeader, correlationId) = RequestOps.withCorrelationId(request)
    MDC.put(CorrelationIdHeaderKey, correlationId)
    updatedRequestHeader
  }

}
