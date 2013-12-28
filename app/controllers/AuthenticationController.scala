package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._

/**
 *  Handles sign in and sign out user actions.
 *  After successful authentication current user name will be available in session,
 *  one can obtain it as follows:
 *  {{{
 *    request.session.get(Security.username)
 *  }}}
 *
 */
trait AuthenticationController {
  this: Controller =>

  val loginForm = Form(
    tuple(
      "login" -> text,
      "password" -> text
    ) verifying("Invalid username or password", result => result match {
      case (login, password) => check(login, password)
    })
  )

  def check(login: String, password: String) = {
    login == "admin" && password == "admin"
  }

  def signin = Action {
    implicit request => Ok(html.signin(loginForm))
  }

  def authenticate = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.signin(formWithErrors)),
        user => Redirect(routes.ArticleController.listAllArticles()).withSession(Security.username -> user._1)
      )
  }

  def signout = Action {
    Redirect(routes.ArticleController.listAllArticles()).withNewSession
  }
}

