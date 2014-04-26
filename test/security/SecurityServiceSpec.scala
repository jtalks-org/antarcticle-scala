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
      val userInfo = UserInfo(username, password, "fn".some, "ln".some)
      val authUser = AuthenticatedUser(1, username, Authorities.User)
      val userFromDb = UserRecord(Some(1), username, password)
      val generatedToken = "2314"

      "return remember me token and authenticated user" in {
        authenticationManager.authenticate(username, password) returns future(userInfo.some)
        usersRepository.getByUsername(userInfo.username)(FakeSessionValue) returns userFromDb.some
        tokenProvider.generateToken returns generatedToken

        securityService.signInUser(username, password) must beSuccessful((generatedToken, authUser)).await
      }

      "authenticated admin should have Admin authority" in {
        authenticationManager.authenticate(username, password) returns future(userInfo.some)
        usersRepository.getByUsername(userInfo.username)(FakeSessionValue) returns userFromDb.copy(admin=true).some
        tokenProvider.generateToken returns generatedToken

        Await.result(securityService.signInUser(username, password), 10 seconds) must beSuccessful.like {
          case (_, user) if user.authority == Authorities.Admin => ok
        }
      }

      "create new user when not exists" in {
        authenticationManager.authenticate(username, password) returns future(userInfo.some)
        usersRepository.getByUsername(userInfo.username)(FakeSessionValue) returns None
        tokenProvider.generateToken returns generatedToken

        Await.result(securityService.signInUser(username, password), 10 seconds)

        there was one(usersRepository).insert(UserRecord(None, username, password, false, "fn".some, "ln".some))(FakeSessionValue)
      }

      "created user should have User authority" in {
        authenticationManager.authenticate(username, password) returns future(userInfo.some)
        usersRepository.getByUsername(userInfo.username)(FakeSessionValue) returns None
        tokenProvider.generateToken returns generatedToken

        Await.result(securityService.signInUser(username, password), 10 seconds) must beSuccessful.like {
          case (_, user) if user.authority == Authorities.User => ok
        }
      }

      "issue remember me token to authenticated user" in {
        val userId = 1
        authenticationManager.authenticate(username, password) returns future(userInfo.some)
        usersRepository.getByUsername(username)(FakeSessionValue) returns None
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

        securityService.signInUser("", "") must beFailing.await
      }
    }
  }
}
