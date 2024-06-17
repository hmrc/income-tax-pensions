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

package models.commonTaskList

import play.api.libs.json.{Json, OWrites, Reads}

trait SectionTitle extends Enumerable.Implicits

object SectionTitle extends SectionTitle {

  case class PensionsTitle() extends WithName("Pensions") with SectionTitle
  object PensionsTitle {
    implicit val nonStrictReads: Reads[PensionsTitle] = Reads.pure(PensionsTitle())
    implicit val writes: OWrites[PensionsTitle]       = OWrites[PensionsTitle](_ => Json.obj())
  }

  case class PaymentsIntoPensionsTitle() extends WithName("PaymentsIntoPensions") with SectionTitle
  object PaymentsIntoPensionsTitle {
    // implicit val format: OFormat[PaymentsIntoPensionsTitle] = Json.format[PaymentsIntoPensionsTitle]
    implicit val nonStrictReads: Reads[PaymentsIntoPensionsTitle] = Reads.pure(PaymentsIntoPensionsTitle())
    implicit val writes: OWrites[PaymentsIntoPensionsTitle]       = OWrites[PaymentsIntoPensionsTitle](_ => Json.obj())
  }

  val values: Seq[SectionTitle] = Seq(
    PensionsTitle(),
    PaymentsIntoPensionsTitle()
  )

  implicit val enumerable: Enumerable[SectionTitle] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
