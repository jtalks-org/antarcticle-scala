package security

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import views.html
import play.api.mvc.Cookie
import scala.Some
import play.api.libs.concurrent.Execution.Implicits._
import java.util.concurrent.TimeUnit
import org.joda.time.{DateTimeConstants, Weeks}

/**
 *  Handles sign in and sign out user actions.
 */
trait AuthenticationController {
  this: Controller with SecurityServiceComponent with Authentication  =>

  val rememberMeExpirationTime = DateTimeConstants.SECONDS_PER_WEEK * 4

  val loginForm = Form(
    tuple(
      "login" -> text,
      "password" -> text
    )
  )

  def signin = Action { implicit request =>
    Ok(html.signin(loginForm))
  }

  def authenticate = Action.async { implicit request =>
    val (username, password) = loginForm.bindFromRequest.get
    for {
      signInResult <- securityService.signInUser(username, password)
    } yield {
      signInResult.fold(
        fail = nel => BadRequest(views.html.templates.formErrors(nel.list)),
        succ = { case (token, authUser) =>
          Redirect(controllers.routes.ArticleController.listAllArticles())
            // http only to prevent session hijacking with XSS
            .withCookies(Cookie(Constants.RememberMeCookie, token, Some(rememberMeExpirationTime), httpOnly = true))
        }
      )
    }
  }

  def signout = Action {
    Redirect(controllers.routes.ArticleController.listAllArticles())
      .withNewSession
      // TODO: move this constant
      .discardingCookies(DiscardingCookie(Constants.RememberMeCookie))
  }
}
