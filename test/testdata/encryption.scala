package testdata

import models.database.TextAndKey
import models.encryption.EncryptedValue

object encryption {
  val textAndKey     = TextAndKey("some-test", "some-key")
  val encryptedFalse = EncryptedValue("encrypted-false", "nonce")
  val encryptedTrue  = EncryptedValue("encrypted-true", "nonce")
}
