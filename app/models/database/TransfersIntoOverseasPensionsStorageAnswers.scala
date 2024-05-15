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

package models.database

import models.frontend.TransfersIntoOverseasPensionsAnswers
import play.api.libs.json.{Json, OFormat}

final case class TransfersIntoOverseasPensionsStorageAnswers(transferPensionSavings: Option[Boolean],
                                                             overseasTransferCharge: Option[Boolean],
                                                             pensionSchemeTransferCharge: Option[Boolean])

object TransfersIntoOverseasPensionsStorageAnswers {
  implicit val format: OFormat[TransfersIntoOverseasPensionsStorageAnswers] = Json.format[TransfersIntoOverseasPensionsStorageAnswers]

  def fromJourneyAnswers(answers: TransfersIntoOverseasPensionsAnswers): TransfersIntoOverseasPensionsStorageAnswers =
    TransfersIntoOverseasPensionsStorageAnswers(
      answers.transferPensionSavings,
      answers.overseasTransferCharge,
      answers.pensionSchemeTransferCharge
    )
}
