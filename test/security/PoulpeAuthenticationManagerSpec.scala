package security

import util.TestHelpers
import play.api.test.{WithServer, FakeApplication, PlaySpecification}
import play.api.mvc.{SimpleResult, Action}
import scala.concurrent._
import scala.concurrent.duration.Duration
import play.api.mvc.Results._

class PoulpeAuthenticationManagerSpec extends PlaySpecification {

  val port = TestHelpers.findFreePort()
  val poulpeUrl = "http://127.0.0.1:" + port
  val authUrl = "/rest/authenticate"
  var poulpeAuthManager = new PoulpeAuthenticationManager(poulpeUrl)

  val successResponse =
    """
      |<authentication xmlns="http://www.jtalks.org/namespaces/1.0">
      |    <credentials>
      |        <username>admin</username>
      |    </credentials>
      |    <status>success</status>
      |    <profile>
      |        <email>admin@jtalks.org</email>
      |        <firstName>name</firstName>
      |        <enabled>true</enabled>
      |    </profile>
      |</authentication>
    """.stripMargin

  val disabledUserResponse =
    """
      |<authentication xmlns="http://www.jtalks.org/namespaces/1.0">
      |    <credentials>
      |        <username>admin</username>
      |    </credentials>
      |    <status>success</status>
      |    <profile>
      |        <email>admin@jtalks.org</email>
      |        <firstName>name</firstName>
      |        <enabled>false</enabled>
      |    </profile>
      |</authentication>
    """.stripMargin

  val invalidCredentialsResponse =
    """
      |<authentication xmlns="http://www.jtalks.org/namespaces/1.0">
      |    <credentials>
      |        <username>admin</username>
      |    </credentials>
      |    <status>fail</status>
      |    <statusInfo>Incorrect username or password</statusInfo>
      |</authentication>
    """.stripMargin

  def getFakeAppWithResponse(result: SimpleResult) = FakeApplication(
    withRoutes = {
      case ("GET", authUrl) => 
        Action {
          result
        }
    }
  )

  "authentication manager" should {

    "authenticate valid user" in new WithServer(getFakeAppWithResponse(Ok(successResponse)), port) {
      var result = Await.result(poulpeAuthManager.authenticate("admin", "123"), Duration.create(10, "seconds"))

      result.get.username must equalTo("admin")
      result.get.firstName must not be empty
    }

    "not perform authentication for invalid credentials" in
      new WithServer(getFakeAppWithResponse(NotFound(invalidCredentialsResponse)), port) {
      var result = Await.result(poulpeAuthManager.authenticate("admin", "invalidPassword"), Duration.create(10, "seconds"))

      result must be(None)
    }

    "not authenticate disabled user" in new WithServer(getFakeAppWithResponse(Ok(disabledUserResponse)), port) {
      var result = Await.result(poulpeAuthManager.authenticate("admin", "123"), Duration.create(10, "seconds"))

      result must be(None)
    }
  }

}
