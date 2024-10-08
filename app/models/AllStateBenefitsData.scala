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

import connectors.OptionalContentHttpReads
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, OWrites, Reads}
import utils.JsonUtils.jsonObjNoNulls

import java.time.Instant

case class AllStateBenefitsData(stateBenefitsData: Option[StateBenefitsData],
                                customerAddedStateBenefitsData: Option[CustomerAddedStateBenefitsData] = None) {
  def latestSubmittedByHMRC: Boolean =
    AllStateBenefitsData.lastSubmittedByHMRC(
      stateBenefitsData.flatMap(_.submittedOn),
      customerAddedStateBenefitsData.flatMap(_.submittedOn)
    )
}

object AllStateBenefitsData {
  val empty: AllStateBenefitsData = AllStateBenefitsData(None, None)

  def lastSubmittedByHMRC(hmrcSubmittedOn: Option[Instant], customerSubmittedOn: Option[Instant]): Boolean =
    hmrcSubmittedOn.exists { h =>
      h.isAfter(customerSubmittedOn.getOrElse(Instant.MIN))
    }

  implicit val allStateBenefitsDataWrites: OWrites[AllStateBenefitsData] = (data: AllStateBenefitsData) =>
    jsonObjNoNulls(
      "stateBenefits"              -> data.stateBenefitsData,
      "customerAddedStateBenefits" -> data.customerAddedStateBenefitsData
    )

  implicit val allStateBenefitsDataReads: Reads[AllStateBenefitsData] = (
    (JsPath \ "stateBenefits").readNullable[StateBenefitsData] and
      (JsPath \ "customerAddedStateBenefits").readNullable[CustomerAddedStateBenefitsData]
  )(AllStateBenefitsData.apply _)

  implicit val optRds: OptionalContentHttpReads[AllStateBenefitsData] = new OptionalContentHttpReads[AllStateBenefitsData]
}
