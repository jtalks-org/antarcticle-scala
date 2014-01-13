package security

import org.specs2.mutable.Specification
import util.FakeSessionProvider
import repositories.UsersRepositoryComponent
import util.FakeSessionProvider.FakeSessionValue
import org.mockito.Matchers
import models.database.UserRecord
import org.specs2.specification.BeforeExample
import org.specs2.mock.Mockito
import org.specs2.mock.mockito.ArgumentCapture
import org.specs2.scalaz.ValidationMatchers
import util.ScalazValidationTestUtils._
import scala.concurrent.future
import scala.concurrent.ExecutionContext.Implicits.global
import scalaz._
import Scalaz._

class SecurityServiceSpec extends Specification with ValidationMatchers
    with Mockito with BeforeExample {

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

  "sign in" should {
    "success" should {
      val username = "userdfsd"
      val password = "rerfev"
      val userInfo = UserInfo(username, "fn".some, "ln".some)
      val authUser = AuthenticatedUser(1, username)
      val userFromDb = UserRecord(Some(1), username)
      val generatedToken = "2314"

      "return remember me token and authenticated user" in {
         authenticationManager.authenticate(username, password) returns future(userInfo.some)
         usersRepository.getByUsername(userInfo.username)(FakeSessionValue) returns userFromDb.some
         tokenProvider.generateToken returns generatedToken

         securityService.signInUser(username, password) must beSuccessful((generatedToken, authUser)).await
      }

      "create new user when not exists" in {
         authenticationManager.authenticate(username, password) returns future(userInfo.some)
         usersRepository.getByUsername(userInfo.username)(FakeSessionValue) returns None
         tokenProvider.generateToken returns generatedToken

         securityService.signInUser(username, password)

         there was one(usersRepository).insert(UserRecord(None, username, false, "fn".some, "ln".some))(FakeSessionValue)
      }

      "issue remember me token to authenticated user" in {
         authenticationManager.authenticate(username, password) returns future(userInfo.some)
         usersRepository.getByUsername(userInfo.username)(FakeSessionValue) returns None
         usersRepository.insert(any[UserRecord])(Matchers.eq(FakeSessionValue)) returns 1
         tokenProvider.generateToken returns generatedToken

         securityService.signInUser(username, password)

         there was one(usersRepository).updateRememberToken(1, generatedToken)(FakeSessionValue)
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
