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

package stubs.repositories

import cats.data.EitherT
import cats.implicits._
import models.common._
import models.database._
import models.domain.ApiResultT
import models.error.ServiceError
import play.api.libs.json.JsValue
import repositories.JourneyAnswersRepository

import scala.concurrent.{ExecutionContext, Future}

case class StubJourneyAnswersRepository(
    getAnswer: Option[JourneyAnswers] = None,
    var getAllJourneyStatuses: List[JourneyNameAndStatus] = List.empty,
    getJourneyStatus: List[JourneyNameAndStatus] = List.empty,
    saveJourneyStatus: Unit = Right(()),
    upsertDateField: Either[ServiceError, Unit] = Right(()),
    var upsertAnswersList: List[JsValue] = Nil,
    upsertStatusField: Either[ServiceError, Unit] = Right(()),
    var getPaymentsIntoPensionsRes: Either[ServiceError, Option[PaymentsIntoPensionsStorageAnswers]] = Right(None),
    var getUkPensionIncomeRes: Either[ServiceError, Option[UkPensionIncomeStorageAnswers]] = Right(None),
    var getStatePensionRes: Either[ServiceError, Option[IncomeFromPensionsStatePensionStorageAnswers]] = Right(None),
    var getAnnualAllowancesRes: Either[ServiceError, Option[AnnualAllowancesStorageAnswers]] = Right(None),
    var getUnauthorisedPaymentsRes: Either[ServiceError, Option[UnauthorisedPaymentsStorageAnswers]] = Right(None),
    var getPaymentsIntoOverseasPensionsRes: Either[ServiceError, Option[PaymentsIntoOverseasPensionsStorageAnswers]] = Right(None),
    var getIncomeFromOverseasPensionsRes: Either[ServiceError, Option[IncomeFromOverseasPensionsStorageAnswers]] = Right(None),
    var getTransferIntoOverseasPensionsRes: Either[ServiceError, Option[TransfersIntoOverseasPensionsStorageAnswers]] = Right(None),
    var getShortServiceRefundsRes: Either[ServiceError, Option[ShortServiceRefundsStorageAnswers]] = Right(None)
) extends JourneyAnswersRepository {
  implicit val ec: ExecutionContext = ExecutionContext.global

  private def rightT[A](a: Either[ServiceError, Option[A]]): ApiResultT[Option[A]] = EitherT.fromEither[Future](a)
  private def unit: ApiResultT[Unit]                                               = EitherT.rightT[Future, ServiceError](())

  def setStatus(ctx: JourneyContext, status: JourneyStatus): ApiResultT[Unit] =
    EitherT.fromEither[Future](upsertStatusField)

  def testOnlyClearAllData(): ApiResultT[Unit] = {
    getAllJourneyStatuses = Nil
    upsertAnswersList = Nil
    getPaymentsIntoPensionsRes = Right(None)
    getUkPensionIncomeRes = Right(None)
    getStatePensionRes = Right(None)
    getAnnualAllowancesRes = Right(None)
    getUnauthorisedPaymentsRes = Right(None)
    getPaymentsIntoOverseasPensionsRes = Right(None)
    getIncomeFromOverseasPensionsRes = Right(None)
    getTransferIntoOverseasPensionsRes = Right(None)
    getShortServiceRefundsRes = Right(None)

    unit
  }

  def getAllJourneyStatuses(taxYear: TaxYear, mtditid: Mtditid): ApiResultT[List[JourneyNameAndStatus]] =
    EitherT.rightT[Future, ServiceError](getAllJourneyStatuses)

  def getJourneyStatus(ctx: JourneyContext): ApiResultT[List[JourneyNameAndStatus]] = EitherT.rightT[Future, ServiceError](getJourneyStatus)

  def getPaymentsIntoPensions(ctx: JourneyContextWithNino): ApiResultT[Option[PaymentsIntoPensionsStorageAnswers]] =
    rightT(getPaymentsIntoPensionsRes)

  def upsertPaymentsIntoPensions(ctx: JourneyContextWithNino, storageAnswers: PaymentsIntoPensionsStorageAnswers): ApiResultT[Unit] = {
    getPaymentsIntoPensionsRes = storageAnswers.some.asRight[ServiceError]
    unit
  }

  def getUkPensionIncome(ctx: JourneyContextWithNino): ApiResultT[Option[UkPensionIncomeStorageAnswers]] =
    rightT(getUkPensionIncomeRes)

  def upsertUkPensionIncome(ctx: JourneyContextWithNino, storageAnswers: UkPensionIncomeStorageAnswers): ApiResultT[Unit] = {
    getUkPensionIncomeRes = storageAnswers.some.asRight[ServiceError]
    unit
  }

  def getStatePension(ctx: JourneyContextWithNino): ApiResultT[Option[IncomeFromPensionsStatePensionStorageAnswers]] =
    rightT(getStatePensionRes)

  def upsertStatePension(ctx: JourneyContextWithNino, storageAnswers: IncomeFromPensionsStatePensionStorageAnswers): ApiResultT[Unit] = {
    getStatePensionRes = storageAnswers.some.asRight[ServiceError]
    unit
  }

  def getAnnualAllowances(ctx: JourneyContextWithNino): ApiResultT[Option[AnnualAllowancesStorageAnswers]] =
    rightT(getAnnualAllowancesRes)

  def upsertAnnualAllowances(ctx: JourneyContextWithNino, storageAnswers: AnnualAllowancesStorageAnswers): ApiResultT[Unit] = {
    getAnnualAllowancesRes = storageAnswers.some.asRight[ServiceError]
    unit
  }

  def getUnauthorisedPayments(ctx: JourneyContextWithNino): ApiResultT[Option[UnauthorisedPaymentsStorageAnswers]] =
    rightT(getUnauthorisedPaymentsRes)

  def upsertUnauthorisedPayments(ctx: JourneyContextWithNino, storageAnswers: UnauthorisedPaymentsStorageAnswers): ApiResultT[Unit] = {
    getUnauthorisedPaymentsRes = storageAnswers.some.asRight[ServiceError]
    unit
  }

  def getPaymentsIntoOverseasPensions(ctx: JourneyContextWithNino): ApiResultT[Option[PaymentsIntoOverseasPensionsStorageAnswers]] =
    rightT(getPaymentsIntoOverseasPensionsRes)

  def upsertPaymentsIntoOverseasPensions(ctx: JourneyContextWithNino,
                                         storageAnswers: PaymentsIntoOverseasPensionsStorageAnswers): ApiResultT[Unit] = {
    getPaymentsIntoOverseasPensionsRes = storageAnswers.some.asRight[ServiceError]
    unit
  }

  def getIncomeFromOverseasPensions(ctx: JourneyContextWithNino): ApiResultT[Option[IncomeFromOverseasPensionsStorageAnswers]] =
    rightT(getIncomeFromOverseasPensionsRes)

  def upsertIncomeFromOverseasPensions(ctx: JourneyContextWithNino, storageAnswers: IncomeFromOverseasPensionsStorageAnswers): ApiResultT[Unit] = {
    getIncomeFromOverseasPensionsRes = storageAnswers.some.asRight[ServiceError]
    unit
  }

  def getTransferIntoOverseasPensions(ctx: JourneyContextWithNino): ApiResultT[Option[TransfersIntoOverseasPensionsStorageAnswers]] =
    rightT(getTransferIntoOverseasPensionsRes)

  def upsertTransferIntoOverseasPensions(ctx: JourneyContextWithNino,
                                         storageAnswers: TransfersIntoOverseasPensionsStorageAnswers): ApiResultT[Unit] = {
    getTransferIntoOverseasPensionsRes = storageAnswers.some.asRight[ServiceError]
    unit
  }

  def getShortServiceRefunds(ctx: JourneyContextWithNino): ApiResultT[Option[ShortServiceRefundsStorageAnswers]] =
    rightT(getShortServiceRefundsRes)

  def upsertShortServiceRefunds(ctx: JourneyContextWithNino, storageAnswers: ShortServiceRefundsStorageAnswers): ApiResultT[Unit] = {
    getShortServiceRefundsRes = storageAnswers.some.asRight[ServiceError]
    unit
  }
}
