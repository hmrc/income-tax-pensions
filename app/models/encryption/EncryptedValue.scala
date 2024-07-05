package models.encryption

import play.api.libs.json.{Json, OFormat}

case class EncryptedValue(value: String, nonce: String)
object EncryptedValue {
  implicit val cryptoFormat: OFormat[EncryptedValue] = Json.format[EncryptedValue]
}
