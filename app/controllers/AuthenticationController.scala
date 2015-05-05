package controllers

import conf.Constants._
import models.UserModels.User
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import scalaz._
import Scalaz._
import security.{Authentication, SecurityServiceComponent}
import views.html
import scala.concurrent.ExecutionContext.Implicits.global
import scala.async.Async.{async, await}

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


  def getReferer(implicit request: Request[AnyContent]) : String = {
    request.session.get(REFERER).filter(
      referer => referer != routes.AuthenticationController.showRegistrationPage().url
    ).getOrElse(routes.ArticleController.allArticles().absoluteURL())
  }

  def login = Action.async { implicit request =>
    val (username, password, referer) = loginForm.bindFromRequest.get
    securityService.signInUser(username, password).map {_.fold(
      fail = nel => BadRequest(views.html.templates.formErrors(nel.list)),
      succ = { case (token, authUser) =>
        Ok(referer)
          // http only to prevent session hijacking with XSS
          .withCookies(Cookie(rememberMeCookie, token, Some(rememberMeExpirationTime), httpOnly = true))
      }
    )}
  }

  def logout = Action {
    Redirect(controllers.routes.ArticleController.allArticles())
      .withNewSession
      .discardingCookies(DiscardingCookie(rememberMeCookie))
  }

  def showRegistrationPage = Action { implicit request =>
    Ok(html.signup(registerForm))
  }

  def register = Action.async { implicit request =>
    val (username, email, password) = registerForm.bindFromRequest.get
    val activationUrl = request.path.indexOf("/signup") match {
      case i if i < 0 => request.host
      case i => request.host + request.path.slice(0, i)
    }
    securityService.signUpUser(User(username, email, password), activationUrl).map {
      _.fold(
        fail = nel => BadRequest(views.html.templates.formErrors(nel.list)),
        succ = uid => Ok(routes.ArticleController.allArticles().absoluteURL())
      )
    }
  }

  def activate(uid: String) = Action.async { implicit request =>
    val mainPage = Redirect(routes.ArticleController.allArticles())
    async {
      val cookie = await(securityService.activateUser(uid)).toOption map {
        token => Cookie(rememberMeCookie, token, some(rememberMeExpirationTime), httpOnly = true)
      }
      cookie.cata(mainPage.withCookies(_), mainPage)
    }
  }
}
