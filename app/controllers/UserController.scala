package controllers

import play.api.mvc.{Controller, Action}
import services.ArticlesServiceComponent

/**
 *
 */
trait UserController {
  this: Controller with ArticlesServiceComponent =>

  //todo: must be protected with authentication
  def viewProfile(userName: String, page: Int = 0) = Action {
    implicit request => {
      Ok(views.html.profile(articlesService.getPageForUser(page, userName), userName))
    }
  }
}
