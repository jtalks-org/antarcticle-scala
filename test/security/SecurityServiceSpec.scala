package security

import java.sql.Timestamp

import models.database.UserRecord
import org.mockito.Matchers
import org.mockito.Matchers.{eq => mockEq}
import org.specs2.matcher.Matcher
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.scalaz.ValidationMatchers
import org.specs2.specification.BeforeExample
import org.specs2.time.NoTimeConversions
import repositories.UsersRepositoryComponent
import services.{MailServiceComponent, ApplicationPropertiesServiceComponent}
import util.FakeSessionProvider
import util.FakeSessionProvider.FakeSessionValue
import utils.SecurityUtil
import validators.UserValidator

import scala.slick.jdbc.JdbcBackend
import scalaz.Scalaz._

class SecurityServiceSpec extends Specification
    with ValidationMatchers with Mockito with BeforeExample with NoTimeConversions {

  object service extends SecurityServiceComponentImpl with UsersRepositoryComponent
  with FakeSessionProvider with SecurityServiceComponent with ApplicationPropertiesServiceComponent with MailServiceComponent {
    override val usersRepository = mock[UsersRepository]
    override val authenticationManager = mock[AuthenticationManager]
    override val userValidator = mock[UserValidator]
    override val mailService = mock[MailService]
    override val propertiesService = mock[ApplicationPropertiesService]

  }

  import service._

  def before = {
    org.mockito.Mockito.reset(usersRepository)
    org.mockito.Mockito.reset(authenticationManager)
  }

  def anySession = any[JdbcBackend#Session]

  "sign in" should {
    "be success" in {
      val username = "userdfsd"
      val password = "rerfev"
      val salt = Some("fakeSalt")
      val encodedPassword: String = SecurityUtil.encodePassword(password, salt)
      val email = "mail01@mail.zzz"
      val userInfo = UserInfo(username, password, "fn".some, "ln".some, true)
      val authUser = AuthenticatedUser(1, username, Authorities.User)
      val userFromDb = UserRecord(Some(1), username, encodedPassword, email, false, salt)
      val userFromDb2 =  UserRecord(Some(2), username.toUpperCase, encodedPassword, email, false, salt)
      val usernameIgnoreCase: Matcher[String]  = (_: String).equalsIgnoreCase(username)
      val fakeCreatedAt = new Timestamp(System.currentTimeMillis())

      def beMostlyEqualTo = (be_==(_:UserRecord)) ^^^ ((_:UserRecord).copy(
        salt = "salt".some,
        password = "pwd",
        uid = "uid",
        createdAt = fakeCreatedAt
      ))

      "return remember me token and authenticated user" in {
        authenticationManager.authenticate(username, password) returns userInfo.some
        usersRepository.getByUsername(userInfo.username)(FakeSessionValue) returns Some(userFromDb)

        securityService.signInUser(username, password) must beSuccessful
      }

      "authenticated admin should have Admin authority" in {
        authenticationManager.authenticate(username, password) returns userInfo.some
        usersRepository.getByUsername(userInfo.username)(FakeSessionValue) returns Some(userFromDb.copy(admin=true))

        securityService.signInUser(username, password) must beSuccessful.like {
          case (_, user:AuthenticatedUser) if user.authority == Authorities.Admin => ok
        }
      }


      "create new user when not exists" in {

        def withMockedAuthenticationManagerAndTokenProvider[T](doTest: => T) = {
          authenticationManager.authenticate(usernameIgnoreCase, ===(password)) returns userInfo.some
          usersRepository.getByUsername(usernameIgnoreCase)(anySession) returns None

          doTest

          val expectedRecord = UserRecord(None, username, encodedPassword, "NA", false, salt, "fn".some,
            "ln".some, active = true, createdAt = fakeCreatedAt)
          there was one(usersRepository).insert(beMostlyEqualTo(expectedRecord))(anySession)
        }

        "and username has a correct case" in {
          withMockedAuthenticationManagerAndTokenProvider {
            securityService.signInUser(username, password) must beSuccessful

            there was one (authenticationManager).authenticate(===(username), ===(password))
          }
        }
      }

       "update user when password does not match" in {

        def withMockedAuthenticationManagerAndTokenProvider[T](doTest: => T) = {
          authenticationManager.authenticate(username, password) returns userInfo.some

          doTest

          there was no(usersRepository).insert(any[UserRecord])(anySession)
          val expectedRecord = UserRecord(Some(1), username, encodedPassword, email, false, salt, "fn".some, "ln".some)
          there was one(usersRepository).update(beMostlyEqualTo(expectedRecord))(anySession)
        }

        val userWithEmptyPassword: UserRecord = userFromDb.copy(password = "")
        "and one user exists with case-insensitive username" in {
          withMockedAuthenticationManagerAndTokenProvider{
            usersRepository.getByUsername(userInfo.username)(FakeSessionValue) returns userWithEmptyPassword.some
            securityService.signInUser(username, password) must beSuccessful
          }
        }

        "and several users exist with case-insensitive username" in {
          withMockedAuthenticationManagerAndTokenProvider {
            usersRepository.getByUsername(userInfo.username)(FakeSessionValue) returns userWithEmptyPassword.some
            securityService.signInUser(username, password) must beSuccessful
          }
        }

      }

      "created user should have User authority" in {
        authenticationManager.authenticate(username, password) returns userInfo.some
        usersRepository.getByUsername(userInfo.username)(FakeSessionValue) returns None

        securityService.signInUser(username, password) must beSuccessful.like {
          case (_, user:AuthenticatedUser) if user.authority == Authorities.User => ok
        }
      }

      "issue remember me token to authenticated user" in {
        val userId = 1
        authenticationManager.authenticate(username, password) returns userInfo.some
        usersRepository.getByUsername(username)(FakeSessionValue) returns None
        usersRepository.insert(any[UserRecord])(Matchers.eq(FakeSessionValue)) returns userId
        usersRepository.updateRememberToken(mockEq(userId), anyString)(mockEq(FakeSessionValue)) returns true

        securityService.signInUser(username, password) must beSuccessful

        there was one(usersRepository).updateRememberToken(mockEq(userId), anyString)(mockEq(FakeSessionValue))
      }

      "trim username" in {
        authenticationManager.authenticate(username, password) returns userInfo.some
        usersRepository.getByUsername(username)(FakeSessionValue) returns Some(userFromDb)

        securityService.signInUser(' ' + username + ' ', password) must beSuccessful.like {
          case (_, user:AuthenticatedUser) if user.username == username => ok
        }

        there was one(usersRepository).getByUsername(===(username))(anySession)
      }

    }

    "fail" in {
      "return validation error" in {
        authenticationManager.authenticate(anyString, anyString) returns None
        usersRepository.getByUsername(anyString)(anySession) returns None

        securityService.signInUser("", "") must beFailing
      }
    }
  }
}
