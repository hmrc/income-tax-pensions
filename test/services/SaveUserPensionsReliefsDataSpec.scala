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
import connectors._
import connectors.httpParsers.CreateUpdatePensionChargesHttpParser.CreateUpdatePensionChargesResponse
import connectors.httpParsers.GetPensionReliefsHttpParser.GetPensionReliefsResponse
import connectors.httpParsers.RefreshIncomeSourceHttpParser.RefreshIncomeSourceResponse
import models._
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class SaveUserPensionsReliefsDataSpec extends TestUtils {
  SharedMetricRegistries.clear()
  
  val submissionConnector: SubmissionConnector = mock[SubmissionConnector]
  val reliefsConnector: PensionReliefsConnector = mock[PensionReliefsConnector]
  
  val service: PensionsService = new PensionsService(
    reliefsConnector, mock[PensionChargesConnector], mock[GetStateBenefitsConnector], mock[PensionIncomeConnector], submissionConnector)

  val taxYear = 2022
  val nino = "AA123456A"
  val mtditid = "1234567890"
  
  
  "saveUserPensionReliefsData" should {
    
    val pensionRelief = PensionReliefs(Some(100.01), Some(100.01), Some(100.01), Some(100.01), Some(100.01))
    val userData = CreateOrUpdatePensionReliefsModel(pensionRelief)
    val expectedMergedDataChange = CreateOrUpdatePensionReliefsModel(fullPensionReliefsModel.pensionReliefs)
    
    val expectedReliefsResult: GetPensionReliefsResponse = Right(Some(fullPensionReliefsModel))
    
    "return Right(unit) " should {
      
      "successfully merge changes if update contains pensions Reliefs" in {
        (reliefsConnector.getPensionReliefs(_: String, _: Int)(_: HeaderCarrier))
          .expects(nino, taxYear, *)
          .returning(Future.successful(expectedReliefsResult))

        (reliefsConnector.createOrAmendPensionReliefs(_: String, _: Int, _: CreateOrUpdatePensionReliefsModel)(_: HeaderCarrier))
          .expects(nino, taxYear, expectedMergedDataChange, *)
          .returning(Future.successful(Right(())))

        (submissionConnector.refreshPensionsResponse(_: String, _: String, _: Int)(_: HeaderCarrier))
          .expects(nino, mtditid, taxYear, *)
          .returning(Future.successful(Right(())))

        val Right(result) = await(service.saveUserPensionReliefsData(nino, mtditid, taxYear, userData))

        result mustBe ()
      }
    }

    "return error when Get Pension Reliefs fails" in {
      val expectedErrorResult: GetPensionReliefsResponse = Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError))

      (reliefsConnector.getPensionReliefs(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedErrorResult))

      val result = await(service.saveUserPensionReliefsData(nino, mtditid, taxYear, userData))

      result mustBe expectedErrorResult
    }
    
    "return error when Create Pension Reliefs fails" in {
      val expectedErrorResult: CreateUpdatePensionChargesResponse = Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError))
     
      (reliefsConnector.getPensionReliefs(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedReliefsResult))

      (reliefsConnector.createOrAmendPensionReliefs(_: String, _: Int, _: CreateOrUpdatePensionReliefsModel)(_: HeaderCarrier))
        .expects(nino, taxYear, expectedMergedDataChange, *)
        .returning(Future.successful(expectedErrorResult))

      val result = await(service.saveUserPensionReliefsData(nino, mtditid, taxYear, userData))

      result mustBe expectedErrorResult
    }

    "return error when Refresh submission tax fails" in {
      val expectedErrorResult: RefreshIncomeSourceResponse = Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel.parsingError))
      
      (reliefsConnector.getPensionReliefs(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedReliefsResult))

      (reliefsConnector.createOrAmendPensionReliefs(_: String, _: Int, _: CreateOrUpdatePensionReliefsModel)(_: HeaderCarrier))
        .expects(nino, taxYear, expectedMergedDataChange, *)
        .returning(Future.successful(Right(())))

      (submissionConnector.refreshPensionsResponse(_: String, _: String, _: Int)(_: HeaderCarrier))
        .expects(nino, mtditid, taxYear, *)
        .returning(Future.successful(expectedErrorResult))

      val result = await(service.saveUserPensionReliefsData(nino, mtditid, taxYear, userData))

      result mustBe expectedErrorResult

    }

  }

}
