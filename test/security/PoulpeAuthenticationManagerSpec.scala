package security

import play.api.mvc.Results._
import play.api.mvc.{Action, SimpleResult}
import play.api.test.{FakeApplication, PlaySpecification, WithServer}
import util.TestHelpers

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
      case ("GET", url) if url == authUrl =>
        Action {
          result
        }
    }
  )

  "authentication manager" should {

    "authenticate valid user" in new WithServer(getFakeAppWithResponse(Ok(successResponse)), port) {
      poulpeAuthManager.authenticate("admin", "123") must beSome.which {
        user:UserInfo => user.username == "admin" && user.firstName.isDefined
      }.await
    }

    "not perform authentication for invalid credentials" in
      new WithServer(getFakeAppWithResponse(NotFound(invalidCredentialsResponse)), port) {
      poulpeAuthManager.authenticate("admin", "invalidPassword") must beNone.await
    }

    "not authenticate disabled user" in new WithServer(getFakeAppWithResponse(Ok(disabledUserResponse)), port) {
      poulpeAuthManager.authenticate("admin", "123") must beNone.await
    }
  }

}
