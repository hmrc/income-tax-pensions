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

package models.logging

import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}

object HeaderCarrierExtensions {
  val CorrelationIdHeaderKey = "CorrelationId"

  implicit class HeaderCarrierOps(val headerCarrier: HeaderCarrier) extends AnyVal {
    def withCorrelationId(correlationId: String): HeaderCarrier =
      addIfMissing(CorrelationIdHeaderKey, correlationId)

    def maybeCorrelationId: Option[String] = headerCarrier.otherHeaders.collectFirst {
      case (key, value) if key == CorrelationIdHeaderKey => value
    }

    def correlationId: String = maybeCorrelationId.getOrElse("unknown")

    def toExplicitHeaders: Seq[(String, String)] =
      Seq(
        HeaderNames.xRequestId            -> headerCarrier.requestId.map(_.value),
        HeaderNames.xSessionId            -> headerCarrier.sessionId.map(_.value),
        HeaderNames.xForwardedFor         -> headerCarrier.forwarded.map(_.value),
        HeaderNames.xRequestChain         -> Some(headerCarrier.requestChain.value),
        HeaderNames.authorisation         -> headerCarrier.authorization.map(_.value),
        HeaderNames.trueClientIp          -> headerCarrier.trueClientIp,
        HeaderNames.trueClientPort        -> headerCarrier.trueClientPort,
        HeaderNames.googleAnalyticTokenId -> headerCarrier.gaToken,
        HeaderNames.googleAnalyticUserId  -> headerCarrier.gaUserId,
        HeaderNames.deviceID              -> headerCarrier.deviceID,
        HeaderNames.akamaiReputation      -> headerCarrier.akamaiReputation.map(_.value),
        CorrelationIdHeaderKey            -> maybeCorrelationId
      ).collect { case (k, Some(v)) => (k, v) }

    private def addIfMissing(headerKey: String, headerValue: String): HeaderCarrier = {
      val extraHeaders = headerCarrier.extraHeaders
      if (headerCarrier.extraHeaders.map(_._1).map(_.toLowerCase()).contains(headerKey.toLowerCase))
        headerCarrier
      else {
        val updatedExtraHeaders = (headerKey, headerValue) :: extraHeaders.toList
        headerCarrier.withExtraHeaders(updatedExtraHeaders: _*)
      }
    }
  }
}
