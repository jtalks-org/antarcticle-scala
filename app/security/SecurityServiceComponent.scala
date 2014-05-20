package security

import repositories.UsersRepositoryComponent
import services.SessionProvider
import scalaz._
import Scalaz._
import scala.concurrent._
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

    val failedSignIn: ValidationNel[String, Nothing] = "Invalid username or password".failNel

    private [security] def encodePassword(password:String, salt:Option[String]) = {
      val mergedPasswordAndSalt = salt.cata(some = str => password + '{' + str + '}', none = password)
      val digest = MessageDigest.getInstance("MD5").digest(mergedPasswordAndSalt.getBytes)
      new java.math.BigInteger(1, digest).toString(16)
    }

    def signInUser(username: String, password: String) = {
       Option(username) match {
         case None => future(failedSignIn)
         case _ => doSignIn(username.trim, password)
       }
    }

    private def doSignIn(username: String, password: String) = {

      def getUserFromAuthManager: Future[Option[UserRecord]] = {

        def randomString(length: Int) = {
          val r = new Random
          val sb = new StringBuilder
          for (i <- 1 to length) {
            sb.append(r.nextPrintableChar())
          }
          sb.toString()
        }

        def createOrUpdateUser(userInfo:UserInfo) = withSession {
          implicit s: JdbcBackend#Session =>

          val salt = some(randomString(64))
          val encodedPassword = encodePassword(password, salt)
          val userRecord = usersRepository.findByUserName(username) match {
            case Nil => none
            case user :: Nil => some(user)
            case users => users find {user => user.username === username}
          }

          userRecord cata (
            some = user => {
              usersRepository.updatePassword(user.id.get, encodedPassword, salt)
              user.id
            },
            none = some {
              val record = UserRecord(None, username, encodedPassword, false, salt, userInfo.firstName, userInfo.lastName)
              usersRepository.insert(record)
            }
          )
        }

       authenticationManager.authenticate(username, password) map (result => {
         for {
           userInfo <- result
           id = createOrUpdateUser(userInfo)
         } yield UserRecord(id, username, password, false, userInfo.firstName, userInfo.lastName)
       })
      }

      def getUserFormDatabase: Future[Option[UserRecord]] =
        withSession {
          implicit s: JdbcBackend#Session =>

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

private[security] case class UserInfo(username: String, password:String, firstName: Option[String], lastName: Option[String])


