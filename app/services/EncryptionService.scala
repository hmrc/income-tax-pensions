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

import config.AppConfig
import models.common.Mtditid
import models.database.{EncryptedStorageAnswers, StorageAnswers, TextAndKey}
import models.encryption.{EncryptedValue, EncryptionDecryptionException}
import models.error.ServiceError
import models.error.ServiceError.CannotEncryptStorageDataError
import utils.TypeCaster.Converter

import java.security.{InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, SecureRandom}
import java.util.Base64
import javax.crypto._
import javax.crypto.spec.{GCMParameterSpec, SecretKeySpec}
import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success, Try}

trait EncryptionService {
  def encryptUserData[A](mtditid: Mtditid, value: StorageAnswers[A]): Either[ServiceError, EncryptedStorageAnswers[A]]
  def encrypt[A](valueToEncrypt: A)(implicit textAndKeyAes: TextAndKey): EncryptedValue
  def decrypt[A](valueToDecrypt: String, nonce: String)(implicit textAndKeyAes: TextAndKey, converter: Converter[A]): A
}

@Singleton
class AesGCMCryptoEncryptionService @Inject() (appConfig: AppConfig) extends EncryptionService {
  private val IV_SIZE                       = 96
  private val TAG_BIT_LENGTH                = 128
  private val ALGORITHM_TO_TRANSFORM_STRING = "AES/GCM/NoPadding"
  private lazy val secureRandom             = new SecureRandom()
  private val ALGORITHM_KEY                 = "AES"
  private val METHOD_ENCRYPT                = "encrypt"
  private val METHOD_DECRYPT                = "decrypt"

  def encryptUserData[A](mtditid: Mtditid, value: StorageAnswers[A]): Either[ServiceError, EncryptedStorageAnswers[A]] = {
    val textAndKeyAes: TextAndKey = TextAndKey(mtditid.value, appConfig.encryptionKey)

    Try(value.encrypted(this, textAndKeyAes)).toEither.left.map(err => CannotEncryptStorageDataError(err.getMessage))
  }

  private def getCipherInstance: Cipher = Cipher.getInstance(ALGORITHM_TO_TRANSFORM_STRING)

  def encrypt[A](valueToEncrypt: A)(implicit textAndKeyAes: TextAndKey): EncryptedValue =
    if (appConfig.useEncryption) {
      val initialisationVector = generateInitialisationVector
      val nonce                = new String(Base64.getEncoder.encode(initialisationVector))
      val gcmParameterSpec     = new GCMParameterSpec(TAG_BIT_LENGTH, initialisationVector)
      val secretKey            = validateSecretKey(textAndKeyAes.aesKey, METHOD_ENCRYPT)
      val cipherText =
        generateCipherText(valueToEncrypt.toString, validateAssociatedText(textAndKeyAes.associatedText, METHOD_ENCRYPT), gcmParameterSpec, secretKey)
      EncryptedValue(cipherText, nonce)
    } else {
      EncryptedValue(valueToEncrypt.toString, s"${valueToEncrypt.toString}-Nonce")
    }

  def decrypt[A](valueToDecrypt: String, nonce: String)(implicit textAndKeyAes: TextAndKey, converter: Converter[A]): A =
    if (appConfig.useEncryption) {
      val initialisationVector = Base64.getDecoder.decode(nonce)
      val gcmParameterSpec     = new GCMParameterSpec(TAG_BIT_LENGTH, initialisationVector)
      val secretKey            = validateSecretKey(textAndKeyAes.aesKey, METHOD_DECRYPT)

      Try {
        decryptCipherText(valueToDecrypt, validateAssociatedText(textAndKeyAes.associatedText, METHOD_DECRYPT), gcmParameterSpec, secretKey)
      }.toEither match {
        case Left(exception) => throw exception
        case Right(value)    => converter.convert(value)
      }
    } else {
      converter.convert(valueToDecrypt)
    }

  private def generateInitialisationVector: Array[Byte] = {
    val iv = new Array[Byte](IV_SIZE)
    secureRandom.nextBytes(iv)
    iv
  }

  private def validateSecretKey(key: String, method: String): SecretKey = Try {
    val decodedKey = Base64.getDecoder.decode(key)
    new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM_KEY)
  } match {
    case Success(secretKey) => secretKey
    case Failure(ex)        => throw new EncryptionDecryptionException(method, "The key provided is invalid", ex.getMessage)
  }

  def generateCipherText(valueToEncrypt: String, associatedText: Array[Byte], gcmParameterSpec: GCMParameterSpec, secretKey: SecretKey): String =
    Try {
      val cipher = getCipherInstance
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec, new SecureRandom())
      cipher.updateAAD(associatedText)
      cipher.doFinal(valueToEncrypt.getBytes)
    } match {
      case Success(cipherTextBytes) => new String(Base64.getEncoder.encode(cipherTextBytes))
      case Failure(ex)              => throw processCipherTextFailure(ex, METHOD_ENCRYPT)
    }

  def decryptCipherText(valueToDecrypt: String, associatedText: Array[Byte], gcmParameterSpec: GCMParameterSpec, secretKey: SecretKey): String =
    Try {
      val cipher = getCipherInstance
      cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec, new SecureRandom())
      cipher.updateAAD(associatedText)
      cipher.doFinal(Base64.getDecoder.decode(valueToDecrypt))
    } match {
      case Success(value) => new String(value)
      case Failure(ex)    => throw processCipherTextFailure(ex, METHOD_DECRYPT)
    }

  private def validateAssociatedText(associatedText: String, method: String): Array[Byte] =
    associatedText match {
      case text if text.nonEmpty => text.getBytes
      case _ => throw new EncryptionDecryptionException(method, "associated text must not be null", "associated text was not defined")
    }

  private def processCipherTextFailure(ex: Throwable, method: String): Throwable = ex match {
    case e: NoSuchAlgorithmException =>
      throw new EncryptionDecryptionException(method, "Algorithm being requested is not available in this environment", e.getMessage)
    case e: NoSuchPaddingException =>
      throw new EncryptionDecryptionException(method, "Padding Scheme being requested is not available this environment", e.getMessage)
    case e: InvalidKeyException =>
      throw new EncryptionDecryptionException(
        method,
        "Key being used is not valid." +
          " It could be due to invalid encoding, wrong length or uninitialized",
        e.getMessage)
    case e: InvalidAlgorithmParameterException =>
      throw new EncryptionDecryptionException(method, "Algorithm parameters being specified are not valid", e.getMessage)
    case e: IllegalStateException => throw new EncryptionDecryptionException(method, "Cipher is in an illegal state", e.getMessage)
    case e: UnsupportedOperationException =>
      throw new EncryptionDecryptionException(method, "Provider might not be supporting this method", e.getMessage)
    case e: IllegalBlockSizeException => throw new EncryptionDecryptionException(method, "Error occured due to block size", e.getMessage)
    case e: BadPaddingException       => throw new EncryptionDecryptionException(method, "Error occured due to padding scheme", e.getMessage)
    case _                            => throw new EncryptionDecryptionException(method, "Unexpected exception", ex.getMessage)
  }

}
