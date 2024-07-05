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

package stubs.services

import models.common.Mtditid
import models.database.{EncryptedStorageAnswers, StorageAnswers, TextAndKeyAes}
import models.encryption.EncryptedValue
import models.error.ServiceError
import services.EncryptionService
import utils.TypeCaster

case class StubEncryptionService() extends EncryptionService {
  def encryptUserData[A](mtditid: Mtditid, value: StorageAnswers[A]): Either[ServiceError, EncryptedStorageAnswers[A]] = ???

  def encrypt[A](valueToEncrypt: A)(implicit textAndKey: TextAndKeyAes): EncryptedValue =
    EncryptedValue(s"encrypted-$valueToEncrypt", "nonce")

  def decrypt[A](valueToDecrypt: String, nonce: String)(implicit textAndKey: TextAndKeyAes, converter: TypeCaster.Converter[A]): A = {
    val decryptedValue = valueToDecrypt.replaceFirst("encrypted-", "")
    converter.convert(decryptedValue)
  }

}
