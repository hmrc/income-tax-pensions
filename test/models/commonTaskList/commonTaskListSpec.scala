package models.commonTaskList

import models.domain.AllJourneys
import org.scalatest.wordspec.AnyWordSpecLike
import testdata.appConfig.createAppConfig
import testdata.commonTaskList.emptyCommonTaskListModel
import utils.TestUtils.currTaxYear

class commonTaskListSpec extends AnyWordSpecLike {

  "fromAllJourneys" should {
    "generate TaskListModel in checkNow status for no data in journeys" in {
      val all       = AllJourneys.empty
      val appConfig = createAppConfig()
      val url       = appConfig.incomeTaxPensionsFrontendUrl

      val actual = fromAllJourneys(all, url, currTaxYear)

      assert(actual === emptyCommonTaskListModel(currTaxYear))
    }
  }
}
