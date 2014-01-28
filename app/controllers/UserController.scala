package controllers

import play.api.mvc.{Controller, Action}
import services.ArticlesServiceComponent
import security.Authentication

/**
 *
 */
trait UserController {
  this: Controller with ArticlesServiceComponent with Authentication  =>


  def viewProfile(userName: String, page: Int) = Action { implicit request =>
      Ok(views.html.profile(articlesService.getPageForUser(page, userName), userName))
  }
}
