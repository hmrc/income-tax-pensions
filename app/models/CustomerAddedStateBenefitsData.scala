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

package models

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, OWrites, Reads}
import utils.JsonUtils.jsonObjNoNulls

case class CustomerAddedStateBenefitsData(
    statePensions: Option[Set[CustomerAddedStateBenefit]] = None,
    statePensionLumpSums: Option[Set[CustomerAddedStateBenefit]] = None
)

object CustomerAddedStateBenefitsData {

  implicit val customerAddedStateBenefitsDataWrites: OWrites[CustomerAddedStateBenefitsData] = (data: CustomerAddedStateBenefitsData) =>
    jsonObjNoNulls(
      "statePension"        -> data.statePensions,
      "statePensionLumpSum" -> data.statePensionLumpSums
    )

  implicit val customerAddedStateBenefitsDataReads: Reads[CustomerAddedStateBenefitsData] = (
    (JsPath \ "statePension").readNullable[Set[CustomerAddedStateBenefit]] and
      (JsPath \ "statePensionLumpSum").readNullable[Set[CustomerAddedStateBenefit]]
  )(CustomerAddedStateBenefitsData.apply _)
}
