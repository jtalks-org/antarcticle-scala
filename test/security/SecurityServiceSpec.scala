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
import scala.concurrent.Await
import scala.concurrent.duration._
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

  //TODO: don't use await somehow?
  "sign in" should {
    "success" should {
      val username = "userdfsd"
      val password = "rerfev"
      val salt = Some("fakeSalt")
      val encodedPassword: String = service.securityService.encodePassword(password, salt)
      val userInfo = UserInfo(username, password, "fn".some, "ln".some)
      val authUser = AuthenticatedUser(1, username, Authorities.User)
      val userFromDb = UserRecord(Some(1), username, encodedPassword, false, salt)
      val userFromDb2 =  UserRecord(Some(2), username.toUpperCase, encodedPassword, false, salt)
      val generatedToken = "2314"

      "return remember me token and authenticated user" in {
        authenticationManager.authenticate(username, password) returns future(userInfo.some)
        usersRepository.findByUserName(userInfo.username)(FakeSessionValue) returns List(userFromDb)
        tokenProvider.generateToken returns generatedToken

        securityService.signInUser(username, password) must beSuccessful((generatedToken, authUser)).await
      }

      "authenticated admin should have Admin authority" in {
        authenticationManager.authenticate(username, password) returns future(userInfo.some)
        usersRepository.findByUserName(userInfo.username)(FakeSessionValue) returns List(userFromDb.copy(admin=true))
        tokenProvider.generateToken returns generatedToken

        Await.result(securityService.signInUser(username, password), 10 seconds) must beSuccessful.like {
          case (_, user:AuthenticatedUser) if user.authority == Authorities.Admin => ok
        }
      }

      "create new user when not exists" in {
        authenticationManager.authenticate(username, password) returns future(userInfo.some)
        usersRepository.findByUserName(userInfo.username)(FakeSessionValue) returns Nil
        tokenProvider.generateToken returns generatedToken

        Await.result(securityService.signInUser(username, password), 10 seconds)

        def beMostlyEqualTo = (be_==(_:UserRecord)) ^^^ ((_:UserRecord).copy(salt = "salt".some, password = "pwd"))
        val expectedRecord = UserRecord(None, username, encodedPassword, false, salt, "fn".some, "ln".some)
        there was one(usersRepository).insert(beMostlyEqualTo(expectedRecord))(any[JdbcBackend#Session])
      }

      "do case sensetive authorisation if several similar usernames are exists" in {
        val m: Matcher[String]  = (_: String).equalsIgnoreCase(username)
        usersRepository.findByUserName(m)(any[JdbcBackend#Session]) returns List(userFromDb, userFromDb2)
        tokenProvider.generateToken returns generatedToken

        Await.result(securityService.signInUser(username, password), 10 seconds) must beSuccessful.like {
          case (_, user:AuthenticatedUser) if user.username == username => ok
        }

        Await.result(securityService.signInUser(username.toUpperCase, password), 10 seconds) must beSuccessful.like {
          case (_, user:AuthenticatedUser) if user.username == username.toUpperCase => ok
        }

        there was one(usersRepository).findByUserName(===(username))(any[JdbcBackend#Session])
        there was one(usersRepository).findByUserName(===(username.toUpperCase))(any[JdbcBackend#Session])
        there was no (authenticationManager).authenticate(anyString, anyString)
      }

      "created user should have User authority" in {
        authenticationManager.authenticate(username, password) returns future(userInfo.some)
        usersRepository.findByUserName(userInfo.username)(FakeSessionValue) returns Nil
        tokenProvider.generateToken returns generatedToken

        Await.result(securityService.signInUser(username, password), 10 seconds) must beSuccessful.like {
          case (_, user:AuthenticatedUser) if user.authority == Authorities.User => ok
        }
      }

      "issue remember me token to authenticated user" in {
        val userId = 1
        authenticationManager.authenticate(username, password) returns future(userInfo.some)
        usersRepository.findByUserName(username)(FakeSessionValue) returns Nil
        usersRepository.insert(any[UserRecord])(Matchers.eq(FakeSessionValue)) returns userId
        usersRepository.updateRememberToken(userId, generatedToken)(FakeSessionValue) returns true
        tokenProvider.generateToken returns generatedToken

        Await.result(securityService.signInUser(username, password), 10 seconds)

        there was one(usersRepository).updateRememberToken(userId, generatedToken)(FakeSessionValue)
      }
    }

    "fail" should {
      "return validation error" in {
        authenticationManager.authenticate(any[String], any[String]) returns future(None)
        usersRepository.findByUserName(anyString)(any[JdbcBackend#Session]) returns Nil

        securityService.signInUser("", "") must beFailing.await
      }
    }
  }
}
