package services

import models.AllStateBenefitsData
import models.common.JourneyContextWithNino
import models.statebenefit.{ClaimCYAModel, StateBenefitsUserData}
import org.scalatest.EitherValues._
import org.scalatest.wordspec.AnyWordSpecLike
import services.StateBenefitServiceImplSpec.{TestCase, stubbedNow}
import stubs.connectors.StubStateBenefitsConnector
import testdata.common
import testdata.connector.stateBenefits
import testdata.frontend.incomeFromPensionsStatePensionAnswers
import utils.EitherTTestOps.convertScalaFuture
import utils.TestUtils.hc

import java.time.{Instant, LocalDate}
import scala.concurrent.ExecutionContext.Implicits.global

class StateBenefitServiceImplSpec extends AnyWordSpecLike {

  val ctx = JourneyContextWithNino(common.taxYear, common.mtditid, common.nino)

  "getStateBenefits" should {
    "return an empty object if no answers downstream" in new TestCase {
      val result = service.getStateBenefits(ctx).value.futureValue.value
      assert(result === AllStateBenefitsData.empty)
    }

    "return answers from downstream" in new TestCase {
      override val service = new StateBenefitServiceImpl(StubStateBenefitsConnector(stateBenefitsResults = Some(stateBenefits.allStateBenefitsData)))
      val result           = service.getStateBenefits(ctx).value.futureValue.value
      assert(result === stateBenefits.allStateBenefitsData)
    }
  }

  "saveClaim" should {
    "save claim when no claim exists" in new TestCase {
      val actualClaims = (for {
        _ <- service.upsertStateBenefits(ctx, incomeFromPensionsStatePensionAnswers.sample)
        claims = stateBenefitsConnector.claims
      } yield claims).value.futureValue.value

      val expected = List(
        StateBenefitsUserData(
          "statePensionLumpSum",
          None,
          "sessionId",
          "mtditid",
          "123456789",
          2021,
          "customerOverride",
          Some(ClaimCYAModel(Some(common.uuid), LocalDate.parse("2019-04-23"), Some(300.0), Some(400.0))),
          stubbedNow
        ),
        StateBenefitsUserData(
          "statePension",
          None,
          "sessionId",
          "mtditid",
          "123456789",
          2021,
          "customerOverride",
          Some(ClaimCYAModel(Some(common.uuid), LocalDate.parse("2019-04-23"), Some(300.0), Some(400.0))),
          stubbedNow
        )
      )

      assert(actualClaims === expected)
    }
  }
}

object StateBenefitServiceImplSpec {
  val stubbedNow = Instant.now()

  trait TestCase {
    val stateBenefitsConnector = StubStateBenefitsConnector()
    val service = new StateBenefitServiceImpl(stateBenefitsConnector) {
      override def now: Instant = stubbedNow
    }
  }
}
