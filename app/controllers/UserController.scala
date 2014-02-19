package controllers

import play.api.mvc.{Controller, Action}
import services.{UsersServiceComponent, ArticlesServiceComponent}
import security.Authentication

/**
 * Maintains all information from user profile: article list for a particular user,
 * preferences and so on. Authentication and permission management are out of scope,
 * please refer to [[controllers.AuthenticationController]] for further details.
 */
trait UserController {
  this: Controller with ArticlesServiceComponent with UsersServiceComponent with Authentication  =>

  def viewProfile(userName: String, page: Int, tag : Option[String] = None) = Action { implicit request =>
    val user = usersService.getByName(userName)
    user match {
      case Some(_) => Ok(views.html.profile(articlesService.getPageForUser(page, userName, tag), user.get))
      case None => NotFound(views.html.errors.notFound())
    }
  }
}
