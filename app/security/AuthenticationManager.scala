package security

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scalaz._
import Scalaz._
import play.api.libs.ws.WS
import java.security.MessageDigest

private[security] trait AuthenticationManager {
  def authenticate(username: String, password: String): Future[Option[UserInfo]]
}

class FakeAuthenticationManager extends AuthenticationManager {
  def authenticate(username: String, password: String) = {
    future {
      if (username == "admin" && password == "admin") {
        UserInfo("admin", "firstName".some, "lastName".some).some
      } else {
        None
      }
    }
  }
}

class PoulpeAuthenticationManager(poulpeUrl: String) extends AuthenticationManager {
  def authenticate(username: String, password: String) = {
    val passwordHash = generateMd5Hash(password)

    for {
      response <- sendRequest(poulpeUrl, username, passwordHash)
      xmlResponseBody = response.xml
    } yield {
      //TODO: log error on malformed response
      val status = (xmlResponseBody \\ "status").headOption.map(_.text)
      status.filter(_ == "success").map { _ =>
        val firstName = (xmlResponseBody \\ "firstName").headOption.map(_.text)
        val lastName = (xmlResponseBody \\ "lastName").headOption.map(_.text)
        UserInfo(username, firstName, lastName).some
      } getOrElse None
    }
  }

  private def generateMd5Hash(str: String) = {
    val digest = MessageDigest.getInstance("MD5").digest(str.getBytes)
    new java.math.BigInteger(1, digest).toString(16)
  }

  private def sendRequest(poulpeUrl: String, username: String, password: String) = {
    WS.url(s"$poulpeUrl/rest/authenticate")
      .withQueryString(("username", username), ("passwordHash", password))
      .get()
  }
}
