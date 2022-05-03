/*
 * Copyright 2022 HM Revenue & Customs
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

package connectors.httpParsers

import connectors.httpParsers.GetEmploymentsHttpParser.GetEmploymentsHttpReads
import models.{EmploymentPensionModel, ErrorModel, GetEmploymentPensionsModel}
import play.api.http.Status
import uk.gov.hmrc.http.HttpResponse
import utils.TestUtils

class GetEmploymentsHttpParserSpec extends TestUtils {

  "The GetEmploymentsHttpParser" when {

    "a full model is returned from employments BE" should {

      "parse the GetEmploymentPensionsModel correctly" in {

        val employmentBody: String = {
          """
            |{
            |  "hmrcEmploymentData":[
            |    {
            |      "employmentId":"1234567890",
            |      "employerName":"HMRC pensions scheme",
            |      "employerRef":"Some HMRC ref",
            |      "payrollId":"Payroll ID",
            |      "startDate":"very early",
            |      "cessationDate":"too late",
            |      "dateIgnored":"ignoredField",
            |      "submittedOn":"ignoredField",
            |      "employmentData":{
            |        "occPen":true,
            |        "pay":{
            |           "taxablePayToDate":"127000",
            |           "totalTaxToDate":"450"
            |        }
            |      }
            |    },
            |    {
            |      "employmentId":"1234567891",
            |      "employerName":"Extra pensions scheme",
            |      "employerRef":"Some other HMRC ref",
            |      "payrollId":"ignoredField",
            |      "startDate":"ignoredField",
            |      "cessationDate":"ignoredField",
            |      "dateIgnored":"ignoredField",
            |      "submittedOn":"ignoredField",
            |      "employmentData":{
            |        "submittedOn":"ignoredField",
            |        "employmentSequenceNumber":"ignoredField",
            |        "companyDirector":"ignoredField",
            |        "closeCompany":"ignoredField",
            |        "directorshipCeasedDate":"ignoredField",
            |        "occPen":false,
            |        "disguisedRemuneration":"ignoredField",
            |        "pay":{
            |           "taxablePayToDate":"127001"
            |        }
            |      }
            |    }
            |  ],
            |  "customerEmploymentData":[
            |    {
            |      "employmentId":"1234567892",
            |      "employerName":"Customer pension scheme",
            |      "employerRef":"Some customer ref",
            |      "payrollId":"ignoredField",
            |      "startDate":"ignoredField",
            |      "cessationDate":"ignoredField",
            |      "dateIgnored":"ignoredField",
            |      "submittedOn":"ignoredField",
            |      "employmentData":{
            |        "submittedOn":"ignoredField",
            |        "employmentSequenceNumber":"ignoredField",
            |        "companyDirector":"ignoredField",
            |        "closeCompany":"ignoredField",
            |        "directorshipCeasedDate":"ignoredField",
            |        "disguisedRemuneration":"ignoredField",
            |        "pay":{
            |           "taxablePayToDate":"129000",
            |           "totalTaxToDate":"470",
            |           "payFrequency":"ignoredField"
            |        }
            |      }
            |    }
            |  ],
            |  "customerExpenses": {
            |      "submittedOn":"someDate"
            |  }
            |}
            |""".stripMargin
        }

        val expectedResult =
          GetEmploymentPensionsModel(
            Seq(
              EmploymentPensionModel(
                "1234567890",
                "HMRC pensions scheme",
                Some("Some HMRC ref"),
                Some("Payroll ID"),
                Some("very early"),
                Some("too late"),
                Some(BigDecimal(127000)),
                Some(BigDecimal(450)),
                Some(true))
            ),
            Seq()
          )

        GetEmploymentsHttpReads.read("", "",
          HttpResponse(Status.OK, employmentBody)) mustBe Right(Some(expectedResult))
      }
    }

    "a minimal model is returned from employment BE" should {

      "return the minimal data when occPen is set to true" in {

        val employmentBody: String = {
          """
            |{
            |  "hmrcEmploymentData":[
            |    {
            |      "employmentId":"1234567890",
            |      "employerName":"HMRC pensions scheme",
            |      "employmentData":{
            |        "occPen":true
            |      }
            |    }
            |  ],
            |  "customerEmploymentData":[]
            |}
            |""".stripMargin
        }

        val expectedResult =
          GetEmploymentPensionsModel(
            Seq(
              EmploymentPensionModel("1234567890", "HMRC pensions scheme", None, None, None, None, None, None, Some(true))
            ),
            Seq()
          )

        GetEmploymentsHttpReads.read("", "",
          HttpResponse(Status.OK, employmentBody)) mustBe Right(Some(expectedResult))

      }

      "return empty data when occPen is not set to true" in {

        val employmentBody: String = {
          """
            |{
            |  "hmrcEmploymentData":[
            |    {
            |      "employmentId":"1234567890",
            |      "employerName":"HMRC pensions scheme"
            |    }
            |  ],
            |  "customerEmploymentData":[]
            |}
            |""".stripMargin
        }

        val expectedResult = GetEmploymentPensionsModel(Seq(), Seq())

        GetEmploymentsHttpReads.read("", "",
          HttpResponse(Status.OK, employmentBody)) mustBe Right(Some(expectedResult))

      }
    }

    "return an Invalid Json error" when {

      "there are missing required fields in the returned json" in {

        val employmentBody: String = {
          """
            |{
            |  "hmrcEmploymentData":[
            |    {
            |      "employerName":"HMRC pensions scheme"
            |    }
            |  ],
            |  "customerEmploymentData":[]
            |}
            |""".stripMargin
        }

        val expectedResult = Left(ErrorModel(500, "Invalid Json"))

        GetEmploymentsHttpReads.read("", "",
          HttpResponse(Status.OK, employmentBody)) mustBe expectedResult
      }
    }

    "return an empty response" when {

      "NO CONTENT is returned from employment BE" in {
        GetEmploymentsHttpReads.read("", "",
          HttpResponse(Status.NO_CONTENT, "")) mustBe Right(None)
      }
    }

    "return an error model response" when {

      "a status other then 200, 201 is returned from employment BE" in {
        GetEmploymentsHttpReads.read("", "",
          HttpResponse(Status.BAD_REQUEST, "")) mustBe
          Left(ErrorModel(Status.BAD_REQUEST, "Error returned when attempting to retrieve employment details"))
      }
    }
  }

}
