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

package models.employment

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, OWrites, Reads}
import utils.JsonUtils.jsonObjNoNulls

import java.time.Instant

final case class OtherEmploymentIncome(submittedOn: Option[Instant] = None,
                                       shareOptions: Option[Set[ShareOption]] = None,
                                       sharesAwardedOrReceived: Option[Set[SharesAwardedOrReceived]] = None,
                                       lumpSums: Option[Set[LumpSum]] = None,
                                       disability: Option[Disability] = None,
                                       foreignService: Option[ForeignService] = None)

object OtherEmploymentIncome {
  implicit val otherEmploymentsWrites: OWrites[OtherEmploymentIncome] = (otherEmploymentIncome: OtherEmploymentIncome) =>
    jsonObjNoNulls(
      "submittedOn"             -> otherEmploymentIncome.submittedOn,
      "shareOption"             -> otherEmploymentIncome.shareOptions,
      "sharesAwardedOrReceived" -> otherEmploymentIncome.sharesAwardedOrReceived,
      "lumpSums"                -> otherEmploymentIncome.lumpSums,
      "disability"              -> otherEmploymentIncome.disability,
      "foreignService"          -> otherEmploymentIncome.foreignService
    )

  implicit val otherEmploymentsReads: Reads[OtherEmploymentIncome] = (
    (JsPath \ "submittedOn").readNullable[Instant] and
      (JsPath \ "shareOption").readNullable[Set[ShareOption]] and
      (JsPath \ "sharesAwardedOrReceived").readNullable[Set[SharesAwardedOrReceived]] and
      (JsPath \ "lumpSums").readNullable[Set[LumpSum]] and
      (JsPath \ "disability").readNullable[Disability] and
      (JsPath \ "foreignService").readNullable[ForeignService]
  )(OtherEmploymentIncome.apply _)
}
