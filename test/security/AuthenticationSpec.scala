package security

import org.specs2.mutable.Specification
import repositories.UsersRepositoryComponent
import org.specs2.mock.Mockito
import util.FakeSessionProvider
import org.specs2.specification.AfterExample
import play.api.mvc._
import conf.Constants
import scala.slick.jdbc.JdbcBackend
import org.mockito.Matchers
import play.api.test.FakeRequest
import scala.Some
import models.database.UserRecord
import play.api.mvc.Cookie

class AuthenticationSpec extends Specification with Mockito with AfterExample {

  object auth extends Authentication
                      with Controller
                      with UsersRepositoryComponent
                      with FakeSessionProvider {
    override val usersRepository = mock[UsersRepository]
  }

  import auth._

  protected def after = {
    org.mockito.Mockito.reset(usersRepository)
  }

  "Authentication currentUser method" should {

    "return not authorised user if there are no valid cookie" in {
      val user = auth.currentPrincipal(FakeRequest())

      user must be(AnonymousPrincipal)
      there was no(usersRepository).getByRememberToken(anyString)(any[JdbcBackend#Session])
    }

    "return user if request has a valid cookie for user" in {
      val userRecord = Some(UserRecord(Some(1), "testUserName", false))
      val tokenCookie = Cookie(Constants.rememberMeCookie, "user", Some(1000), "/", None, false, false)
      usersRepository.getByRememberToken(Matchers.eq("user"))(any[JdbcBackend#Session]) returns userRecord

      val user = auth.currentPrincipal(FakeRequest().withCookies(tokenCookie))

      user must beEqualTo(AuthenticatedUser(1, "testUserName", Authorities.User))
      there was one(usersRepository).getByRememberToken(Matchers.eq("user"))(any[JdbcBackend#Session])
    }

    "return admin if request has a valid cookie for admin" in {
      val adminCookie = Cookie(Constants.rememberMeCookie, "admin", Some(1000), "/", None, false, false)
      val adminRecord = Some(UserRecord(Some(2), "testUserName", true))
      usersRepository.getByRememberToken(Matchers.eq("admin"))(any[JdbcBackend#Session]) returns adminRecord

      val user = auth.currentPrincipal(FakeRequest().withCookies(adminCookie))

      user must beEqualTo(AuthenticatedUser(2, "testUserName", Authorities.Admin))
      there was one(usersRepository).getByRememberToken(Matchers.eq("admin"))(any[JdbcBackend#Session])
    }
  }
}
