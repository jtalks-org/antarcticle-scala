package security

import org.specs2.mutable.Specification
import util.FakeSessionProvider
import repositories.UsersRepositoryComponent
import util.FakeSessionProvider.FakeSessionValue
import org.mockito.Matchers
import models.database.UserRecord
import org.specs2.specification.BeforeExample
import org.specs2.mock.Mockito
import org.specs2.scalaz.ValidationMatchers
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits.global
import scalaz._
import Scalaz._
import org.specs2.time.NoTimeConversions
import scala.slick.jdbc.JdbcBackend
import org.specs2.matcher.Matcher

class SecurityServiceSpec extends Specification
    with ValidationMatchers with Mockito with BeforeExample with NoTimeConversions {

  object service extends SecurityServiceComponentImpl with UsersRepositoryComponent
  with FakeSessionProvider {
    override val usersRepository = mock[UsersRepository]
    override val tokenProvider = mock[TokenProvider]
    override val authenticationManager = mock[AuthenticationManager]
  }

  import service._

  def before = {
    org.mockito.Mockito.reset(usersRepository)
    org.mockito.Mockito.reset(tokenProvider)
    org.mockito.Mockito.reset(authenticationManager)
  }

  def anySession = any[JdbcBackend#Session]

  "sign in" should {
    "be success" in {
      val username = "userdfsd"
      val password = "rerfev"
      val salt = Some("fakeSalt")
      val encodedPassword: String = service.securityService.encodePassword(password, salt)
      val userInfo = UserInfo(username, password, "fn".some, "ln".some)
      val authUser = AuthenticatedUser(1, username, Authorities.User)
      val userFromDb = UserRecord(Some(1), username, encodedPassword, false, salt)
      val userFromDb2 =  UserRecord(Some(2), username.toUpperCase, encodedPassword, false, salt)
      val generatedToken = "2314"
      val usernameIgnoreCase: Matcher[String]  = (_: String).equalsIgnoreCase(username)

      def beMostlyEqualTo = (be_==(_:UserRecord)) ^^^ ((_:UserRecord).copy(salt = "salt".some, password = "pwd"))

      "return remember me token and authenticated user" in {
        authenticationManager.authenticate(username, password) returns future(userInfo.some)
        usersRepository.findByUsername(userInfo.username)(FakeSessionValue) returns List(userFromDb)
        tokenProvider.generateToken returns generatedToken

        securityService.signInUser(username, password) must beSuccessful(generatedToken, authUser)
      }

      "authenticated admin should have Admin authority" in {
        authenticationManager.authenticate(username, password) returns future(userInfo.some)
        usersRepository.findByUsername(userInfo.username)(FakeSessionValue) returns List(userFromDb.copy(admin=true))
        tokenProvider.generateToken returns generatedToken

        securityService.signInUser(username, password) must beSuccessful.like {
          case (_, user:AuthenticatedUser) if user.authority == Authorities.Admin => ok
        }
      }


      "create new user when not exists" in {

        def withMockedAuthenticationManagerAndTokenProvider[T](doTest: => T) = {
          authenticationManager.authenticate(usernameIgnoreCase, ===(password)) returns future(userInfo.some)
          usersRepository.findByUsername(usernameIgnoreCase)(anySession) returns Nil
          usersRepository.getByUsername(usernameIgnoreCase)(anySession) returns None
          tokenProvider.generateToken returns generatedToken

          doTest

          val expectedRecord = UserRecord(None, username, encodedPassword, false, salt, "fn".some, "ln".some)
          there was one(usersRepository).insert(beMostlyEqualTo(expectedRecord))(anySession)
          there was no(usersRepository).updatePassword(anyInt, anyString, any[Option[String]])(anySession)
        }

        "and username has a correct case" in {
          withMockedAuthenticationManagerAndTokenProvider {
            securityService.signInUser(username, password) must beSuccessful

            there was one (authenticationManager).authenticate(===(username), ===(password))
          }
        }

        "and username has another case" in {
          withMockedAuthenticationManagerAndTokenProvider {
            securityService.signInUser(username.toUpperCase, password) must beSuccessful

            there was one (authenticationManager).authenticate(===(username.toUpperCase), ===(password))
          }
        }
      }

      "create a new user if username has another case and password is different" in {

        val petya = "petya"
        val petyaPassword = "password"
        val petyaPassEncoded = service.securityService.encodePassword(petyaPassword, salt)
        val petya2 = "Petya"
        val petya2Password = "qweerty"
        val petya2PassEncoded = service.securityService.encodePassword(petya2Password, salt)
        val petya2UserInfo = UserInfo(petya2, petya2Password, "fn".some, "ln".some)
        val petyaUserRecord =  UserRecord(Some(2), petya, petyaPassEncoded, false, salt)

        authenticationManager.authenticate(===(petya2), ===(petya2Password)) returns future(petya2UserInfo.some)
        usersRepository.findByUsername(===(petya2))(anySession) returns List(petyaUserRecord)
        usersRepository.getByUsername(===(petya2))(anySession) returns None
        tokenProvider.generateToken returns generatedToken

        securityService.signInUser(petya2, petya2Password) must beSuccessful

        val expectedRecord = UserRecord(None, petya2, petya2PassEncoded, false, salt, "fn".some, "ln".some)
        there was one(usersRepository).insert(beMostlyEqualTo(expectedRecord))(anySession)
        there was no(usersRepository).update(any[UserRecord])(anySession)

      }

      "update user when password does not match" in {

        def withMockedAuthenticationManagerAndTokenProvider[T](doTest: => T) = {
          authenticationManager.authenticate(username, password) returns future(userInfo.some)
          tokenProvider.generateToken returns generatedToken

          doTest

          there was no(usersRepository).insert(any[UserRecord])(anySession)
          val expectedRecord = UserRecord(Some(1), username, encodedPassword, false, salt, "fn".some, "ln".some)
          there was one(usersRepository).update(beMostlyEqualTo(expectedRecord))(anySession)
        }

        val userWithEmptyPassword: UserRecord = userFromDb.copy(password = "")
        "and one user exists with case-insensitive username" in {
          withMockedAuthenticationManagerAndTokenProvider{
            usersRepository.findByUsername(userInfo.username)(FakeSessionValue) returns List(userWithEmptyPassword)
            usersRepository.getByUsername(userInfo.username)(FakeSessionValue) returns userWithEmptyPassword.some
            securityService.signInUser(username, password) must beSuccessful
          }
        }

        "and several users exist with case-insensitive username" in {
          withMockedAuthenticationManagerAndTokenProvider {
            usersRepository.findByUsername(userInfo.username)(FakeSessionValue) returns List(userWithEmptyPassword, userFromDb2)
            usersRepository.getByUsername(userInfo.username)(FakeSessionValue) returns userWithEmptyPassword.some
            securityService.signInUser(username, password) must beSuccessful
          }
        }

      }

      "do case sensitive authorisation if several similar usernames are exists" in {
        val m: Matcher[String]  = (_: String).equalsIgnoreCase(username)
        usersRepository.findByUsername(m)(anySession) returns List(userFromDb, userFromDb2)
        usersRepository.getByUsername(===(username))(anySession) returns userFromDb.some
        tokenProvider.generateToken returns generatedToken

        securityService.signInUser(username, password) must beSuccessful.like {
          case (_, user:AuthenticatedUser) if user.username == username => ok
        }

        securityService.signInUser(username.toUpperCase, password) must beSuccessful.like {
          case (_, user:AuthenticatedUser) if user.username == username.toUpperCase => ok
        }

        there was one(usersRepository).findByUsername(===(username))(anySession)
        there was one(usersRepository).findByUsername(===(username.toUpperCase))(anySession)
        there was no (authenticationManager).authenticate(anyString, anyString)
      }

      "created user should have User authority" in {
        authenticationManager.authenticate(username, password) returns future(userInfo.some)
        usersRepository.findByUsername(userInfo.username)(FakeSessionValue) returns Nil
        usersRepository.getByUsername(userInfo.username)(FakeSessionValue) returns None
        tokenProvider.generateToken returns generatedToken

        securityService.signInUser(username, password) must beSuccessful.like {
          case (_, user:AuthenticatedUser) if user.authority == Authorities.User => ok
        }
      }

      "issue remember me token to authenticated user" in {
        val userId = 1
        authenticationManager.authenticate(username, password) returns future(userInfo.some)
        usersRepository.findByUsername(username)(FakeSessionValue) returns Nil
        usersRepository.getByUsername(username)(FakeSessionValue) returns None
        usersRepository.insert(any[UserRecord])(Matchers.eq(FakeSessionValue)) returns userId
        usersRepository.updateRememberToken(userId, generatedToken)(FakeSessionValue) returns true
        tokenProvider.generateToken returns generatedToken

        securityService.signInUser(username, password) must beSuccessful

        there was one(usersRepository).updateRememberToken(userId, generatedToken)(FakeSessionValue)
      }

      "trim username" in {
        usersRepository.findByUsername(username)(FakeSessionValue) returns List(userFromDb)
        tokenProvider.generateToken returns generatedToken

        securityService.signInUser(' ' + username + ' ', password) must beSuccessful.like {
          case (_, user:AuthenticatedUser) if user.username == username => ok
        }

        there was one(usersRepository).findByUsername(===(username))(anySession)
        there was no (authenticationManager).authenticate(anyString, anyString)
      }

    }

    "fail" in {
      "return validation error" in {
        authenticationManager.authenticate(anyString, anyString) returns future(None)
        usersRepository.findByUsername(anyString)(anySession) returns Nil

        securityService.signInUser("", "") must beFailing
      }
    }
  }
}
