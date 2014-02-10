package controllers

import play.api.mvc.{Controller, Action}
import services.{UsersServiceComponent, ArticlesServiceComponent}
import security.Authentication

/**
 *
 */
trait UserController {
  this: Controller with ArticlesServiceComponent with UsersServiceComponent with Authentication  =>

  def viewProfile(userName: String, page: Int) = Action { implicit request =>
    val user = usersService.getByName(userName)
    user match {
      case Some(_) => Ok(views.html.profile(articlesService.getPageForUser(page, userName), user.get))
      case None => NotFound
    }
  }
}
