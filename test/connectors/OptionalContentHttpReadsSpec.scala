package connectors

import cats.implicits._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsNumber, JsString, Json}
import uk.gov.hmrc.http.HttpResponse

class OptionalContentHttpReadsSpec extends AnyWordSpecLike with Matchers {
  val underTest = new OptionalContentHttpReads[String]

  "read" should {
    "return None when no content" in {
      val res = underTest.read("method", "url", HttpResponse(204, ""))
      res shouldBe None.asRight
    }

    "return Some when there is successful response with a content" in {
      val res = underTest.read("method", "url", HttpResponse(200, Json.stringify(JsString("some content"))))
      res shouldBe "some content".some.asRight
    }

    "fail to parse incorrect value" in {
      val res = underTest.read("method", "url", HttpResponse(200, Json.stringify(JsNumber(42))))
      res shouldBe a[Left[_, _]]
    }
  }
}