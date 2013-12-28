package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._

/**
 *
 */
trait AuthenticationController {
  this: Controller =>

  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text
    ) verifying("Invalid username or password", result => result match {
      case (email, password) => check(email, password)
    })
  )

  def check(username: String, password: String) = {
    username == "admin" && password == "admin"
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

