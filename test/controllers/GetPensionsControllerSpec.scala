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

package controllers

import connectors.httpParsers.GetPensionChargesHttpParser.GetPensionChargesResponse
import connectors.httpParsers.GetPensionReliefsHttpParser.GetPensionReliefsResponse
import connectors.httpParsers.GetStateBenefitsHttpParser.GetStateBenefitsResponse
import connectors.{GetStateBenefitsConnector, PensionChargesConnector, PensionReliefsConnector}
import models.{AllPensionsData, DesErrorBodyModel, DesErrorModel}
import org.scalamock.handlers.CallHandler4
import play.api.http.Status.{BAD_REQUEST, NO_CONTENT, OK}
import play.api.libs.json.Json
import services.PensionsService
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestUtils

import scala.concurrent.{ExecutionContext, Future}

class GetPensionsControllerSpec extends TestUtils{
  val reliefsConnector: PensionReliefsConnector = mock[PensionReliefsConnector]
  val chargesConnector: PensionChargesConnector = mock[PensionChargesConnector]
  val stateBenefitsConnector: GetStateBenefitsConnector = mock[GetStateBenefitsConnector]
  val pensionsService: PensionsService = mock[PensionsService]
  val controller: GetPensionsController = new GetPensionsController(pensionsService, authorisedAction, mockControllerComponents)

  val taxYear = 2022
  val nino = "AA123456A"
  val mtditid = "1234567890"

  val expectedReliefsResult: GetPensionReliefsResponse = Right(Some(fullPensionReliefsModel))
  val expectedChargesResult: GetPensionChargesResponse = Right(Some(fullPensionChargesModel))
  val expectedStateBenefitsResult: GetStateBenefitsResponse = Right(Some(fullStateBenefitsModel))


  def mockGetAllPensionsData(): CallHandler4[String, Int, HeaderCarrier, ExecutionContext,
  Future[Either[DesErrorModel, AllPensionsData]]] = {
    val validAllPensionsData = Right(fullPensionsModel)
    (pensionsService.getAllPensionsData(_:String, _:Int)(_:HeaderCarrier, _:ExecutionContext))
      .expects(*, *, *, *)
    .returning(Future.successful(validAllPensionsData))
  }

  def mockGetAllPensionsDataNoCharges(): CallHandler4[String, Int, HeaderCarrier, ExecutionContext,
    Future[Either[DesErrorModel, AllPensionsData]]] = {
    val validAllPensionsData = Right(AllPensionsData(Some(fullPensionReliefsModel), None, Some(fullStateBenefitsModel)))
    (pensionsService.getAllPensionsData(_:String, _:Int)(_:HeaderCarrier, _:ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.successful(validAllPensionsData))
  }

  def mockGetAllPensionsDataEmpty(): CallHandler4[String, Int, HeaderCarrier, ExecutionContext,
    Future[Either[DesErrorModel, AllPensionsData]]] = {
    val validEmptyPensionsData = Right(AllPensionsData(None, None, None))
    (pensionsService.getAllPensionsData(_:String, _:Int)(_:HeaderCarrier, _:ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.successful(validEmptyPensionsData))
  }

  def mockGetAllPensionsDataBadRequest(): CallHandler4[String, Int, HeaderCarrier, ExecutionContext,
    Future[Either[DesErrorModel, AllPensionsData]]] = {
    val badRequestResponse = Left(DesErrorModel(BAD_REQUEST, DesErrorBodyModel.invalidTaxYear))
    (pensionsService.getAllPensionsData(_:String, _:Int)(_:HeaderCarrier, _:ExecutionContext))
      .expects(*, *, *, *)
      .returning(Future.successful(badRequestResponse))
  }



  ".getPensions" should {

    "return a 200 response and AllPensionData model" in {
      val result = {
        mockAuth()
        mockGetAllPensionsData()
        controller.getPensions(nino, taxYear)(fakeRequest)
      }

      status(result) mustBe OK
      bodyOf(result) mustBe Json.toJson(fullPensionsModel).toString()
    }

    "return a 200 response and AllPensionsData model when one pension type is None" in {
      val result = {
        mockAuth()
        mockGetAllPensionsDataNoCharges()
        controller.getPensions(nino, taxYear)(fakeRequest)
      }

      status(result) mustBe OK
      bodyOf(result) mustBe Json.toJson(AllPensionsData(None, None, None)).toString()
    }


    "return a No Content if pension reliefs, pension charges and state benefits are all None" in {
      val result = {
        mockAuth()
        mockGetAllPensionsDataEmpty()
        controller.getPensions(nino, taxYear)(fakeRequest)
      }

      status(result) mustBe NO_CONTENT
      bodyOf(result) mustBe Json.toJson(AllPensionsData(None, None, None)).toString()
    }

    "return an error" when {

      "either connector returns an error" in {
        val result = {
          mockAuth()
          mockGetAllPensionsDataBadRequest()
          controller.getPensions(nino, taxYear)(fakeRequest)
        }

        status(result) mustBe BAD_REQUEST
      }
    }
  }
}
