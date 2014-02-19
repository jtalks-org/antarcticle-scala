package security

import play.api.mvc._
import repositories.UsersRepositoryComponent
import services.SessionProvider
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits._
import scalaz._
import Scalaz._
import Authorities.Authority
import conf.Constants

case class AuthenticatedUser(userId: Int, username: String, authority: Authority)
              extends AuthenticatedPrincipal(userId, authority)

trait Authentication {
  this: Controller with UsersRepositoryComponent with SessionProvider =>

  // TODO: move AuthenticatedUser creation to sec. service
  def currentUser(implicit request: RequestHeader): Option[AuthenticatedUser] = {
    def getUserByToken(token: String) = withSession(usersRepository.getByRemeberToken(token)(_))

    for {
      tokenCookie <- request.cookies.get(Constants.rememberMeCookie)
      token = tokenCookie.value
      user <- getUserByToken(token)
      userId <- user.id
    } yield  {
      val authority = if (user.admin) Authorities.Admin else Authorities.User
      AuthenticatedUser(userId, user.username, authority)
    }
  }

  implicit def currentPrincipal(implicit request: RequestHeader): Principal =
    currentUser getOrElse AnonymousPrincipal

  // implicit def authUserToOption(authUser: AuthenticatedUser) = Some(authUser)

  def withUser(f: AuthenticatedUser => Request[AnyContent] => Result,
    onUnauthorized: RequestHeader => SimpleResult = defaultOnUnauthorized): EssentialAction = {
    Action { implicit request =>
      currentUser.cata(
        some = user => f(user)(request),
        none = onUnauthorized(request)
      )
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

  protected val defaultOnUnauthorized = (req: RequestHeader) =>
    Redirect(controllers.routes.AuthenticationController.showLoginPage())

  private val defaultOnUnauthorizedAsync = (req: RequestHeader) => Future(defaultOnUnauthorized(req))
}
