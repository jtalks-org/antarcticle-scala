package security

import repositories.UsersRepositoryComponent
import services.SessionProvider
import scalaz._
import Scalaz._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.slick.jdbc.JdbcBackend
import scala.Predef._
import models.database.UserRecord
import utils.Implicits._
import java.security.MessageDigest
import scala.util.Random

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

    private [security] def encodePassword(password:String, salt:Option[String]) = {
      val mergedPasswordAndSalt = salt.cata(some = str => password + '{' + str + '}', none = password)
      val digest = MessageDigest.getInstance("MD5").digest(mergedPasswordAndSalt.getBytes)
      new java.math.BigInteger(1, digest).toString(16)
    }


    def signInUser(username: String, password: String) = {

      def getUserFromAuthManager: Future[Option[UserRecord]] = {

        def randomString(length: Int) = {
          val r = new scala.util.Random
          val sb = new StringBuilder
          for (i <- 1 to length) {
            sb.append(r.nextPrintableChar())
          }
          sb.toString()
        }

        def createUser(userInfo:UserInfo) = withSession {
          implicit s: JdbcBackend#Session =>

          val salt = some(randomString(64))
          val encodedPassword = encodePassword(password, salt)
          val userToInsert = UserRecord(None, username, encodedPassword, false, salt, userInfo.firstName, userInfo.lastName)
          usersRepository.findByUserName(username) match {
            case user :: Nil => usersRepository.remove(user.username)
            case users => users find {user => user.username === username} map {user => usersRepository.remove(user.username)}
          }
          usersRepository.insert(userToInsert)
        }

       authenticationManager.authenticate(username, password) map (result => {
         for {
           userInfo <- result
           id = createUser(userInfo)
         } yield UserRecord(some(id), username, password, false, userInfo.firstName, userInfo.lastName)
       })
      }

      def getUserFormDatabase: Future[Option[UserRecord]] =
        withSession {
          implicit s: JdbcBackend#Session =>

            import scala.concurrent.future

            future {

              def isValidPassword(user:UserRecord): Boolean = {
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
        }

      def getUserRecord: Future[Option[UserRecord]] = {
        val dbUser: Future[Option[UserRecord]] = getUserFormDatabase
        dbUser flatMap {
          user => {
            user match {
              case u: Some[UserRecord] => dbUser
              case None => getUserFromAuthManager
            }
          }
        }
      }

      (for {
        user <- OptionT.optionT(getUserRecord)
        authority = if (user.admin) Authorities.Admin else Authorities.User
        authenticatedUser = AuthenticatedUser(user.id.get, user.username, authority)
        token = issueRememberMeTokenTo(authenticatedUser.userId)
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

  }
}

private[security] case class UserInfo(username: String, password:String, firstName: Option[String], lastName: Option[String])


