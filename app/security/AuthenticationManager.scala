package security

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scalaz._
import Scalaz._
import play.api.libs.ws.WS
import java.security.MessageDigest
import utils.HashingUtil

private[security] trait AuthenticationManager {
  def authenticate(username: String, password: String): Future[Option[UserInfo]]
}

class FakeAuthenticationManager extends AuthenticationManager {
  def authenticate(username: String, password: String) = {
    future {
      (username, password) match {
        case ("admin", "admin") => UserInfo("admin", password, "firstName".some, "lastName".some).some
        case _ => none[UserInfo]
      }
    }
  }
}

class PoulpeAuthenticationManager(poulpeUrl: String) extends AuthenticationManager {
  def authenticate(username: String, password: String) = {
    val passwordHash = HashingUtil.md5(password)

    for {
      response <- sendRequest(poulpeUrl, username, passwordHash)
      xmlResponseBody = response.xml
    } yield {
      for {
         status <- (xmlResponseBody \\ "status").headOption.map(_.text) if status == "success"
         enabled <- (xmlResponseBody \\ "enabled").headOption.map(_.text) if enabled == "true"
         firstName = (xmlResponseBody \\ "firstName").headOption.map(_.text)
         lastName = (xmlResponseBody \\ "lastName").headOption.map(_.text)
         password = (xmlResponseBody \\ "password").headOption.map(_.text)
      } yield UserInfo(username, "todo",  firstName, lastName)
    }
  }

  private def sendRequest(poulpeUrl: String, username: String, password: String) = {
    WS.url(s"$poulpeUrl/rest/authenticate")
      .withQueryString(("username", username), ("passwordHash", password))
      .get()
  }
}
