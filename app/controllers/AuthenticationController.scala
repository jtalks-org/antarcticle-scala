package controllers

import play.api.mvc.{DiscardingCookie, Cookie, Action, Controller}
import security.{Authentication, SecurityServiceComponent}
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import views.html
import conf.Constants._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.data.format.Formatter
import play.api.data.format.Formats.stringFormat

/**
 *  Handles sign in and sign out user actions.
 *  After successful sign in a new session is created with http-only "remember me" 
 *  cookies to prevent session fixation attacks. By default this cookie is valid for four weeks. 
 */
trait AuthenticationController {
  this: Controller with SecurityServiceComponent with Authentication  =>

  def trimmer: Formatter[String] = new Formatter[String] {
    def bind(key: String, data: Map[String, String]) = {
      stringFormat.bind(key, data).fold(
        left => Left(left),
        right => Right(right.trim())
      )
    }
    def unbind(key: String, value: String) = stringFormat.unbind(key, value.trim())
  }

  val loginForm = Form(
    tuple(
      "login" -> of[String](trimmer),
      "password" -> text
    )
  )

  def showLoginPage = Action { implicit request =>
    Ok(html.signin(loginForm))
  }

  def login = Action.async { implicit request =>
    val (username, password) = loginForm.bindFromRequest.get
    for {
      signInResult <- securityService.signInUser(username, password)
    } yield {
      signInResult.fold(
        fail = nel => BadRequest(views.html.templates.formErrors(nel.list)),
        succ = { case (token, authUser) =>
          Ok(routes.ArticleController.allArticles().absoluteURL())
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
