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

package utils

import models.database.TextAndKey
import models.encryption.EncryptedValue
import services.EncryptionService

import java.time.{Instant, LocalDate, Month}
import java.util.UUID

trait Encryptable[A] {
  def encrypt(value: A)(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKey): EncryptedValue
}

object EncryptorInstances {

  implicit val booleanEncryptor: Encryptable[Boolean] = new Encryptable[Boolean] {
    override def encrypt(value: Boolean)(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKey): EncryptedValue =
      aesGCMCrypto.encrypt(value)
  }

  implicit val stringEncryptor: Encryptable[String] = new Encryptable[String] {
    override def encrypt(value: String)(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKey): EncryptedValue =
      aesGCMCrypto.encrypt(value)
  }

  implicit val bigDecimalEncryptor: Encryptable[BigDecimal] = new Encryptable[BigDecimal] {
    def encrypt(value: BigDecimal)(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKey): EncryptedValue = aesGCMCrypto.encrypt(value)
  }

  implicit val monthEncryptor: Encryptable[Month] = new Encryptable[Month] {
    override def encrypt(value: Month)(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKey): EncryptedValue =
      aesGCMCrypto.encrypt(value)
  }

  implicit val instantEncryptor: Encryptable[Instant] = new Encryptable[Instant] {
    override def encrypt(value: Instant)(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKey): EncryptedValue =
      aesGCMCrypto.encrypt(value)
  }

  implicit val localDateEncryptor: Encryptable[LocalDate] = new Encryptable[LocalDate] {
    override def encrypt(value: LocalDate)(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKey): EncryptedValue =
      aesGCMCrypto.encrypt(value)
  }

  implicit val uuidEncryptor: Encryptable[UUID] = new Encryptable[UUID] {
    override def encrypt(value: UUID)(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKey): EncryptedValue =
      aesGCMCrypto.encrypt(value)
  }
}

object EncryptableSyntax {
  implicit class EncryptableOps[A](value: A)(implicit e: Encryptable[A]) {
    def encrypted(implicit aesGCMCrypto: EncryptionService, textAndKey: TextAndKey): EncryptedValue = e.encrypt(value)
  }
}
