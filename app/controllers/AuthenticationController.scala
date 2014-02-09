package controllers

import play.api.mvc.{DiscardingCookie, Cookie, Action, Controller}
import security.{Authentication, SecurityServiceComponent}
import org.joda.time.DateTimeConstants
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import views.html
import conf.Constants
import scala.concurrent.ExecutionContext.Implicits.global

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
          Ok(routes.ArticleController.listAllArticles().absoluteURL())
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
