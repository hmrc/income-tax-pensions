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

package models.frontend.ukpensionincome

import models.database.UkPensionIncomeStorageAnswers
import models.domain.PensionAnswers
import models.submission.EmploymentPensions
import play.api.libs.json.{Json, OFormat}

final case class UkPensionIncomeAnswers(
    uKPensionIncomesQuestion: Boolean,
    uKPensionIncomes: List[SingleUkPensionIncomeAnswers]
) extends PensionAnswers {
  def isFinished: Boolean =
    !uKPensionIncomesQuestion || (uKPensionIncomes.nonEmpty && uKPensionIncomes.forall(_.isFinished))
}

object UkPensionIncomeAnswers {
  implicit val format: OFormat[UkPensionIncomeAnswers] = Json.format[UkPensionIncomeAnswers]

  def mkAnswers(downstreamAnswers: EmploymentPensions, maybeDbAnswers: Option[UkPensionIncomeStorageAnswers]): Option[UkPensionIncomeAnswers] =
    downstreamAnswers.toUkPensionIncomeAnswers(maybeDbAnswers)
}
