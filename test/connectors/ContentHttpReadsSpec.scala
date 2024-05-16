package connectors

import models.ServiceErrorModel
import models.error.ServiceError.{CannotParseJsonError, CannotReadJsonError}
import models.frontend.PaymentsIntoPensionsAnswers
import org.scalatest.EitherValues._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.Reads._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse

class ContentHttpReadsSpec extends AnyWordSpecLike with Matchers {

  "read" should {
    val okResponse = HttpResponse(
      200,
      _: String,
      Map.empty
    )

    "parse json" in {
      val json   = """{"foo": "bar"}"""
      val result = ContentHttpReads.readOne[JsValue]("GET", "url", okResponse(json))

      result shouldBe Right(Json.parse(json))
    }

    "return an error if invalid json" in {
      val json   = """{"foo": "bar"""
      val result = ContentHttpReads.readOne[JsValue]("GET", "url", okResponse(json))

      assert(result === ServiceErrorModel.parsingError)
    }

    "return an error if cannot be deserialized to a case class" in {
      val json   = """{"foo": "bar"}"""
      val result = ContentHttpReads.readOne[PaymentsIntoPensionsAnswers]("GET", "url", okResponse(json))

      result.left.value shouldBe a[CannotReadJsonError]
    }

  }
}
