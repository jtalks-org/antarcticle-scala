package security

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scalaz._
import Scalaz._

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
