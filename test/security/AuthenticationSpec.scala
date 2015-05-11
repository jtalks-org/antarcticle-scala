package security

import conf.Constants
import models.database.UserRecord
import org.mockito.Matchers
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.specification.AfterEach
import play.api.mvc.{Cookie, _}
import play.api.test.FakeRequest
import repositories.UsersRepositoryComponent
import util.FakeSessionProvider

import scala.slick.jdbc.JdbcBackend

class AuthenticationSpec extends Specification with Mockito with AfterEach {

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
      val userRecord = Some(UserRecord(Some(1), "testUserName", "testpassword", "mail01@mail.zzz", false))
      val tokenCookie = Cookie(Constants.rememberMeCookie, "user", Some(1000), "/", None, false, false)
      usersRepository.getByRememberToken(Matchers.eq("user"))(any[JdbcBackend#Session]) returns userRecord

      val user = auth.currentPrincipal(FakeRequest().withCookies(tokenCookie))

      user must beEqualTo(AuthenticatedUser(1, "testUserName", Authorities.User))
      there was one(usersRepository).getByRememberToken(Matchers.eq("user"))(any[JdbcBackend#Session])
    }

    "return admin if request has a valid cookie for admin" in {
      val adminCookie = Cookie(Constants.rememberMeCookie, "admin", Some(1000), "/", None, false, false)
      val adminRecord = Some(UserRecord(Some(2), "testUserName", "testPassword", "mail01@mail.zzz", true))
      usersRepository.getByRememberToken(Matchers.eq("admin"))(any[JdbcBackend#Session]) returns adminRecord

      val user = auth.currentPrincipal(FakeRequest().withCookies(adminCookie))

      user must beEqualTo(AuthenticatedUser(2, "testUserName", Authorities.Admin))
      there was one(usersRepository).getByRememberToken(Matchers.eq("admin"))(any[JdbcBackend#Session])
    }
  }
}
