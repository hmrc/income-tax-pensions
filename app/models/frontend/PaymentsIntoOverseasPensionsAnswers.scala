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

package models.frontend

import models.database.{PaymentsIntoOverseasPensionsStorageAnswer, PaymentsIntoOverseasPensionsStorageAnswers}
import models.domain.PensionAnswers
import models.{GetPensionIncomeModel, GetPensionReliefsModel}
import play.api.libs.json.{Json, OFormat}

case class PaymentsIntoOverseasPensionsAnswers(paymentsIntoOverseasPensionsQuestions: Option[Boolean] = None,
                                               paymentsIntoOverseasPensionsAmount: Option[BigDecimal] = None,
                                               employerPaymentsQuestion: Option[Boolean] = None,
                                               taxPaidOnEmployerPaymentsQuestion: Option[Boolean] = None,
                                               schemes: List[OverseasPensionScheme] = List.empty[OverseasPensionScheme])
    extends PensionAnswers {
  def isFinished: Boolean =
    paymentsIntoOverseasPensionsQuestions.exists(x =>
      if (!x) {
        true
      } else {
        paymentsIntoOverseasPensionsAmount.isDefined &&
        employerPaymentsQuestion.exists(x =>
          if (!x) true else taxPaidOnEmployerPaymentsQuestion.exists(x => if (x) true else schemes.nonEmpty && schemes.forall(_.isFinished)))
      })

  def toStorageAnswers: PaymentsIntoOverseasPensionsStorageAnswer =
    PaymentsIntoOverseasPensionsStorageAnswer(paymentsIntoOverseasPensionsQuestions, employerPaymentsQuestion, taxPaidOnEmployerPaymentsQuestion)
}

object PaymentsIntoOverseasPensionsAnswers {
  implicit val format: OFormat[PaymentsIntoOverseasPensionsAnswers] = Json.format[PaymentsIntoOverseasPensionsAnswers]

  def mkAnswers(maybeReliefsAnswers: Option[GetPensionReliefsModel],
                maybeIncomeAnswers: Option[GetPensionIncomeModel],
                maybeDbAnswers: Option[PaymentsIntoOverseasPensionsStorageAnswers]): Option[PaymentsIntoOverseasPensionsAnswers] =
    maybeDbAnswers
      .getOrElse(PaymentsIntoOverseasPensionsStorageAnswers.empty)
      .toPaymentsIntoOverseasPensionsAnswers(maybeIncomeAnswers, maybeReliefsAnswers)
}
