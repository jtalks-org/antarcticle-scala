package security

import play.api.mvc._
import repositories.UsersRepositoryComponent
import services.SessionProvider
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import scalaz._
import Scalaz._
import scala.language.implicitConversions

case class AuthenticatedUser(id: Int, username: String)

trait Authentication {
  this: Controller with UsersRepositoryComponent with SessionProvider =>

  implicit def currentUser(implicit request: RequestHeader): Option[AuthenticatedUser] = {
    def getUserByToken(token: String) = withSession(usersRepository.getByRemeberToken(token)(_))

    for {
      tokenCookie <- request.cookies.get(Constants.RememberMeCookie)
      token = tokenCookie.value
      user <- getUserByToken(token)
      userId <- user.id
    } yield AuthenticatedUser(userId, user.username)
  }

  implicit def authUserToOption(authUser: AuthenticatedUser) = Some(authUser)

  def withUser(f: AuthenticatedUser => Request[AnyContent] => Result,
    onUnauthorized: RequestHeader => SimpleResult = defaultOnUnauthorized): EssentialAction = {
    Action { request =>
      currentUser(request).map { user =>
        f(user)(request)
      }.getOrElse(onUnauthorized(request))
    }
  }

  def withUserAsync(f: AuthenticatedUser => Request[AnyContent] => Future[SimpleResult],
    onUnauthorized: RequestHeader => Future[SimpleResult] = defaultOnUnauthorizedAsync): EssentialAction = {
    Action.async { implicit request =>
      Future(currentUser).flatMap(_.cata(
        some = user => f(user)(request),
        none = onUnauthorized(request)
      ))
    }
  }

  protected val defaultOnUnauthorized = (req: RequestHeader) => Redirect(routes.AuthenticationController.signin())

  private val defaultOnUnauthorizedAsync = (req: RequestHeader) => Future(defaultOnUnauthorized(req))
}
