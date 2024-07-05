/*
 * Copyright 2024 HM Revenue & Customs
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

package models.database

import cats.data.EitherT
import models.error.ServiceError
import models.error.ServiceError._
import play.api.libs.json.Writes
import services.EncryptionService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait StorageAnswers[A] {
  def encrypted(implicit aesGCMCrypto: EncryptionService, textAndKeyAes: TextAndKey): EncryptedStorageAnswers[A]
}

trait EncryptedStorageAnswers[A] {
  protected[database] def unsafeDecrypted(implicit aesGCMCrypto: EncryptionService, textAndKeyAes: TextAndKey): A

  def decryptedT(aesGCMCrypto: EncryptionService, textAndKeyAes: TextAndKey)(implicit ec: ExecutionContext): EitherT[Future, ServiceError, A] =
    EitherT.fromEither[Future](decrypted(aesGCMCrypto, textAndKeyAes))

  private def decrypted(implicit aesGCMCrypto: EncryptionService, textAndKeyAes: TextAndKey): Either[ServiceError, A] =
    Try(unsafeDecrypted).toEither.left
      .map(err => CannotDecryptStorageDataError(err.getMessage))

}

object EncryptedStorageAnswers {
  implicit def writes[A]: Writes[EncryptedStorageAnswers[A]] = Writes {
    case e: EncryptedPaymentsIntoPensionsStorageAnswers           => EncryptedPaymentsIntoPensionsStorageAnswers.format.writes(e)
    case e: EncryptedUkPensionIncomeStorageAnswers                => EncryptedUkPensionIncomeStorageAnswers.format.writes(e)
    case e: EncryptedIncomeFromPensionsStatePensionStorageAnswers => EncryptedIncomeFromPensionsStatePensionStorageAnswers.format.writes(e)
    case e: EncryptedAnnualAllowancesStorageAnswers               => EncryptedAnnualAllowancesStorageAnswers.format.writes(e)
    case e: EncryptedUnauthorisedPaymentsStorageAnswers           => EncryptedUnauthorisedPaymentsStorageAnswers.format.writes(e)
    case e: EncryptedPaymentsIntoOverseasPensionsStorageAnswers   => EncryptedPaymentsIntoOverseasPensionsStorageAnswers.format.writes(e)
    case e: EncryptedIncomeFromOverseasPensionsStorageAnswers     => EncryptedIncomeFromOverseasPensionsStorageAnswers.format.writes(e)
    case e: EncryptedTransfersIntoOverseasPensionsStorageAnswers  => EncryptedTransfersIntoOverseasPensionsStorageAnswers.format.writes(e)
    case e: EncryptedShortServiceRefundsStorageAnswers            => EncryptedShortServiceRefundsStorageAnswers.format.writes(e)
  }
}
