package testdata.connector

import models.employment.CreateUpdateEmploymentRequest
import models.employment.CreateUpdateEmploymentRequest.{CreateUpdateEmployment, CreateUpdateEmploymentData, PayModel}

object employment {

  val fullEmploymentRequest: CreateUpdateEmploymentRequest = CreateUpdateEmploymentRequest(
    Some("employmentId"),
    Some(
      CreateUpdateEmployment(
        Some("ref"),
        "employer",
        "2020-01-01",
        Some("2020-01-02"),
        Some("payrollId")
      )),
    Some(
      CreateUpdateEmploymentData(
        PayModel(1.0, 2.0)
      )),
    Some(true)
  )

}
