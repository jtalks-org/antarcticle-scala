package security

import repositories.UsersRepositoryComponent
import services.SessionProvider
import scalaz._
import Scalaz._
import scala.concurrent.Future
import scala.slick.jdbc.JdbcBackend
import scala.Predef._
import models.database.UserRecord
import utils.Implicits._

trait SecurityServiceComponent {
  val securityService: SecurityService

  trait SecurityService {
    /*
     * @return new remember me token for user if success
     */
    def signInUser(username: String, password: String): Future[ValidationNel[String, (String, AuthenticatedUser)]]
  }
}

trait SecurityServiceComponentImpl extends SecurityServiceComponent {
  this: UsersRepositoryComponent with SessionProvider =>

  val securityService = new SecurityServiceImpl
  val tokenProvider: TokenProvider
  val authenticationManager: AuthenticationManager

  class SecurityServiceImpl extends SecurityService {
    def signInUser(username: String, password: String) = {
      (for {
        userInfo <- OptionT(authenticationManager.authenticate(username, password))
        authenticatedUser = getOrCreateAuthenticatedUser(userInfo)
        token = issueRememberMeTokenTo(authenticatedUser.id)
      } yield (token, authenticatedUser)).fold(
        some = _.successNel,
        none = "Invalid username or password".failNel
      )
    }

    private def issueRememberMeTokenTo(userId: Int) = {
      val token = tokenProvider.generateToken
      withSession { implicit session: JdbcBackend#Session =>
        usersRepository.updateRememberToken(userId, token)
      }
      token
    }

    private def getOrCreateAuthenticatedUser(userInfo: UserInfo) =
      withSession { implicit s: JdbcBackend#Session =>
        def createUser = {
          val userToInsert = UserRecord(None, userInfo.username, false,
            userInfo.firstName, userInfo.lastName)
          usersRepository.insert(userToInsert)
        }

        usersRepository.getByUsername(userInfo.username).cata(
          some = user => AuthenticatedUser(user.id.get, user.username),
          none = AuthenticatedUser(createUser, userInfo.username)
        )
      }
  }
}

private[security] case class UserInfo(username: String, firstName: Option[String], lastName: Option[String])


