package testdata

import models.frontend.UnauthorisedPaymentsAnswers

object unauthorisedPayments {
  val unauthorisedPaymentsAnswers = UnauthorisedPaymentsAnswers(
    Some(true),
    Some(true),
    Some(1.0),
    Some(true),
    Some(2.0),
    Some(3.0),
    Some(true),
    Some(4.0),
    Some(true),
    Some(List("pstr1"))
  )
}
