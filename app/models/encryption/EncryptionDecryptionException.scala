package models.encryption

class EncryptionDecryptionException(method: String, reason: String, message: String) extends RuntimeException(message) {
  val failureReason          = s"$reason for $method"
  val failureMessage: String = message
}
