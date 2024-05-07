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

package controllers

import connectors.httpParsers.GetPensionChargesHttpParser.GetPensionChargesResponse
import connectors.httpParsers.GetPensionReliefsHttpParser.GetPensionReliefsResponse
import connectors.httpParsers.GetStateBenefitsHttpParser.GetStateBenefitsResponse
import connectors.{GetStateBenefitsConnector, PensionChargesConnector, PensionReliefsConnector}
import models.{AllPensionsData, DesErrorBodyModel, DesErrorModel, ServiceErrorModel}
import org.scalamock.handlers.CallHandler4
import play.api.http.Status.{BAD_REQUEST, NO_CONTENT, OK}
import play.api.libs.json.Json
import services.PensionsService
import uk.gov.hmrc.http.HeaderCarrier
import utils.AllStateBenefitsDataBuilder.anAllStateBenefitsData
import utils.EmploymentPensionsBuilder.employmentPensionsData
import utils.TestUtils

import scala.concurrent.Future

class GetAllPensionsControllerSpec extends TestUtils {
  val reliefsConnector: PensionReliefsConnector         = mock[PensionReliefsConnector]
  val chargesConnector: PensionChargesConnector         = mock[PensionChargesConnector]
  val stateBenefitsConnector: GetStateBenefitsConnector = mock[GetStateBenefitsConnector]
  val pensionsService: PensionsService                  = mock[PensionsService]
  val controller: GetAllPensionsController              = new GetAllPensionsController(pensionsService, authorisedAction, mockControllerComponents)

  val expectedReliefsResult: GetPensionReliefsResponse      = Right(Some(fullPensionReliefsModel))
  val expectedChargesResult: GetPensionChargesResponse      = Right(Some(fullPensionChargesModel))
  val expectedStateBenefitsResult: GetStateBenefitsResponse = Right(Some(anAllStateBenefitsData))

  def mockGetAllPensionsData(): CallHandler4[String, Int, String, HeaderCarrier, Future[Either[ServiceErrorModel, AllPensionsData]]] = {
    val validAllPensionsData = Right(fullPensionsModel)
    (pensionsService
      .getAllPensionsData(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(validAllPensionsData))
  }

  def mockGetAllPensionsDataNoCharges(): CallHandler4[String, Int, String, HeaderCarrier, Future[Either[ServiceErrorModel, AllPensionsData]]] = {
    val validAllPensionsData = Right(
      AllPensionsData(Some(fullPensionReliefsModel), None, Some(anAllStateBenefitsData), Some(employmentPensionsData), Some(fullPensionIncomeModel)))
    (pensionsService
      .getAllPensionsData(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(validAllPensionsData))
  }

  def mockGetAllPensionsDataEmpty(): CallHandler4[String, Int, String, HeaderCarrier, Future[Either[ServiceErrorModel, AllPensionsData]]] = {
    val validEmptyPensionsData = Right(AllPensionsData(None, None, None, None, None))
    (pensionsService
      .getAllPensionsData(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(validEmptyPensionsData))
  }

  def mockGetAllPensionsDataBadRequest(): CallHandler4[String, Int, String, HeaderCarrier, Future[Either[ServiceErrorModel, AllPensionsData]]] = {
    val badRequestResponse = Left(DesErrorModel(BAD_REQUEST, DesErrorBodyModel.invalidTaxYear))
    (pensionsService
      .getAllPensionsData(_: String, _: Int, _: String)(_: HeaderCarrier))
      .expects(*, *, *, *)
      .returning(Future.successful(badRequestResponse))
  }

  ".getPensions" should {

    "return a 200 response and AllPensionData model" in {
      val result = {
        mockAuth()
        mockGetAllPensionsData()
        controller.getAllPensions(nino, taxYear)(fakeRequest)
      }

      status(result) mustBe OK
      bodyOf(result) mustBe Json.toJson(fullPensionsModel).toString()
    }

    "return a 200 response and AllPensionsData model when a pension type is None" in {
      val result = {
        mockAuth()
        mockGetAllPensionsDataNoCharges()
        controller.getAllPensions(nino, taxYear)(fakeRequest)
      }

      status(result) mustBe OK
      bodyOf(result) mustBe Json
        .toJson(
          AllPensionsData(
            Some(fullPensionReliefsModel),
            None,
            Some(anAllStateBenefitsData),
            Some(employmentPensionsData),
            Some(fullPensionIncomeModel)))
        .toString()
    }

    "return a No Content if pension reliefs, pension charges and state benefits are all None" in {
      val result = {
        mockAuth()
        mockGetAllPensionsDataEmpty()
        controller.getAllPensions(nino, taxYear)(fakeRequest)
      }

      status(result) mustBe NO_CONTENT
    }

    "return an error" when {

      "either connector returns an error" in {
        val result = {
          mockAuth()
          mockGetAllPensionsDataBadRequest()
          controller.getAllPensions(nino, taxYear)(fakeRequest)
        }

        status(result) mustBe BAD_REQUEST
      }
    }
  }
}
