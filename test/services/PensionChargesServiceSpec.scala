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
import connectors.httpParsers.GetPensionChargesHttpParser.GetPensionChargesResponse
import connectors.httpParsers.RefreshIncomeSourceHttpParser.RefreshIncomeSourceResponse
import models._
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.Future

class PensionChargesServiceSpec extends TestUtils {
  SharedMetricRegistries.clear()
  
  val chargesConnector: PensionChargesConnector = mock[PensionChargesConnector]
  val submissionConnector: SubmissionConnector = mock[SubmissionConnector]
  
  val service: PensionChargesService = new PensionChargesService( chargesConnector, submissionConnector)

  val taxYear = 2022
  val nino = "AA123456A"
  val mtditid = "1234567890"
  
  val expectedChargesResult: GetPensionChargesResponse = Right(Some(fullPensionChargesModel))

  "saveUserPensionChargesData" should {

    val (userData, pensionSchemeUnauthorisedPayments) = createUserData()
    
    "return Right(unit) " should {
      
      "successfully merge changes if update contains pensionSchemeUnauthorisedPayments" in {
        fullPensionChargesModel.copy(pensionSchemeUnauthorisedPayments = pensionSchemeUnauthorisedPayments)

        (chargesConnector.createUpdatePensionCharges(_: String, _: Int, _: CreateUpdatePensionChargesRequestModel)(_: HeaderCarrier))
          .expects(nino, taxYear, userData, *)
          .returning(Future.successful(Right(())))

        (submissionConnector.refreshPensionsResponse(_: String, _: String, _: Int)(_: HeaderCarrier))
          .expects(nino, mtditid, taxYear, *)
          .returning(Future.successful(Right(())))

        val Right(result) = await(service.saveUserPensionChargesData(nino, mtditid, taxYear, userData))

        result mustBe ()
      }
    }


    "return error when Create Pension Charges fails" in {
      val expectedErrorResult: CreateUpdatePensionChargesResponse = Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError))

      fullPensionChargesModel.copy(pensionSchemeUnauthorisedPayments = pensionSchemeUnauthorisedPayments)

      (chargesConnector.createUpdatePensionCharges(_: String, _: Int, _: CreateUpdatePensionChargesRequestModel)(_: HeaderCarrier))
        .expects(nino, taxYear, userData, *)
        .returning(Future.successful(expectedErrorResult))

      val result = await(service.saveUserPensionChargesData(nino, mtditid, taxYear, userData))

      result mustBe expectedErrorResult
    }

    "return error when Refresh submission tax fails" in {
      fullPensionChargesModel.copy(pensionSchemeUnauthorisedPayments = pensionSchemeUnauthorisedPayments)
      val expectedErrorResult: RefreshIncomeSourceResponse = Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel.parsingError))
      

      (chargesConnector.createUpdatePensionCharges(_: String, _: Int, _: CreateUpdatePensionChargesRequestModel)(_: HeaderCarrier))
        .expects(nino, taxYear, userData, *)
        .returning(Future.successful(Right(())))

      (submissionConnector.refreshPensionsResponse(_: String, _: String, _: Int)(_: HeaderCarrier))
        .expects(nino, mtditid, taxYear, *)
        .returning(Future.successful(expectedErrorResult))

      val result = await(service.saveUserPensionChargesData(nino, mtditid, taxYear, userData))

      result mustBe expectedErrorResult
    }
  }

  "deleteUserPensionChargesData" should {

    "return Right(unit) " should {

      "successfully delete pension charges" in {

        (chargesConnector.deletePensionCharges(_: String, _: Int)(_: HeaderCarrier))
          .expects(nino, taxYear, *)
          .returning(Future.successful(Right(())))

        (submissionConnector.refreshPensionsResponse(_: String, _: String, _: Int)(_: HeaderCarrier))
          .expects(nino, mtditid, taxYear, *)
          .returning(Future.successful(Right(())))

        val Right(result) = await(service.deleteUserPensionChargesData(nino, mtditid, taxYear))

        result mustBe ()
      }
    }


    "return error when Create Pension Charges fails" in {
      val expectedErrorResult: CreateUpdatePensionChargesResponse = Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError))


      (chargesConnector.deletePensionCharges(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedErrorResult))

      val result = await(service.deleteUserPensionChargesData(nino, mtditid, taxYear))

      result mustBe expectedErrorResult
    }

    "return error when Refresh submission tax fails" in {
      val expectedErrorResult: RefreshIncomeSourceResponse = Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel.parsingError))


      (chargesConnector.deletePensionCharges(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(())))

      (submissionConnector.refreshPensionsResponse(_: String, _: String, _: Int)(_: HeaderCarrier))
        .expects(nino, mtditid, taxYear, *)
        .returning(Future.successful(expectedErrorResult))

      val result = await(service.deleteUserPensionChargesData(nino, mtditid, taxYear))

      result mustBe expectedErrorResult
    }
  }

  private def createUserData() = {
    val pensionSchemeUnauthorisedPayments = Some(PensionSchemeUnauthorisedPayments(
      pensionSchemeTaxReference = Some(Seq("00543216RA", "00123456RB")),
      surcharge = Some(Charge(
        amount = 124.44,
        foreignTaxPaid = 123.33
      )),
      noSurcharge = Some(Charge(
        amount = 222.44,
        foreignTaxPaid = 223.33
      ))
    ))

    val userData = CreateUpdatePensionChargesRequestModel(
      None,
      None,
      pensionSchemeUnauthorisedPayments,
      None,
      None
    )

    (userData, pensionSchemeUnauthorisedPayments)
  }

}
