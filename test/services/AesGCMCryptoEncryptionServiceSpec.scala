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

package services

import com.codahale.metrics.SharedMetricRegistries
import config.AppConfig
import models.database.TextAndKey
import models.encryption.{EncryptedValue, EncryptionDecryptionException}
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import utils.TypeCaster.Converter.stringLoader

import java.util.Base64
import scala.util.Try

class AesGCMCryptoEncryptionServiceSpec extends AnyWordSpecLike {
  SharedMetricRegistries.clear()

  def createUnderTest(config: Configuration = Configuration()): AesGCMCryptoEncryptionService = {
    val app                  = new GuiceApplicationBuilder().configure(config).build()
    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    new AesGCMCryptoEncryptionService(appConfig)
  }

  private val secretKey      = "VqmXp7yigDFxbCUdDdNZVIvbW6RgPNJsliv6swQNCL8="
  private val secretKey2     = "cXo7u0HuJK8B/52xLwW7eQ=="
  private val textToEncrypt  = "textNotEncrypted"
  private val associatedText = "associatedText"
  private val encryptedTextTest: EncryptedValue = EncryptedValue(
    "jOrmajkEqb7Jbo1GvK4Mhc3E7UiOfKS3RCy3O/F6myQ=",
    "WM1yMH4KBGdXe65vl8Gzd37Ob2Bf1bFUSaMqXk78sNeorPFOSWwwhOj0Lcebm5nWRhjNgL4K2SV3GWEXyyqeIhWQ4fJIVQRHM9VjWCTyf7/1/f/ckAaMHqkF1XC8bnW9"
  )

  implicit val textAndKeyAes: TextAndKey = TextAndKey(associatedText, secretKey)

  val underTest = createUnderTest()

  "encrypt" should {
    "return plain text when turned off" in {
      val config = Configuration(
        "useEncryption" -> "false"
      )

      val underTest     = createUnderTest(config)
      val encryptedText = underTest.encrypt(textToEncrypt)

      assert(encryptedText.value === textToEncrypt)
    }

    "return encrypted and then decrypted should return the original text" in {
      val encryptedText = underTest.encrypt(textToEncrypt)
      val decryptedText = underTest.decrypt(encryptedText.value, encryptedText.nonce)

      assert(encryptedText !== textToEncrypt)
      assert(decryptedText === textToEncrypt)
    }

    "return an EncryptionDecryptionError if the associated text is an empty string" in {
      implicit val textAndKeyAes: TextAndKey = TextAndKey("", secretKey)

      val actual = Try(underTest.encrypt(textToEncrypt)).failed.get
      assert(actual.isInstanceOf[EncryptionDecryptionException])
      assert(actual.getMessage === "associated text was not defined")
    }

    "return an EncryptionDecryptionError if the key is empty" in {
      implicit val textAndKeyAes: TextAndKey = TextAndKey(associatedText, "")

      val actual = Try(underTest.encrypt(textToEncrypt)).failed.get

      assert(actual.isInstanceOf[EncryptionDecryptionException])
      assert(actual.asInstanceOf[EncryptionDecryptionException].failureReason === "The key provided is invalid for encrypt")
    }

    "return an EncryptionDecryptionError if the key is invalid" in {
      implicit val textAndKeyAes: TextAndKey = TextAndKey(associatedText, "invalidKey")

      val actual = Try(underTest.encrypt(textToEncrypt)).failed.get.asInstanceOf[EncryptionDecryptionException]

      assert(
        actual.failureReason.contains("Key being used is not valid." +
          " It could be due to invalid encoding, wrong length or uninitialized"))
    }
  }

  "decrypt" should {
    "return plain text when turned off" in {
      val config = Configuration(
        "useEncryption" -> "false"
      )

      val underTest = createUnderTest(config)
      val actual    = underTest.decrypt(textToEncrypt, "nonce")
      assert(actual === textToEncrypt)
    }

    "decrypt an encrypted value when the encrytedValue, associatedText, nonce, and secretKey are the same used for encryption" in {
      val decryptedText = underTest.decrypt(encryptedTextTest.value, encryptedTextTest.nonce)
      assert(decryptedText === textToEncrypt)
    }

    "return a EncryptionDecryptionException if the encrytedValue is different" in {
      val decryptedAttempt = intercept[EncryptionDecryptionException](
        underTest.decrypt(Base64.getEncoder.encodeToString("diffentvalues".getBytes), encryptedTextTest.nonce)
      )
      assert(decryptedAttempt.failureReason.contains("Error occured due to padding scheme"))
    }

    "return a EncryptionDecryptionException if the nonce is different" in {
      val decryptedAttempt = intercept[EncryptionDecryptionException](
        underTest.decrypt(encryptedTextTest.value, Base64.getEncoder.encodeToString("jdbfjdgvcjksabcvajbvjkbvjbdvjbvjkabv".getBytes))
      )
      assert(decryptedAttempt.failureReason.contains("Error occured due to padding scheme"))
    }

    "return a EncryptionDecryptionException if the associatedText is different" in {
      implicit val textAndKeyAes: TextAndKey = TextAndKey("idsngfbsadjvbdsvjb", secretKey)

      val decryptedAttempt = intercept[EncryptionDecryptionException](
        underTest.decrypt(encryptedTextTest.value, encryptedTextTest.nonce)
      )
      assert(decryptedAttempt.failureReason.contains("Error occured due to padding scheme"))
    }

    "return a EncryptionDecryptionException if the secretKey is different" in {
      implicit val textAndKeyAes: TextAndKey = TextAndKey("idsngfbsadjvbdsvjb", secretKey2)

      val decryptedAttempt = intercept[EncryptionDecryptionException](
        underTest.decrypt(encryptedTextTest.value, encryptedTextTest.nonce)
      )
      assert(decryptedAttempt.failureReason.contains("Error occured due to padding scheme"))
    }

    "return an EncryptionDecryptionError if the associated text is an empty string" in {
      val emptyAssociatedText = ""

      implicit val textAndKeyAes: TextAndKey = TextAndKey(emptyAssociatedText, secretKey2)

      val decryptedAttempt = intercept[EncryptionDecryptionException](
        underTest.decrypt(encryptedTextTest.value, encryptedTextTest.nonce)
      )
      assert(decryptedAttempt.failureReason.contains("associated text must not be null"))
    }

    "return an EncryptionDecryptionError if the key is empty" in {
      val invalidSecretKey = ""

      implicit val textAndKeyAes: TextAndKey = TextAndKey(associatedText, invalidSecretKey)

      val decryptedAttempt = intercept[EncryptionDecryptionException](
        underTest.decrypt(encryptedTextTest.value, encryptedTextTest.nonce)
      )
      assert(decryptedAttempt.failureReason.contains("The key provided is invalid"))
    }

    "return an EncryptionDecryptionError if the key is invalid" in {
      val invalidSecretKey = "invalidKey"

      implicit val textAndKeyAes: TextAndKey = TextAndKey(associatedText, invalidSecretKey)

      val decryptedAttempt = intercept[EncryptionDecryptionException](
        underTest.decrypt(encryptedTextTest.value, encryptedTextTest.nonce)
      )
      assert(
        decryptedAttempt.failureReason.contains("Key being used is not valid." +
          " It could be due to invalid encoding, wrong length or uninitialized"))
    }
  }

}
