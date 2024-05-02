/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import cats.implicits.catsSyntaxEitherId
import models.submission.EmploymentPensions
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout}
import services.LoadSubmittedDataService
import stubs.services.StubLoadSubmittedDataService
import utils.EmploymentPensionsBuilder.employmentPensionsData
import utils.FutureUtils.FutureOps
import utils.TestUtils

import scala.concurrent.Future

class LoadSubmittedDataControllerSpec extends TestUtils {

  "getting employment pensions" when {
    "service call is successful" when {
      "the returned EmploymentPensions model is populated" must {
        "return 200 OK with the model" in new Test {
          val result: Future[Result] =
            controller(service)
              .loadEmploymentPension(nino, taxYear)
              .apply(fakeRequest)

          val expectedJsonResp: JsValue =
            Json.toJson(employmentPensionsData)

          status(result) shouldBe OK
          contentAsJson(result) shouldBe expectedJsonResp
        }
      }
      "returns an empty EmploymentPensions model" must {
        "return a 204 NO_CONTENT" in new Test {
          override val service: StubLoadSubmittedDataService =
            new StubLoadSubmittedDataService(
              loadEmploymentResult = EmploymentPensions.empty.asRight.toFuture
            )

          val result: Future[Result] =
            controller(service)
              .loadEmploymentPension(nino, taxYear)
              .apply(fakeRequest)

          status(result) shouldBe NO_CONTENT
        }
      }
    }
    "service call is unsuccessful and returns a ServiceErrorModel" must {
      "return the error status" in new Test {
        override val service: StubLoadSubmittedDataService =
          new StubLoadSubmittedDataService(
            loadEmploymentResult = someServiceError.asLeft[EmploymentPensions].toFuture
          )

        val result: Future[Result] =
          controller(service)
            .loadEmploymentPension(nino, taxYear)
            .apply(fakeRequest)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }

  trait Test {
    mockAuth()

    val service: StubLoadSubmittedDataService =
      new StubLoadSubmittedDataService()

    def controller(service: LoadSubmittedDataService): LoadSubmittedDataController =
      new LoadSubmittedDataController(service, authorisedAction, mockControllerComponents)
  }

}
