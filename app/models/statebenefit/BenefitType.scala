package models.statebenefit

sealed trait BenefitType {
  val value: String
}

object BenefitType {
  case object StatePension extends BenefitType {
    override val value: String = "statePension"
  }
  case object StatePensionLumpSum extends BenefitType {
    override val value: String = "statePensionLumpSum"
  }

  val values: List[BenefitType] = List(StatePension, StatePensionLumpSum)
}
