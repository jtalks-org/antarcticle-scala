package controllers

import conf.Constants._
import models.UserModels.User
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import security.{Authentication, SecurityServiceComponent}
import views.html

/**
 *  Handles sign in and sign out user actions.
 *  After successful sign in a new session is created with http-only "remember me" 
 *  cookies to prevent session fixation attacks. By default this cookie is valid for four weeks. 
 */
trait AuthenticationController {
  this: Controller with SecurityServiceComponent with PropertiesProvider with Authentication  =>

  val loginForm = Form(
    tuple(
      "login" -> text,
      "password" -> text,
      "referer" -> text
    )
  )

  val registerForm = Form(
    tuple(
      "login" -> text,
      "email" -> text,
      "password" -> text
    )
  )

  def showLoginPage = Action { implicit request =>
    Ok(html.signin(loginForm.bind(Map("referer" -> getReferer))))
  }


  def getReferer(implicit request: Request[AnyContent]) : String =
    request.session.get(REFERER).getOrElse(routes.ArticleController.allArticles().absoluteURL())


  def login = Action { implicit request =>
    val (username, password, referer) = loginForm.bindFromRequest.get
    securityService.signInUser(username, password).fold(
      fail = nel => BadRequest(views.html.templates.formErrors(nel.list)),
      succ = { case (token, authUser) =>
        Ok(referer)
          // http only to prevent session hijacking with XSS
          .withCookies(Cookie(rememberMeCookie, token, Some(rememberMeExpirationTime), httpOnly = true))
      }
    )
  }

  def logout = Action {
    Redirect(controllers.routes.ArticleController.allArticles())
      .withNewSession
      .discardingCookies(DiscardingCookie(rememberMeCookie))
  }

  def showRegistrationPage = Action { implicit request =>
    Ok(html.signup(registerForm))
  }

  def register() = Action { implicit request =>
    val (username, email, password) = registerForm.bindFromRequest.get
    securityService.signUpUser(User(username, email, password)).fold(
      fail = nel => BadRequest(views.html.templates.formErrors(nel.list)),
      succ = user => Ok(routes.ArticleController.allArticles().absoluteURL())
    )
  }

  def activate(uid: String) = Action { implicit request =>
    Ok("Activation is not allowed")
  }
}
