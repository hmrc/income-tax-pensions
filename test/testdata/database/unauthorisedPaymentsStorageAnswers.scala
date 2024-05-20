package testdata.database

import models.database.UnauthorisedPaymentsStorageAnswers

object unauthorisedPaymentsStorageAnswers {
  val sampleStorageAnswers: UnauthorisedPaymentsStorageAnswers =
    UnauthorisedPaymentsStorageAnswers(Some(true), Some(true), Some(true), Some(true), Some(true))
}
