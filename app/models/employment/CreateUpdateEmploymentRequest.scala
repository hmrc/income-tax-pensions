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

package models.employment

import models.employment.CreateUpdateEmploymentRequest.{CreateUpdateEmployment, CreateUpdateEmploymentData}
import models.frontend.ukpensionincome.SingleUkPensionIncomeAnswers
import play.api.libs.json.{Json, OFormat}

case class CreateUpdateEmploymentRequest(
    employmentId: Option[String],
    employment: Option[CreateUpdateEmployment],
    employmentData: Option[CreateUpdateEmploymentData],
    isHmrcEmploymentId: Option[Boolean]
)

object CreateUpdateEmploymentRequest {
  implicit val format: OFormat[CreateUpdateEmploymentRequest] = Json.format[CreateUpdateEmploymentRequest]

  def fromAnswers(answers: SingleUkPensionIncomeAnswers): CreateUpdateEmploymentRequest =
    CreateUpdateEmploymentRequest(
      employmentId = answers.employmentId,
      employment = Some(
        CreateUpdateEmployment(
          employerRef = answers.pensionSchemeRef,
          employerName = answers.pensionSchemeName.getOrElse(""),
          startDate = answers.startDate.getOrElse(""),
          cessationDate = answers.endDate,
          payrollId = answers.pensionId
        )
      ),
      employmentData = Some(
        CreateUpdateEmploymentData(
          PayModel(
            taxablePayToDate = answers.amount.getOrElse(0.00),
            totalTaxToDate = answers.taxPaid.getOrElse(0.00)
          ))
      ),
      isHmrcEmploymentId = answers.isCustomerEmploymentData.map(!_)
    )

  case class CreateUpdateEmployment(
      employerRef: Option[String],
      employerName: String,
      startDate: String,
      cessationDate: Option[String],
      payrollId: Option[String]
  )
  object CreateUpdateEmployment {
    implicit val format: OFormat[CreateUpdateEmployment] = Json.format[CreateUpdateEmployment]
  }

  case class CreateUpdateEmploymentData(pay: PayModel)
  object CreateUpdateEmploymentData {
    implicit val formats: OFormat[CreateUpdateEmploymentData] = Json.format[CreateUpdateEmploymentData]
  }

  case class PayModel(
      taxablePayToDate: BigDecimal,
      totalTaxToDate: BigDecimal
  )
  object PayModel {
    implicit val formats: OFormat[PayModel] = Json.format[PayModel]
  }

  case class CreatedEmployment(employmentId: Option[String])

  object CreatedEmployment {
    implicit val formats: OFormat[CreatedEmployment] = Json.format[CreatedEmployment]
  }
}
