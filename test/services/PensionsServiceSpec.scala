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
import connectors.httpParsers.GetPensionIncomeHttpParser.GetPensionIncomeResponse
import connectors.httpParsers.GetPensionReliefsHttpParser.GetPensionReliefsResponse
import connectors.httpParsers.RefreshIncomeSourceHttpParser.RefreshIncomeSourceResponse
import connectors.httpParsers.GetStateBenefitsHttpParser.GetStateBenefitsResponse
import models._
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.http.HeaderCarrier
import utils.AllStateBenefitsDataBuilder.anAllStateBenefitsData
import utils.TestUtils

import scala.concurrent.Future

class PensionsServiceSpec extends TestUtils {
  SharedMetricRegistries.clear()

  val reliefsConnector: PensionReliefsConnector = mock[PensionReliefsConnector]
  val chargesConnector: PensionChargesConnector = mock[PensionChargesConnector]
  val stateBenefitsConnector: GetStateBenefitsConnector = mock[GetStateBenefitsConnector]
  val pensionIncomeConnector: PensionIncomeConnector = mock[PensionIncomeConnector]
  val submissionConnector: SubmissionConnector = mock[SubmissionConnector]
  val service: PensionsService = new PensionsService(reliefsConnector, chargesConnector, stateBenefitsConnector, pensionIncomeConnector, submissionConnector)

  val taxYear = 2022
  val nino = "AA123456A"
  val mtditid = "1234567890"

  val expectedReliefsResult: GetPensionReliefsResponse = Right(Some(fullPensionReliefsModel))
  val expectedChargesResult: GetPensionChargesResponse = Right(Some(fullPensionChargesModel))
  val expectedStateBenefitsResult: GetStateBenefitsResponse = Right(Some(anAllStateBenefitsData))
  val expectedPensionIncomeResult: GetPensionIncomeResponse = Right(Some(fullPensionIncomeModel))


  "getAllPensionsData" should {

    "get all pension reliefs, charges and income data and return a full AllPensionsData model" in {

      (reliefsConnector.getPensionReliefs(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedReliefsResult))

      (chargesConnector.getPensionCharges(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedChargesResult))

      (stateBenefitsConnector.getStateBenefits(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedStateBenefitsResult))

      (pensionIncomeConnector.getPensionIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedPensionIncomeResult))

      val result = await(service.getAllPensionsData(nino, taxYear, mtditid))

      result mustBe Right(fullPensionsModel)
    }

    "return a Right if all connectors return None" in {
      (reliefsConnector.getPensionReliefs(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(None)))

      (chargesConnector.getPensionCharges(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(None)))

      (stateBenefitsConnector.getStateBenefits(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(None)))

      (pensionIncomeConnector.getPensionIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(None)))

      val result = await(service.getAllPensionsData(nino, taxYear, mtditid))

      result mustBe Right(AllPensionsData(None, None, None, None))
    }

    "return an error if a connector call fails" in {
      val expectedErrorResult: GetPensionChargesResponse = Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError))

      (reliefsConnector.getPensionReliefs(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedReliefsResult))

      (chargesConnector.getPensionCharges(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedErrorResult))

      val result = await(service.getAllPensionsData(nino, taxYear, mtditid))

      result mustBe expectedErrorResult

    }
  }

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

      val expectedErrorResult: CreateUpdatePensionChargesResponse = Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError))

      fullPensionChargesModel.copy(pensionSchemeUnauthorisedPayments = pensionSchemeUnauthorisedPayments)


      (chargesConnector.createUpdatePensionCharges(_: String, _: Int, _: CreateUpdatePensionChargesRequestModel)(_: HeaderCarrier))
        .expects(nino, taxYear, userData, *)
        .returning(Future.successful(expectedErrorResult))

      val result = await(service.saveUserPensionChargesData(nino, mtditid, taxYear, userData))

      result mustBe expectedErrorResult

    }

    "return error when Refresh submission tax fails fails" in {

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

}
