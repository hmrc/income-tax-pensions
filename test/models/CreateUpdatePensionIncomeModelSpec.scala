package models

import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.wordspec.AnyWordSpecLike

class CreateUpdatePensionIncomeModelSpec extends AnyWordSpecLike {
  "nonEmpty" should {
    val cases = Table(
      ("foreignPension", "overseasContribution", "expected"),
      (None, None, false),
      (Some(Nil), None, false),
      (None, Some(Nil), false),
      (Some(Nil), Some(Nil), false),
      (Some(List(ForeignPension("", 1.0, None, None, None, None))), Some(Nil), true),
      (Some(Nil), Some(List(OverseasPensionContribution(Some("str"), 1.0, None, None, None, None, None, None))), true)
    )

    forAll(cases) { (foreignPension, overseasContribution, expected) =>
      s"return $expected for $foreignPension and $overseasContribution" in {
        val model = CreateUpdatePensionIncomeModel(foreignPension, overseasContribution)
        assert(model.nonEmpty == expected)
      }
    }

  }
}
