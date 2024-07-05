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
import play.api.libs.json.{JsValue, Reads}
import repositories.JourneyAnswersRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

case class StubJourneyAnswersRepository(
    getAnswer: Option[JourneyAnswers] = None,
    var getAllJourneyStatuses: List[JourneyNameAndStatus] = List.empty,
    getJourneyStatus: List[JourneyNameAndStatus] = List.empty,
    saveJourneyStatus: Unit = Right(()),
    upsertDateField: Either[ServiceError, Unit] = Right(()),
    var upsertAnswersList: List[JsValue] = Nil,
    upsertStatusField: Either[ServiceError, Unit] = Right(()),
    getPaymentsIntoPensionsRes: Either[ServiceError, Option[PaymentsIntoPensionsStorageAnswers]] = Right(None),
    getUkPensionIncomeRes: Either[ServiceError, Option[UkPensionIncomeStorageAnswers]] = Right(None),
    getStatePensionRes: Either[ServiceError, Option[IncomeFromPensionsStatePensionStorageAnswers]] = Right(None),
    getAnnualAllowancesRes: Either[ServiceError, Option[AnnualAllowancesStorageAnswers]] = Right(None),
    getUnauthorisedPaymentsRes: Either[ServiceError, Option[UnauthorisedPaymentsStorageAnswers]] = Right(None),
    getPaymentsIntoOverseasPensionsRes: Either[ServiceError, Option[PaymentsIntoOverseasPensionsStorageAnswers]] = Right(None),
    getIncomeFromOverseasPensionsRes: Either[ServiceError, Option[IncomeFromOverseasPensionsStorageAnswers]] = Right(None),
    getTransferIntoOverseasPensionsRes: Either[ServiceError, Option[TransfersIntoOverseasPensionsStorageAnswers]] = Right(None),
    getShortServiceRefundsRes: Either[ServiceError, Option[ShortServiceRefundsStorageAnswers]] = Right(None)
) extends JourneyAnswersRepository {
  implicit val ec: ExecutionContext = ExecutionContext.global

  private def rightT[A](a: Either[ServiceError, Option[A]]): ApiResultT[Option[A]] = EitherT.fromEither[Future](a)

  def upsertAnswers(ctx: JourneyContext, newData: JsValue): ApiResultT[Unit] = {
    upsertAnswersList ::= newData
    EitherT.fromEither[Future](upsertDateField)
  }

  def setStatus(ctx: JourneyContext, status: JourneyStatus): ApiResultT[Unit] =
    EitherT.fromEither[Future](upsertStatusField)

  def testOnlyClearAllData(): ApiResultT[Unit] = {
    upsertAnswersList = Nil
    getAllJourneyStatuses = Nil
    EitherT.rightT[Future, ServiceError](())
  }

  def get(ctx: JourneyContext): ApiResultT[Option[JourneyAnswers]] =
    EitherT.rightT[Future, ServiceError](getAnswer)

  def getAllJourneyStatuses(taxYear: TaxYear, mtditid: Mtditid): ApiResultT[List[JourneyNameAndStatus]] =
    EitherT.rightT[Future, ServiceError](getAllJourneyStatuses)

  def getJourneyStatus(ctx: JourneyContext): ApiResultT[List[JourneyNameAndStatus]] = EitherT.rightT[Future, ServiceError](getJourneyStatus)

  def saveJourneyStatus(ctx: JourneyContext, journeyContext: JourneyContext): ApiResultT[Unit] =
    EitherT.rightT[Future, ServiceError](saveJourneyStatus)

  def getAnswers[A: Reads](ctx: JourneyContext)(implicit ct: ClassTag[A]): ApiResultT[Option[A]] =
    EitherT.rightT[Future, ServiceError](upsertAnswersList.headOption.map(_.as[A]))

  def upsertEncryptedAnswers[A](ctx: JourneyContext, storageAnswers: StorageAnswers[A]): ApiResultT[Unit] = ???

  def getDecryptedAnswers[DecAns, A <: EncryptedStorageAnswers[DecAns]: Reads](ctx: JourneyContext)(implicit
      ct: ClassTag[A]): ApiResultT[Option[DecAns]] = ???

  def getPaymentsIntoPensions(ctx: JourneyContextWithNino): ApiResultT[Option[PaymentsIntoPensionsStorageAnswers]] =
    rightT(getPaymentsIntoPensionsRes)

  def upsertPaymentsIntoPensions(ctx: JourneyContextWithNino, storageAnswers: PaymentsIntoPensionsStorageAnswers): ApiResultT[Unit] = ???

  def getUkPensionIncome(ctx: JourneyContextWithNino): ApiResultT[Option[UkPensionIncomeStorageAnswers]] =
    rightT(getUkPensionIncomeRes)

  def upsertUkPensionIncome(ctx: JourneyContextWithNino, storageAnswers: UkPensionIncomeStorageAnswers): ApiResultT[Unit] = ???

  def getStatePension(ctx: JourneyContextWithNino): ApiResultT[Option[IncomeFromPensionsStatePensionStorageAnswers]] =
    rightT(getStatePensionRes)

  def upsertStatePension(ctx: JourneyContextWithNino, storageAnswers: IncomeFromPensionsStatePensionStorageAnswers): ApiResultT[Unit] = ???

  def getAnnualAllowances(ctx: JourneyContextWithNino): ApiResultT[Option[AnnualAllowancesStorageAnswers]] =
    rightT(getAnnualAllowancesRes)

  def upsertAnnualAllowances(ctx: JourneyContextWithNino, storageAnswers: AnnualAllowancesStorageAnswers): ApiResultT[Unit] = ???

  def getUnauthorisedPayments(ctx: JourneyContextWithNino): ApiResultT[Option[UnauthorisedPaymentsStorageAnswers]] =
    rightT(getUnauthorisedPaymentsRes)

  def upsertUnauthorisedPayments(ctx: JourneyContextWithNino, storageAnswers: UnauthorisedPaymentsStorageAnswers): ApiResultT[Unit] = ???

  def getPaymentsIntoOverseasPensions(ctx: JourneyContextWithNino): ApiResultT[Option[PaymentsIntoOverseasPensionsStorageAnswers]] =
    rightT(getPaymentsIntoOverseasPensionsRes)

  def upsertPaymentsIntoOverseasPensions(ctx: JourneyContextWithNino, storageAnswers: PaymentsIntoOverseasPensionsStorageAnswers): ApiResultT[Unit] =
    ???

  def getIncomeFromOverseasPensions(ctx: JourneyContextWithNino): ApiResultT[Option[IncomeFromOverseasPensionsStorageAnswers]] =
    rightT(getIncomeFromOverseasPensionsRes)

  def upsertIncomeFromOverseasPensions(ctx: JourneyContextWithNino, storageAnswers: IncomeFromOverseasPensionsStorageAnswers): ApiResultT[Unit] = ???

  def getTransferIntoOverseasPensions(ctx: JourneyContextWithNino): ApiResultT[Option[TransfersIntoOverseasPensionsStorageAnswers]] =
    rightT(getTransferIntoOverseasPensionsRes)

  def upsertTransferIntoOverseasPensions(ctx: JourneyContextWithNino, storageAnswers: TransfersIntoOverseasPensionsStorageAnswers): ApiResultT[Unit] =
    ???

  def getShortServiceRefunds(ctx: JourneyContextWithNino): ApiResultT[Option[ShortServiceRefundsStorageAnswers]] =
    rightT(getShortServiceRefundsRes)

  def upsertShortServiceRefunds(ctx: JourneyContextWithNino, storageAnswers: ShortServiceRefundsStorageAnswers): ApiResultT[Unit] = ???
}
