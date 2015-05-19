package security

import java.net.SocketTimeoutException

import akka.actor.ActorSystem
import conf.{PropertiesProvider, PropertiesProviderComponent}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.{MatchResult, Matcher, MatcherMacros}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.scalaz.ValidationMatchers
import org.specs2.specification.BeforeEach
import org.specs2.specification.mutable.ExecutionEnvironment
import repositories.UsersRepositoryComponent
import services.ActorSystemProvider
import spray.http._
import util.MockSession
import util.TestHelpers._

import scala.concurrent.Future
import scalaz._

class PoulpeAuthenticationManagerSpec extends Specification with Mockito with ExecutionEnvironment
with MockSession with MatcherMacros with BeforeEach with ValidationMatchers {

  type ErrorsMatcher = NonEmptyList[String] :=> MatchResult[_]

  val mockPipeline = mock[(HttpRequest) => Future[HttpResponse]]

  override protected def before: Any = {
    org.mockito.Mockito.reset(mockPipeline)
  }

  object service extends AuthenticationManagerProviderImpl with UsersRepositoryComponent
  with MockSessionProvider with ActorSystemProvider with PropertiesProviderComponent {

    trait FakePipeProvider extends PipeProvider {
      def pipeline = mockPipeline
    }
    override val usersRepository = mock[UsersRepository]
    override def actorSystem = ActorSystem("test-antarcticle-system")
    override def propertiesProvider = mock[PropertiesProvider]
    val poulpeAuthManager = new PoulpeAuthenticationManager("") with FakePipeProvider
  }

  def is(implicit ee: ExecutionEnv) = {

    import service._

    "authentication manager" should {

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

      def requestWithUsername(username: String): Matcher[HttpRequest] = { request: HttpRequest =>
        val query = request.uri.query.toMap
        request.method == spray.http.HttpMethods.GET &&
          query.get("username").contains(username) &&
          query.get("passwordHash").isDefined
      }

      def responseWithBody(body: String) = {
        Future.successful(
          HttpResponse(entity = HttpEntity(MediaTypes.`text/xml`, body))
        )
      }

      "authenticate valid user" in  {
        mockPipeline.apply(any[HttpRequest]) returns responseWithBody(successResponse)

        poulpeAuthManager.authenticate("admin", "123") must beSome.which {
          user: UserInfo => user.username == "admin" && user.firstName.isDefined
        }.await

        there was one(mockPipeline).apply(requestWithUsername("admin"))
      }

      "not perform authentication for invalid credentials" in {
        mockPipeline.apply(any[HttpRequest]) returns responseWithBody(disabledUserResponse)

        poulpeAuthManager.authenticate("admin", "invalidPassword") must beNone.await

        there was one(mockPipeline).apply(requestWithUsername("admin"))
      }

      "not authenticate disabled user" in {
        mockPipeline.apply(any[HttpRequest]) returns responseWithBody(invalidCredentialsResponse)

        poulpeAuthManager.authenticate("admin", "123") must beNone.await

        there was one(mockPipeline).apply(requestWithUsername("admin"))
      }

      val registerUsername = "user1"
      val registerPassword = "p@$$w0rD"
      val registerEmail = "emal@email.net"
      val userUid = "fake-user-uid"
      val registerUserInfo = UserInfo(registerUsername, registerPassword, registerEmail)

      def expectedRegisterRequest: Matcher[HttpRequest] = { request: HttpRequest =>
        val body = request.entity.asString
        request.method == spray.http.HttpMethods.POST &&
          body.contains(s"<username>$registerUsername</username>") &&
          body.contains(s"<passwordHash>$registerPassword</passwordHash>") &&
          body.contains(s"<email>$registerEmail</email>")
      }

      "register a valid user" in {
        mockPipeline.apply(any[HttpRequest]) returns Future.successful(HttpResponse(status = StatusCodes.OK))

        poulpeAuthManager.register(registerUserInfo) must beSuccessful.await

        there was one(mockPipeline).apply(expectedRegisterRequest)
      }

      "not register user with existing username" in {

        val response =
          """
            |<errors xmlns="http://www.jtalks.org/namespaces/1.0">
            | <error>user.username.already_exists</error>
            |</errors>
          """.stripMargin

        mockPipeline.apply(any[HttpRequest]) returns Future.successful {
          HttpResponse(status = StatusCodes.BadRequest, entity = HttpEntity(response))
        }

        val expected: ErrorsMatcher = { case errors if errors.size == 1 => ok}

        poulpeAuthManager.register(registerUserInfo) must beFailing.like(expected).await

        there was one(mockPipeline).apply(expectedRegisterRequest)
      }

      "fail when poulpe is not available" in {
        mockPipeline.apply(any[HttpRequest]) returns Future.failed(new SocketTimeoutException())

        poulpeAuthManager.register(registerUserInfo) must throwA[Exception].like{
          case e: Exception => e.isInstanceOf[SocketTimeoutException] must beTrue
        }.await

        there was one(mockPipeline).apply(expectedRegisterRequest)
      }

      def activateRequest(uid: String): Matcher[HttpRequest] = { request: HttpRequest =>
        val query = request.uri.query.toMap
        request.method == spray.http.HttpMethods.GET &&
          query.get("uuid").contains(uid)
      }

      "activation should be successful" in {
        mockPipeline.apply(any[HttpRequest]) returns Future.successful {
          HttpResponse(status = StatusCodes.NoContent)
        }

        poulpeAuthManager.activate(userUid) must beSuccessful.await

        there was one(mockPipeline).apply(activateRequest(userUid))
      }

      "activation should fail if user is already active" in {
        val response =
          """
            |<errors xmlns="http://www.jtalks.org/namespaces/1.0">
            | <error>user.username.already_active</error>
            |</errors>
          """.stripMargin
        mockPipeline.apply(any[HttpRequest]) returns Future.successful {
          HttpResponse(status = StatusCodes.BadRequest, entity = HttpEntity(response))
        }
        val expected: ErrorsMatcher = {case errors if errors.size == 1 => ok}

        poulpeAuthManager.activate(userUid) must beFailing.like(expected).await

        there was one(mockPipeline).apply(activateRequest(userUid))
      }

      "activation sould fail if poulpe is not available" in {
        mockPipeline.apply(any[HttpRequest]) returns Future.failed(new SocketTimeoutException())

        poulpeAuthManager.activate(userUid) must throwA[Exception].like{
          case e: Exception => e.isInstanceOf[SocketTimeoutException] must beTrue
        }.await

        there was one(mockPipeline).apply(activateRequest(userUid))
      }
    }
  }
}
