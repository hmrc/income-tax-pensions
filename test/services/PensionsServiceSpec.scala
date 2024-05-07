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
import connectors.httpParsers.GetPensionChargesHttpParser.GetPensionChargesResponse
import connectors.httpParsers.GetPensionIncomeHttpParser.GetPensionIncomeResponse
import connectors.httpParsers.GetPensionReliefsHttpParser.GetPensionReliefsResponse
import connectors.httpParsers.GetStateBenefitsHttpParser.GetStateBenefitsResponse
import mocks.MockPensionReliefsConnector
import models._
import models.common.{Journey, JourneyContextWithNino, Mtditid}
import models.database.PaymentsIntoPensionsStorageAnswers
import models.employment.AllEmploymentData
import models.frontend.PaymentsIntoPensionsAnswers
import org.scalatest.BeforeAndAfterEach
import org.scalatest.EitherValues._
import org.scalatest.OptionValues._
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.Json
import stubs.repositories.StubJourneyAnswersRepository
import testdata.frontend.paymentsIntoPensionsAnswers
import uk.gov.hmrc.http.HeaderCarrier
import utils.AllEmploymentsDataBuilder.allEmploymentsData
import utils.AllStateBenefitsDataBuilder.anAllStateBenefitsData
import utils.EitherTTestOps.convertScalaFuture
import utils.TestUtils
import utils.TestUtils.currTaxYear

import scala.concurrent.Future

class PensionsServiceSpec extends TestUtils with MockPensionReliefsConnector with BeforeAndAfterEach {
  SharedMetricRegistries.clear()
  private val sampleCtx = JourneyContextWithNino(currTaxYear, Mtditid(mtditid), TestUtils.nino)

  val chargesConnector: PensionChargesConnector         = mock[PensionChargesConnector]
  val stateBenefitsConnector: GetStateBenefitsConnector = mock[GetStateBenefitsConnector]
  val pensionIncomeConnector: PensionIncomeConnector    = mock[PensionIncomeConnector]
  val mockEmploymentConnector: EmploymentConnector      = mock[EmploymentConnector]
  val stubRepository                                    = StubJourneyAnswersRepository()

  val service: PensionsService =
    new PensionsService(
      mockReliefsConnector,
      chargesConnector,
      stateBenefitsConnector,
      pensionIncomeConnector,
      mockEmploymentConnector,
      stubRepository
    )

  val expectedReliefsResult: GetPensionReliefsResponse                        = Right(Some(fullPensionReliefsModel))
  val expectedChargesResult: GetPensionChargesResponse                        = Right(Some(fullPensionChargesModel))
  val expectedStateBenefitsResult: GetStateBenefitsResponse                   = Right(Some(anAllStateBenefitsData))
  val expectedEmploymentsResult: DownstreamErrorOr[Option[AllEmploymentData]] = Right(Some(allEmploymentsData))
  val expectedPensionIncomeResult: GetPensionIncomeResponse                   = Right(Some(fullPensionIncomeModel))

  override def beforeEach(): Unit = {
    stubRepository.testOnlyClearAllData()
    super.beforeEach()
  }

  "getAllPensionsData" should {

    // TODO fix in https://jira.tools.tax.service.gov.uk/browse/SASS-8136
    "get all data and return a full AllPensionsData model" ignore {
      (mockReliefsConnector
        .getPensionReliefs(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedReliefsResult))

      (chargesConnector
        .getPensionCharges(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedChargesResult))

      (stateBenefitsConnector
        .getStateBenefits(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedStateBenefitsResult))

      (mockEmploymentConnector
        .loadEmployments(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedEmploymentsResult))

      (pensionIncomeConnector
        .getPensionIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedPensionIncomeResult))

      val result = await(service.getAllPensionsData(nino, taxYear, mtditid))

      result mustBe Right(fullPensionsModel)
    }

    // TODO fix in https://jira.tools.tax.service.gov.uk/browse/SASS-8136
    "return a Right if all connectors return None" ignore {
      (mockReliefsConnector
        .getPensionReliefs(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(None)))

      (chargesConnector
        .getPensionCharges(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(None)))

      (stateBenefitsConnector
        .getStateBenefits(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(None)))

      (mockEmploymentConnector
        .loadEmployments(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(None)))

      (pensionIncomeConnector
        .getPensionIncome(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(Right(None)))

      val result = await(service.getAllPensionsData(nino, taxYear, mtditid))

      result mustBe Right(AllPensionsData(None, None, None, None, None))
    }

    "return an error if a connector call fails" in {
      val expectedErrorResult: GetPensionChargesResponse = Left(DesErrorModel(INTERNAL_SERVER_ERROR, DesErrorBodyModel.parsingError))

      (mockReliefsConnector
        .getPensionReliefs(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedReliefsResult))

      (chargesConnector
        .getPensionCharges(_: String, _: Int)(_: HeaderCarrier))
        .expects(nino, taxYear, *)
        .returning(Future.successful(expectedErrorResult))

      val result = await(service.getAllPensionsData(nino, taxYear, mtditid))

      result mustBe expectedErrorResult

    }
  }

