package security

import models.UserModels.User
import models.database.UserRecord
import play.api.Logger
import repositories.UsersRepositoryComponent
import services.{ApplicationPropertiesServiceComponent, MailServiceComponent, SessionProvider}
import utils.SecurityUtil
import validators.UserValidator

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.slick.jdbc.JdbcBackend
import scalaz.Scalaz._
import scalaz._

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
    def signInUser(username: String, password: String): Future[ValidationNel[String, (String, AuthenticatedUser)]]
    def signUpUser(user: User, host: String): Future[ValidationNel[String, String]]
    def activateUser(uid: String): Future[ValidationNel[String, String]]
  }
}

trait SecurityServiceComponentImpl extends SecurityServiceComponent {
  this: UsersRepositoryComponent
    with SessionProvider
    with ApplicationPropertiesServiceComponent
    with AuthenticationManagerProvider
    with MailServiceComponent =>

  val securityService = new SecurityServiceImpl
  val userValidator: UserValidator

  class SecurityServiceImpl extends SecurityService {

    override def signInUser(username: String, password: String) = {

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
              val record = UserRecord(None, username.trim, encodedPassword, "NA", admin = false, salt, info.firstName,
                info.lastName, active = true)
              record.copy(id = some(usersRepository.insert(record)))
            }
          )
      }

      authenticationManager.authenticate(username.trim, password) map {result =>
        for {
          user <- result.cata(
            some = info => if (info.active) info.successNel else s"User ${info.username} is not active".failureNel,
            none = "Invalid username or password".failureNel
          )
          userRecord = createOrUpdateUser(user)
          authority = if (userRecord.admin) Authorities.Admin else Authorities.User
          authenticatedUser = AuthenticatedUser(userRecord.id.get, userRecord.username, authority)
          token = issueRememberMeTokenTo(authenticatedUser.userId)
        } yield (token, authenticatedUser)
      }
    }

    private def issueRememberMeTokenTo(userId: Int) =
      withSession { implicit session: JdbcBackend#Session =>
        val token = SecurityUtil.generateRememberMeToken
        usersRepository.updateRememberToken(userId, token)
        token
      }

    override def signUpUser(user: User, host: String): Future[ValidationNel[String, String]] = withSession {
      implicit s: JdbcBackend#Session =>

        def sendActivationLink(userUid: String) = {
          val url = s"http://$host/activate/$userUid"
          val message = s"""<p>Dear ${user.username}!</p>
            |<p>This mail is to confirm your registration at ${propertiesService.getInstanceName}.<br/>
            |Please follow the link below to activate your account <br/><a href='$url'>$url</p>""".stripMargin
          val mailFuture = mailService.sendEmail(
            user.email, s"Account activation at ${propertiesService.getInstanceName}", message
          )

          mailFuture.onSuccess {
            case _ => Logger.info(s"Activation link was sent to user ${user.username}")
          }
          mailFuture.onFailure {
            case error => Logger.warn(s"Problem with sending activation link to user ${user.username}", error)
          }
        }

        def createUserRecord(uid: String): Unit = {
          val salt = some(SecurityUtil.generateSalt)
          val encodedPassword = SecurityUtil.encodePassword(user.password, salt)
          val record = UserRecord(None, user.username, encodedPassword, user.email, salt = salt, uid = uid)
          usersRepository.insert(record)
          sendActivationLink(record.uid)
        }

        userValidator.validate(user).fold(
          fail = e => Future.successful(Failure(e)),
          succ = s => {
            for {
              uid <- authenticationManager.register(UserInfo(user.username, SecurityUtil.md5(user.password), user.email))
            } yield {
              for {
                uuid <- uid
              } yield createUserRecord(uuid)
              uid
            }
          }
        )
    }

    override def activateUser(uid: String): Future[ValidationNel[String, String]] = withSession {
      implicit s: JdbcBackend#Session =>
        authenticationManager.activate(uid).map { result =>
          import scalaz.Validation.FlatMap._
          for {
            _ <- result
            user <- usersRepository.getByUID(uid).toSuccess(NonEmptyList("User not found"))
            token = SecurityUtil.generateRememberMeToken
            _ = usersRepository.update(user.copy(rememberToken = Some(token), active = true))
          } yield token
        }
    }
  }
}




