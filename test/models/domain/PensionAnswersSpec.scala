package models.domain

import models.common.JourneyStatus
import models.common.JourneyStatus._
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import org.scalatest.wordspec.AnyWordSpecLike

class PensionAnswersSpec extends AnyWordSpecLike {

  "getStatus" should {
    val cases = Table[Option[JourneyStatus], Boolean, JourneyStatus](
      ("persistedStatus", "isFinished", "expected"),
      (None, false, CheckOurRecords),
      (None, true, NotStarted),
      (Some(CheckOurRecords), false, CheckOurRecords),
      (Some(CheckOurRecords), true, NotStarted),
      (Some(NotStarted), false, CheckOurRecords),
      (Some(NotStarted), true, NotStarted),
      (Some(InProgress), false, CheckOurRecords),
      (Some(InProgress), true, InProgress),
      (Some(Completed), false, CheckOurRecords),
      (Some(Completed), true, Completed),
      (Some(UnderMaintenance), false, UnderMaintenance),
      (Some(UnderMaintenance), true, UnderMaintenance)
    )

    "calculate status based on persisted status and isFinished answers" in forAll(cases) { case (persistedStatus, finished, expected) =>
      val pensionAnswers = new PensionAnswers {
        def isFinished: Boolean = finished
      }

      assert(pensionAnswers.getStatus(persistedStatus) === expected)
    }
  }
}
