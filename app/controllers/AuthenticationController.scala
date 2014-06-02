package controllers

import play.api.mvc._
import security.{Authentication, SecurityServiceComponent}
import play.api.data.Form
import play.api.data.Forms._
import views.html
import conf.Constants._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.Some

/**
 *  Handles sign in and sign out user actions.
 *  After successful sign in a new session is created with http-only "remember me" 
 *  cookies to prevent session fixation attacks. By default this cookie is valid for four weeks. 
 */
trait AuthenticationController {
  this: Controller with SecurityServiceComponent with Authentication  =>

  val loginForm = Form(
    tuple(
      "login" -> text,
      "password" -> text,
      "referer" -> text
    )
  )

  def showLoginPage = Action { implicit request =>
    Ok(html.signin(loginForm.bind(Map("referer" -> getReferer))))
  }


  def getReferer(implicit request: Request[AnyContent]) : String =
    request.session.get(REFERER).getOrElse(routes.ArticleController.allArticles().absoluteURL())


  def login = Action.async { implicit request =>
    val (username, password, referer) = loginForm.bindFromRequest.get
    for {
      signInResult <- securityService.signInUser(username, password)
    } yield {
      signInResult.fold(
        fail = nel => BadRequest(views.html.templates.formErrors(nel.list)),
        succ = { case (token, authUser) =>
          Ok(referer)
            // http only to prevent session hijacking with XSS
            .withCookies(Cookie(rememberMeCookie, token, Some(rememberMeExpirationTime), httpOnly = true))
        }
      )
    }
  }

  def logout = Action {
    Redirect(controllers.routes.ArticleController.allArticles())
      .withNewSession
      .discardingCookies(DiscardingCookie(rememberMeCookie))
  }
}
