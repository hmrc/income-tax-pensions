package models.charges

import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.wordspec.AnyWordSpecLike

class CreateUpdatePensionChargesRequestModelSpec extends AnyWordSpecLike {

  "nonEmpty" should {
    val empty = CreateUpdatePensionChargesRequestModel.empty

    val cases = Table(
      ("model", "expected"),
      (empty, false),
      (empty.copy(pensionSavingsTaxCharges = Some(PensionSavingsTaxCharges.empty.copy(pensionSchemeTaxReference = Some(List("some value"))))), true),
      (
        empty.copy(pensionSchemeOverseasTransfers =
          Some(PensionSchemeOverseasTransfers.empty.copy(overseasSchemeProvider = List(OverseasSchemeProvider("str1", "str2", "str3", None, None))))),
        true),
      (
        empty.copy(pensionSchemeUnauthorisedPayments =
          Some(PensionSchemeUnauthorisedPayments.empty.copy(pensionSchemeTaxReference = Some(List("some value"))))),
        true),
      (empty.copy(pensionContributions = Some(PensionContributions.empty.copy(pensionSchemeTaxReference = List("some value")))), true),
      (
        empty.copy(overseasPensionContributions =
          Some(OverseasPensionContributions.empty.copy(overseasSchemeProvider = List(OverseasSchemeProvider("str1", "str2", "str3", None, None))))),
        true)
    )

    forAll(cases) { (model, expected) =>
      s"return $expected for $model" in {
        assert(model.nonEmpty == expected)
      }
    }
  }
}
