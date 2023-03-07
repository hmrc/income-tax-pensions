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

package services

import com.codahale.metrics.SharedMetricRegistries
import connectors.httpParsers.CreateUpdatePensionChargesHttpParser.CreateUpdatePensionChargesResponse
import connectors.httpParsers.GetPensionChargesHttpParser.GetPensionChargesResponse
import connectors.httpParsers.GetPensionIncomeHttpParser.GetPensionIncomeResponse
import connectors.httpParsers.GetPensionReliefsHttpParser.GetPensionReliefsResponse
import connectors.httpParsers.GetStateBenefitsHttpParser.GetStateBenefitsResponse
import connectors.httpParsers.RefreshIncomeSourceHttpParser.RefreshIncomeSourceResponse
import connectors._
import models._
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class PensionsIncomeServiceSpec extends TestUtils {
  SharedMetricRegistries.clear()

  val reliefsConnector: PensionReliefsConnector = mock[PensionReliefsConnector]
  val chargesConnector: PensionChargesConnector = mock[PensionChargesConnector]
  val stateBenefitsConnector: GetStateBenefitsConnector = mock[GetStateBenefitsConnector]
  val pensionIncomeConnector: PensionIncomeConnector = mock[PensionIncomeConnector]
  val submissionConnector: SubmissionConnector = mock[SubmissionConnector]
  val service = new PensionIncomeService(pensionIncomeConnector, submissionConnector)

  val taxYear = 2022
  val nino = "AA123456A"
  val mtditid = "1234567890"

  val expectedReliefsResult: GetPensionReliefsResponse = Right(Some(fullPensionReliefsModel))
  val expectedChargesResult: GetPensionChargesResponse = Right(Some(fullPensionChargesModel))
  val expectedStateBenefitsResult: GetStateBenefitsResponse = Right(Some(fullStateBenefitsModel))
  val expectedPensionIncomeResult: GetPensionIncomeResponse = Right(Some(fullPensionIncomeModel))


  "savePensionIncomeSessionData" should {

    "return Right(unit) " should {
      "successfully update when only foreign pension is provided" in {


        val pensionIncomeModel = CreateUpdatePensionIncomeModel(
          fullPensionIncomeModel.foreignPension,
          None
        )



        (pensionIncomeConnector.createOrAmendPensionIncome(_: String, _: Int, _: CreateUpdatePensionIncomeModel)(_: HeaderCarrier))
          .expects(nino, taxYear, pensionIncomeModel, *)
          .returning(Future.successful(Right(())))

        (submissionConnector.refreshPensionsResponse(_: String, _: String, _: Int)(_: HeaderCarrier))
          .expects(nino, mtditid, taxYear, *)
          .returning(Future.successful(Right(())))

        val Right(result) = await(service.savePensionIncomeSessionData(nino, taxYear, mtditid, pensionIncomeModel))

        result mustBe ()

      }
      "successfully update when foreign pension and overseas pension contribution are not provided" in {


        val pensionIncomeModel = CreateUpdatePensionIncomeModel(
          None,
          None
        )

        (pensionIncomeConnector.createOrAmendPensionIncome(_: String, _: Int, _: CreateUpdatePensionIncomeModel)(_: HeaderCarrier))
          .expects(nino, taxYear, pensionIncomeModel, *)
          .returning(Future.successful(Right(())))

        (submissionConnector.refreshPensionsResponse(_: String, _: String, _: Int)(_: HeaderCarrier))
          .expects(nino, mtditid, taxYear, *)
          .returning(Future.successful(Right(())))

        val Right(result) = await(service.savePensionIncomeSessionData(nino, taxYear, mtditid, pensionIncomeModel))

        result mustBe (())

      }
      "successfully update when both foreign pension and overseas pension contribution are provided" in {


        val pensionIncomeModel = CreateUpdatePensionIncomeModel(
          fullPensionIncomeModel.foreignPension,
          fullPensionIncomeModel.overseasPensionContribution
        )

        (pensionIncomeConnector.createOrAmendPensionIncome(_: String, _: Int, _: CreateUpdatePensionIncomeModel)(_: HeaderCarrier))
          .expects(nino, taxYear, pensionIncomeModel, *)
          .returning(Future.successful(Right(())))

        (submissionConnector.refreshPensionsResponse(_: String, _: String, _: Int)(_: HeaderCarrier))
          .expects(nino, mtditid, taxYear, *)
          .returning(Future.successful(Right(())))

        val Right(result) = await(service.savePensionIncomeSessionData(nino, taxYear, mtditid, pensionIncomeModel))

        result mustBe (())

      }
      "successfully update when only pension contribution is provided" in {


        val pensionIncomeModel = CreateUpdatePensionIncomeModel(
          None,
          fullPensionIncomeModel.overseasPensionContribution
        )

        (pensionIncomeConnector.createOrAmendPensionIncome(_: String, _: Int, _: CreateUpdatePensionIncomeModel)(_: HeaderCarrier))
          .expects(nino, taxYear, pensionIncomeModel, *)
          .returning(Future.successful(Right(())))

        (submissionConnector.refreshPensionsResponse(_: String, _: String, _: Int)(_: HeaderCarrier))
          .expects(nino, mtditid, taxYear, *)
          .returning(Future.successful(Right(())))

        val Right(result) = await(service.savePensionIncomeSessionData(nino, taxYear, mtditid, pensionIncomeModel))

        result mustBe (())

      }
    }


    "return error when Create Pension Input fails" in {
      val pensionIncomeModel = CreateUpdatePensionIncomeModel(
        fullPensionIncomeModel.foreignPension,
        fullPensionIncomeModel.overseasPensionContribution
      )

      val expectedErrorResult = Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError))


      (pensionIncomeConnector.createOrAmendPensionIncome(_: String, _: Int, _: CreateUpdatePensionIncomeModel)(_: HeaderCarrier))
        .expects(nino, taxYear, pensionIncomeModel, *)
        .returning(Future.successful(expectedErrorResult))

      val result = await(service.savePensionIncomeSessionData(nino, taxYear, mtditid, pensionIncomeModel))

      result mustBe expectedErrorResult

    }

    "return error when Refresh submission tax fails fails" in {
      val expectedErrorResult: RefreshIncomeSourceResponse = Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel.parsingError))

      val pensionIncomeModel = CreateUpdatePensionIncomeModel(
        None,
        fullPensionIncomeModel.overseasPensionContribution
      )

      (pensionIncomeConnector.createOrAmendPensionIncome(_: String, _: Int, _: CreateUpdatePensionIncomeModel)(_: HeaderCarrier))
        .expects(nino, taxYear, pensionIncomeModel, *)
        .returning(Future.successful(Right(())))




      (submissionConnector.refreshPensionsResponse(_: String, _: String, _: Int)(_: HeaderCarrier))
        .expects(nino, mtditid, taxYear, *)
        .returning(Future.successful(expectedErrorResult))

      val result = await(service.savePensionIncomeSessionData(nino, taxYear, mtditid, pensionIncomeModel))

      result mustBe expectedErrorResult

    }

  }

}
