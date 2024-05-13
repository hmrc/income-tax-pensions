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

package connectors.httpParsers

import org.scalatest.wordspec.AnyWordSpecLike
import DeleteHttpParser._
import play.api.http.Status._
import uk.gov.hmrc.http.HttpResponse

class DeleteHttpParserSpec extends AnyWordSpecLike {

  "read" should {
    "return Right(()) when the response status is NO_CONTENT" in {
      val result = DeleteHttpReads.read("GET", "url", HttpResponse(NO_CONTENT, ""))
      assert(result == Right(()))
    }

    "return Right(()) when the response status is NOT_FOUND" in {
      val result = DeleteHttpReads.read("GET", "url", HttpResponse(NOT_FOUND, ""))
      assert(result == Right(()))
    }
  }
}
