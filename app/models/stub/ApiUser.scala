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

package models.stub

import play.api.libs.json.Json

final case class APIUser(nino: String,
                         dividends: List[String] = Nil,
                         dividendsIncome: List[String] = Nil,
                         interest: List[String] = Nil,
                         savingsIncome: List[String] = Nil,
                         giftAid: List[String] = Nil,
                         employment: List[String] = Nil,
                         pensions: List[String] = Nil,
                         cis: List[String] = Nil,
                         stateBenefits: List[String] = Nil,
                         insurancePolicies: List[String] = Nil,
                         selfEmployment: List[String] = Nil,
                         otherEmploymentsIncome: List[String] = Nil,
                         property: List[String] = Nil)

object APIUser {
  implicit val format = Json.format[APIUser]
}