  "getPaymentsIntoPensions" should {
    val paymentIntoPensionsCtx = sampleCtx.toJourneyContext(Journey.PaymentsIntoPensions)
    val answers                = PaymentsIntoPensionsStorageAnswers(true, Some(true), true, Some(true), Some(true))

    "get None if no answers" in {
      mockGetPensionReliefsT(Right(None))
      val result = service.getPaymentsIntoPensions(sampleCtx).value.futureValue
      assert(result.value === None)
    }

    // TODO It is not valid situation, we probably need to think how we want to handle mismatches IFS vs our DB
    "get answers if there are DB answers, but IFS return None (favour IFS)" in {
      mockGetPensionReliefsT(Right(None))

      val result = (for {
        _   <- stubRepository.upsertAnswers(paymentIntoPensionsCtx, Json.toJson(answers))
        res <- service.getPaymentsIntoPensions(sampleCtx)
      } yield res).value.futureValue.value

      assert(result === Some(PaymentsIntoPensionsAnswers(false, None, Some(true), None, true, Some(true), None, Some(true), None)))
    }

    "return answers" in {
      mockGetPensionReliefsT(Right(Some(GetPensionReliefsModel("unused", None, PensionReliefs(Some(1.0), Some(2.0), Some(3.0), Some(4.0), None)))))
      val result = (for {
        _   <- stubRepository.upsertAnswers(paymentIntoPensionsCtx, Json.toJson(answers))
        res <- service.getPaymentsIntoPensions(sampleCtx)
      } yield res).value.futureValue.value

      assert(result.value === PaymentsIntoPensionsAnswers(true, Some(1.0), Some(true), Some(2.0), true, Some(true), Some(3.0), Some(true), Some(4.0)))
    }
  }

  "upsertPaymentsIntoPensions" should {
    "upsert answers if overseasPensionSchemeContributions does not exist" in {
      val overseasPensionSchemeContributions: Option[BigDecimal] = None
      mockGetPensionReliefsT(Right(None))
      mockCreateOrAmendPensionReliefsT(
        Right(None),
        expectedModel =
          CreateOrUpdatePensionReliefsModel(PensionReliefs(Some(1.0), Some(2.0), Some(3.0), Some(4.0), overseasPensionSchemeContributions))
      )

      val result = service.upsertPaymentsIntoPensions(sampleCtx, paymentsIntoPensionsAnswers).value.futureValue

      assert(result.isRight)
      assert(stubRepository.upsertAnswersList.size === 1)
      val persistedAnswers = stubRepository.upsertAnswersList.head.as[PaymentsIntoPensionsStorageAnswers]
      assert(persistedAnswers === PaymentsIntoPensionsStorageAnswers(true, Some(true), true, Some(true), Some(true)))
    }

    "upsert answers if overseasPensionSchemeContributions exist" in {
      val overseasPensionSchemeContributions: Option[BigDecimal] = Some(5.0)
      mockGetPensionReliefsT(
        Right(Some(GetPensionReliefsModel("unused", None, PensionReliefs(None, None, None, None, overseasPensionSchemeContributions)))))

      mockCreateOrAmendPensionReliefsT(
        Right(None),
        expectedModel =
          CreateOrUpdatePensionReliefsModel(PensionReliefs(Some(1.0), Some(2.0), Some(3.0), Some(4.0), overseasPensionSchemeContributions))
      )

      val result = service.upsertPaymentsIntoPensions(sampleCtx, paymentsIntoPensionsAnswers).value.futureValue

      assert(result.isRight)
      assert(stubRepository.upsertAnswersList.size === 1)
      val persistedAnswers = stubRepository.upsertAnswersList.head.as[PaymentsIntoPensionsStorageAnswers]
      assert(persistedAnswers === PaymentsIntoPensionsStorageAnswers(true, Some(true), true, Some(true), Some(true)))
    }

  }

}
