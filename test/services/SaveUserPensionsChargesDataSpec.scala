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

class SaveUserPensionsChargesDataSpec extends TestUtils {
  SharedMetricRegistries.clear()
  
  val submissionConnector: SubmissionConnector = mock[SubmissionConnector]
  val chargesConnector: PensionChargesConnector = mock[PensionChargesConnector]
  
  val service: PensionsService = new PensionsService(
    mock[PensionReliefsConnector], chargesConnector, mock[GetStateBenefitsConnector], mock[PensionIncomeConnector], submissionConnector)

  val taxYear = 2022
  val nino = "AA123456A"
  val mtditid = "1234567890"
  
  val expectedChargesResult: GetPensionChargesResponse = Right(Some(fullPensionChargesModel))

  "saveUserPensionChargesData" should {

    "return Right(unit) " should {
      "successfully merge changes if update contains pensionSchemeUnauthorisedPayments" in {

        val pensionSchemeUnauthorisedPayments = Some(PensionSchemeUnauthorisedPayments(
          pensionSchemeTaxReference = Seq("00543216RA", "00123456RB"),
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

        val expectedMergedDataChange = CreateUpdatePensionChargesRequestModel(
          fullPensionChargesModel.pensionSavingsTaxCharges,
          fullPensionChargesModel.pensionSchemeOverseasTransfers,
          pensionSchemeUnauthorisedPayments,
          fullPensionChargesModel.pensionContributions,
          fullPensionChargesModel.overseasPensionContributions,
        )


        fullPensionChargesModel.copy(pensionSchemeUnauthorisedPayments = pensionSchemeUnauthorisedPayments)

        (chargesConnector.getPensionCharges(_: String, _: Int)(_: HeaderCarrier))
          .expects(nino, taxYear, *)
          .returning(Future.successful(expectedChargesResult))

        (chargesConnector.createUpdatePensionCharges(_: String, _: Int, _: CreateUpdatePensionChargesRequestModel)(_: HeaderCarrier))
          .expects(nino, taxYear, expectedMergedDataChange, *)
          .returning(Future.successful(Right(())))

        (submissionConnector.refreshPensionsResponse(_: String, _: String, _: Int)(_: HeaderCarrier))
          .expects(nino, mtditid, taxYear, *)
          .returning(Future.successful(Right(())))

        val Right(result) = await(service.saveUserPensionChargesData(nino, mtditid, taxYear, userData))

        result mustBe (())

      }
    }

    "return error when Get Pension Charges fails" in {
      val pensionSchemeUnauthorisedPayments = Some(PensionSchemeUnauthorisedPayments(
        pensionSchemeTaxReference = Seq("00543216RA", "00123456RB"),
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

      val expectedErrorResult: GetPensionChargesResponse = Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError))

      (chargesConnector.getPensionCharges(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedErrorResult))

      val result = await(service.saveUserPensionChargesData(nino, mtditid, taxYear, userData))

      result mustBe expectedErrorResult
    }


    "return error when Create Pension Charges fails" in {
      val pensionSchemeUnauthorisedPayments = Some(PensionSchemeUnauthorisedPayments(
        pensionSchemeTaxReference = Seq("00543216RA", "00123456RB"),
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

      val expectedMergedDataChange = CreateUpdatePensionChargesRequestModel(
        fullPensionChargesModel.pensionSavingsTaxCharges,
        fullPensionChargesModel.pensionSchemeOverseasTransfers,
        pensionSchemeUnauthorisedPayments,
        fullPensionChargesModel.pensionContributions,
        fullPensionChargesModel.overseasPensionContributions,
      )
      val expectedErrorResult: CreateUpdatePensionChargesResponse = Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError))

      fullPensionChargesModel.copy(pensionSchemeUnauthorisedPayments = pensionSchemeUnauthorisedPayments)

      (chargesConnector.getPensionCharges(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedChargesResult))

      (chargesConnector.createUpdatePensionCharges(_: String, _: Int, _: CreateUpdatePensionChargesRequestModel)(_: HeaderCarrier))
        .expects(nino, taxYear, expectedMergedDataChange, *)
        .returning(Future.successful(expectedErrorResult))

      val result = await(service.saveUserPensionChargesData(nino, mtditid, taxYear, userData))

      result mustBe expectedErrorResult

    }

    "return error when Refresh submission tax fails" in {

      val pensionSchemeUnauthorisedPayments = Some(PensionSchemeUnauthorisedPayments(
        pensionSchemeTaxReference = Seq("00543216RA", "00123456RB"),
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

      val expectedMergedDataChange = CreateUpdatePensionChargesRequestModel(
        fullPensionChargesModel.pensionSavingsTaxCharges,
        fullPensionChargesModel.pensionSchemeOverseasTransfers,
        pensionSchemeUnauthorisedPayments,
        fullPensionChargesModel.pensionContributions,
        fullPensionChargesModel.overseasPensionContributions,
      )


      fullPensionChargesModel.copy(pensionSchemeUnauthorisedPayments = pensionSchemeUnauthorisedPayments)
      val expectedErrorResult: RefreshIncomeSourceResponse = Left(APIErrorModel(INTERNAL_SERVER_ERROR, APIErrorBodyModel.parsingError))


      (chargesConnector.getPensionCharges(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedChargesResult))

      (chargesConnector.createUpdatePensionCharges(_: String, _: Int, _: CreateUpdatePensionChargesRequestModel)(_: HeaderCarrier))
        .expects(nino, taxYear, expectedMergedDataChange, *)
        .returning(Future.successful(Right(())))

      (submissionConnector.refreshPensionsResponse(_: String, _: String, _: Int)(_: HeaderCarrier))
        .expects(nino, mtditid, taxYear, *)
        .returning(Future.successful(expectedErrorResult))

      val result = await(service.saveUserPensionChargesData(nino, mtditid, taxYear, userData))

      result mustBe expectedErrorResult

    }

  }

}
