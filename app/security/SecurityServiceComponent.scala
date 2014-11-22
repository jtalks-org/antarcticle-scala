package security

import models.UserModels.User
import repositories.UsersRepositoryComponent
import services.SessionProvider
import validators.UserValidator
import scalaz._
import Scalaz._
import scala.slick.jdbc.JdbcBackend
import models.database.UserRecord
import utils.SecurityUtil

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
    def signUpUser(user: User): ValidationNel[String, UserRecord]
  }

}

trait SecurityServiceComponentImpl extends SecurityServiceComponent {
  this: UsersRepositoryComponent with SessionProvider =>

  val securityService = new SecurityServiceImpl
  val authenticationManager: AuthenticationManager
  val userValidator: UserValidator

  class SecurityServiceImpl extends SecurityService {

    def signInUser(username: String, password: String) = {

      def createOrUpdateUser(info: UserInfo): UserRecord = withSession {
        implicit s: JdbcBackend#Session =>
          val salt = some(SecurityUtil.generateSalt)
          val encodedPassword = SecurityUtil.encodePassword(password, salt)
          usersRepository.getByUsername(username.trim).cata(
            some = user => {
              val record = user.copy(password = encodedPassword, salt = salt,
                firstName = info.firstName, lastName = info.lastName, active = user.active)
              usersRepository.update(record)
              record
            },
            none = {
              val record = UserRecord(None, username.trim, encodedPassword, "NA", false, salt, info.firstName,
                info.lastName, active = true)
              record.copy(id = some(usersRepository.insert(record)))
            }
          )
      }

      for {
        user <- authenticationManager.authenticate(username.trim, password).cata(
          some = info => if (info.active) info.successNel else s"User ${info.username} is not active".failNel,
          none = "Invalid username or password".failNel
        )
        userRecord = createOrUpdateUser(user)
        authority = if (userRecord.admin) Authorities.Admin else Authorities.User
        authenticatedUser = AuthenticatedUser(userRecord.id.get, userRecord.username, authority)
        token = issueRememberMeTokenTo(authenticatedUser.userId)
      } yield (token, authenticatedUser)
    }



    private def issueRememberMeTokenTo(userId: Int) =
      withSession { implicit session: JdbcBackend#Session =>
        val token = SecurityUtil.generateRememberMeToken
        usersRepository.updateRememberToken(userId, token)
        token
      }

    override def signUpUser(user: User): ValidationNel[String, UserRecord] = withSession {
      implicit s: JdbcBackend#Session =>

      def validateUniqueness(user: User) = {
        for {
          _ <- usersRepository.getByUsername(user.username).cata(
            existingUser => s"User with the username ${user.username} already exists".failNel,
            ().successNel
          )
          _ <- usersRepository.getByEmail(user.email).cata(
            existingUser => s"User with the email ${user.email} already exists".failNel,
            ().successNel
          )
        } yield ()
      }

      val result = for {
        _ <- userValidator.validate(user)
        _ <- validateUniqueness(user)
        salt = some(SecurityUtil.generateSalt)
        encodedPassword = SecurityUtil.encodePassword(user.password, salt)
        userRecord = UserRecord(None, user.username, encodedPassword, user.email, salt = salt)
        userId = usersRepository.insert(userRecord)
      } yield userRecord.copy(id = some(userId))
      result
    }
  }

}




