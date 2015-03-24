package security

import models.database.UserRecord
import play.api.Logger
import repositories.UsersRepositoryComponent
import services.SessionProvider

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import security.UserInfoImplicitConversions._
import scala.util.Try
import scalaz._
import Scalaz._
import play.api.libs.ws.WS
import scala.slick.jdbc.JdbcBackend
import utils.SecurityUtil

private[security] trait AuthenticationManager {
  def authenticate(username: String, password: String): Option[UserInfo]
}


class FakeAuthenticationManager extends AuthenticationManager {
  override def authenticate(username: String, password: String) = {
    (username, password) match {
      case ("admin", "admin") => UserInfo("admin", password, "firstName".some, "lastName".some, true).some
      case _ => none[UserInfo]
    }
  }
}

class PoulpeAuthenticationManager(poulpeUrl: String) extends AuthenticationManager {

  override def authenticate(username: String, password: String) = {
    val passwordHash = SecurityUtil.md5(password)
    Await.result(for {
      response <- sendRequest(poulpeUrl, username, passwordHash)
      xmlResponseBody = response.xml
    } yield {
      for {
        status <- (xmlResponseBody \\ "status").headOption.map(_.text) if status == "success"
        enabled <- (xmlResponseBody \\ "enabled").headOption.map(_.text) if enabled == "true"
        returnedUsername <- (xmlResponseBody \\ "username").headOption.map(_.text)
        firstName = (xmlResponseBody \\ "firstName").headOption.map(_.text)
        lastName = (xmlResponseBody \\ "lastName").headOption.map(_.text)
        password = (xmlResponseBody \\ "password").headOption.map(_.text)
      } yield UserInfo(returnedUsername, "todo", firstName, lastName, true)
    }, 10.seconds)
  }

  private def sendRequest(poulpeUrl: String, username: String, password: String) =
    WS.url(s"$poulpeUrl/rest/authenticate")
      .withQueryString(("username", username), ("passwordHash", password))
      .get()

}

class LocalDatabaseAuthenticationManager(repo: UsersRepositoryComponent, provider: SessionProvider)
  extends AuthenticationManager {
  override def authenticate(username: String, password: String) = provider.withSession {
    implicit s: JdbcBackend#Session =>
      def isValidPassword(user: UserRecord) = {
        !password.isEmpty && user.password === SecurityUtil.encodePassword(password, user.salt)
      }
      repo.usersRepository.getByUsername(username) match {
        case user: Some[UserRecord] => if (isValidPassword(user.get)) Some(user.get) else None
        case _ => None
      }
  }
}

class CompositeAuthenticationManager(poulpeAuthManager: Option[AuthenticationManager],
                                     localAuthManager: AuthenticationManager)
  extends AuthenticationManager {
  override def authenticate(username: String, password: String) = {
    Try {
      poulpeAuthManager.cata(
        none = none[UserInfo],
        some = manager => manager.authenticate(username, password))
    } match {
      case scala.util.Success(userInfo) => userInfo
      case scala.util.Failure(e) =>
        Logger.error("Error while asking Poulpe to authenticate user", e)
        localAuthManager.authenticate(username, password)
    }
  }
}


