package security

import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import org.specs2.specification.mutable.ExecutionEnvironment
import play.api.mvc.Action
import play.api.test.FakeApplication
import util.PortFinder

class PoulpeAuthenticationManagerSpec extends Specification with ExecutionEnvironment {

  type HttpResult = play.api.mvc.Result

  def is(implicit ee: ExecutionEnv) = {
  val port = PortFinder.findFreePort()
  val poulpeUrl = "http://127.0.0.1:" + port
  val authUrl = "/rest/authenticate"
  val poulpeAuthManager = new PoulpeAuthenticationManager(poulpeUrl)

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

    def getFakeAppWithResponse(result: HttpResult) = FakeApplication(
      withRoutes = {
        case ("GET", url) if url == authUrl =>
          Action {
            result
          }
      }
    )

//    "authentication manager" should {
//
//      "authenticate valid user" in new WithServer(getFakeAppWithResponse(Ok(successResponse)), port) {
//        poulpeAuthManager.authenticate("admin", "123") must beSome.which {
//          user:UserInfo => user.username == "admin" && user.firstName.isDefined
//        }.awaitFor(10.seconds)
//      }
//
//      "not perform authentication for invalid credentials" in
//        new WithServer(getFakeAppWithResponse(NotFound(invalidCredentialsResponse)), port) {
//        poulpeAuthManager.authenticate("admin", "invalidPassword") must beNone.awaitFor(10.seconds)
//      }
//
//      "not authenticate disabled user" in new WithServer(getFakeAppWithResponse(Ok(disabledUserResponse)), port) {
//        poulpeAuthManager.authenticate("admin", "123") must beNone.awaitFor(10.seconds)
//      }
//    }
  }
}
