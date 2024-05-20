package testdata.connector

import models.{Charge, PensionSchemeUnauthorisedPayments}

object charges {
  val sampleCharges = PensionSchemeUnauthorisedPayments(
    pensionSchemeTaxReference = Some(List("00123456RA")),
    surcharge = Some(
      Charge(
        amount = 1.0,
        foreignTaxPaid = 2.0
      )),
    noSurcharge = Some(
      Charge(
        amount = 3.0,
        foreignTaxPaid = 4.0
      ))
  )

  val sampleChargesWithNones = PensionSchemeUnauthorisedPayments(
    pensionSchemeTaxReference = Some(List("00123456RA")),
    surcharge = None,
    noSurcharge = None
  )

}
