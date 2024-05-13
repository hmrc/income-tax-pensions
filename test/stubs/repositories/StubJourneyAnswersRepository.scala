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
import models.database.JourneyAnswers
import models.domain.ApiResultT
import models.error.ServiceError
import play.api.libs.json.{JsValue, Reads}
import repositories.JourneyAnswersRepository

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

case class StubJourneyAnswersRepository(
    getAnswer: Option[JourneyAnswers] = None,
    getAllJourneyStatuses: List[JourneyNameAndStatus] = List.empty,
    getJourneyStatus: List[JourneyNameAndStatus] = List.empty,
    saveJourneyStatus: Unit = Right(()),
    upsertDateField: Either[ServiceError, Unit] = Right(()),
    var upsertAnswersList: List[JsValue] = Nil,
    upsertStatusField: Either[ServiceError, Unit] = Right(())
) extends JourneyAnswersRepository {
  implicit val ec: ExecutionContext = ExecutionContext.global

  def upsertAnswers(ctx: JourneyContext, newData: JsValue): ApiResultT[Unit] = {
    upsertAnswersList ::= newData
    EitherT.fromEither[Future](upsertDateField)
  }

  def setStatus(ctx: JourneyContext, status: JourneyStatus): ApiResultT[Unit] =
    EitherT.fromEither[Future](upsertStatusField)

  def testOnlyClearAllData(): ApiResultT[Unit] = {
    upsertAnswersList = Nil
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
}
