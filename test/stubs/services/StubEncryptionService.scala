package stubs.services

import models.common.Mtditid
import models.database.{EncryptedStorageAnswers, StorageAnswers, TextAndKey}
import models.encryption.EncryptedValue
import models.error.ServiceError
import services.EncryptionService
import utils.TypeCaster

case class StubEncryptionService() extends EncryptionService {
  def encryptUserData[A](mtditid: Mtditid, value: StorageAnswers[A]): Either[ServiceError, EncryptedStorageAnswers[A]] = ???

  def encrypt[A](valueToEncrypt: A)(implicit textAndKey: TextAndKey): EncryptedValue =
    EncryptedValue(s"encrypted-$valueToEncrypt", "nonce")

  def decrypt[A](valueToDecrypt: String, nonce: String)(implicit textAndKey: TextAndKey, converter: TypeCaster.Converter[A]): A = {
    val decryptedValue = valueToDecrypt.replaceFirst("encrypted-", "")
    converter.convert(decryptedValue)
  }

}
