package security

import akka.actor.ActorSystem
import conf.{PropertiesProvider, PropertiesProviderComponent}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.{Matcher, MatcherMacros}
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeEach
import org.specs2.specification.mutable.ExecutionEnvironment
import repositories.UsersRepositoryComponent
import services.ActorSystemProvider
import spray.http._
import util.MockSession

import scala.concurrent.Future

class PoulpeAuthenticationManagerSpec extends Specification with Mockito with ExecutionEnvironment
with MockSession with MatcherMacros with BeforeEach {

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

    import service._

    "authentication manager" should {

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
    }
  }
}
