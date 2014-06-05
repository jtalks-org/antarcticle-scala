package security

import repositories.UsersRepositoryComponent
import services.SessionProvider
import scalaz._
import Scalaz._
import scala.concurrent._
import scala.slick.jdbc.JdbcBackend
import scala.Predef._
import models.database.UserRecord
import scala.util.Random
import utils.HashingUtil

/**
 * Performs user authentication duty by login and password provided.
 * It also manages remember-me tokens for persistent user sessions.
 */
trait SecurityServiceComponent {
  val securityService: SecurityService

  trait SecurityService {
    /*
     * @return new remember me token for user if success
     */
    def signInUser(username: String, password: String): ValidationNel[String, (String, AuthenticatedUser)]
  }

}

trait SecurityServiceComponentImpl extends SecurityServiceComponent {
  this: UsersRepositoryComponent with SessionProvider =>

  val securityService = new SecurityServiceImpl
  val tokenProvider: TokenProvider
  val authenticationManager: AuthenticationManager

  class SecurityServiceImpl extends SecurityService {

    val failedSignIn: ValidationNel[String, Nothing] = "Invalid username or password".failNel

    private[security] def encodePassword(password: String, salt: Option[String]) = {
      HashingUtil.md5(salt.cata(some = str => s"$password{$str}", none = password))
    }

    def signInUser(username: String, password: String) = {
      Option(username) match {
        case None => failedSignIn
        case _ => doSignIn(username.trim, password)
      }
    }

    private def doSignIn(username: String, password: String) = {

      /**
       * Looks for user in a custom (maybe remote) authentication manager by user's credentials.
       * If user can be found, then local database record is getting updated (or created if not
       * yet exists) with the latest data provided by authentication manager.
       *
       * TODO: shouldn't it be yet another AuthenticationManager, e.g. DatabaseAuthenticationManager?
       */
      def getUserFromAuthManager: Option[UserRecord] = {
        import scala.concurrent.duration._

        def createOrUpdateUser(userInfo: UserInfo): UserRecord = withSession {
          implicit s: JdbcBackend#Session =>

            val salt = some(Random.alphanumeric.take(64).mkString)
            val encodedPassword = encodePassword(password, salt)
            val userRecord = usersRepository.findByUserName(username) match {
              case Nil => none
              case user :: Nil => some(user)
              case users => users find { user => user.username === username}
            }

            userRecord cata(
              some = user => {
                val record = user.copy(password = encodedPassword, salt = salt, firstName = userInfo.firstName, lastName = userInfo.lastName)
                usersRepository.update(record)
                record
              },
              none = {
                val record = UserRecord(None, username, encodedPassword, false, salt, userInfo.firstName, userInfo.lastName)
                val userId = usersRepository.insert(record)
                record.copy(id = some(userId))
              }
              )
        }

        for (
          userInfo <- Await.result(authenticationManager.authenticate(username, password), 10.seconds)
        ) yield createOrUpdateUser(userInfo)
      }

      /**
       * Looks for user in a local database by user's credentials.
       */
      def getUserFormDatabase = withSession { implicit s: JdbcBackend#Session =>

        def isValidPassword(user: UserRecord) = {
          !password.isEmpty && user.password === encodePassword(password, user.salt)
        }

        usersRepository.findByUserName(username) match {
          case Nil => none
          case user :: Nil => if (isValidPassword(user)) some(user) else none
          case users => users find {
            user => (user.username === username) && isValidPassword(user)
          }
        }
      }

      (for {
        user <- getUserFormDatabase orElse getUserFromAuthManager
        authority = if (user.admin) Authorities.Admin else Authorities.User
        authenticatedUser = AuthenticatedUser(user.id.get, user.username, authority)
        token = issueRememberMeTokenTo(authenticatedUser.userId)
      } yield (token, authenticatedUser)).cata(
          some = _.successNel,
          none = failedSignIn
        )
    }

    private def issueRememberMeTokenTo(userId: Int) = {
      val token = tokenProvider.generateToken
      withSession { implicit session: JdbcBackend#Session =>
        usersRepository.updateRememberToken(userId, token)
      }
      token
    }

  }

}

private[security] case class UserInfo(username: String, password: String, firstName: Option[String], lastName: Option[String])


