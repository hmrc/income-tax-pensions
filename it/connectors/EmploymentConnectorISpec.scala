package connectors

import cats.implicits.{catsSyntaxEitherId, catsSyntaxOptionId}
import config.BackendAppConfig
import helpers.WiremockSpec
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.Configuration
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.AllEmploymentsDataBuilder.allEmploymentsData

class EmploymentConnectorISpec extends WiremockSpec {

  val httpClient     = app.injector.instanceOf[HttpClient]
  val configuration  = app.injector.instanceOf[Configuration]
  val servicesConfig = app.injector.instanceOf[ServicesConfig]

  val appConfig = new BackendAppConfig(configuration, servicesConfig) {
    override val employmentBaseUrl: String = s"http://localhost:$wireMockPort"
  }

  implicit val hc = HeaderCarrier()

  val nino    = "AA000001B"
  val taxYear = 2024

  val downstreamUrl = s"/income-tax/nino/$nino/sources\\?taxYear=$taxYear"

  val connector = new EmploymentConnector(httpClient, appConfig)

  "getEmployments" must {
    "return a success response" in {
      stubGetWithResponseBody(
        url = downstreamUrl,
        status = OK,
        response = Json.toJson(allEmploymentsData).toString()
      )

      connector.getEmployments(nino, taxYear).futureValue shouldBe allEmploymentsData.some.asRight
    }
  }

}
