package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import views._
import security.SecurityServiceComponent

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
  this: Controller with SecurityServiceComponent =>

  val fourWeeks = 2419200
  val rememberMeExpirationTime = fourWeeks

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
        succ = _ match {
          case (token, authUser) =>
            Redirect(routes.ArticleController.listAllArticles())
              .withSession(Security.username -> authUser.username)
              // TODO: move this constant
              .withCookies(Cookie("remember_token", token, Some(rememberMeExpirationTime)))
        }
      )
    }
  }

  def signout = Action {
    Redirect(routes.ArticleController.listAllArticles())
      .withNewSession
      // TODO: move this constant
      .discardingCookies(DiscardingCookie("remember_token"))
  }
}

