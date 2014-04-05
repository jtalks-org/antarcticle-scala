package controllers

import play.api.mvc.{Controller, Action}
import services.{UsersServiceComponent, ArticlesServiceComponent}
import security.Authentication
import models.database.UserRecord

/**
 * Maintains all information from user profile: article list for a particular user,
 * preferences and so on. Authentication and permission management are out of scope,
 * please refer to [[controllers.AuthenticationController]] for further details.
 */
trait UserController {
  this: Controller with ArticlesServiceComponent with UsersServiceComponent with Authentication  =>

  def viewProfile(userName: String, tags : Option[String] = None) = viewProfilePaged(userName, 1, tags)
  
  def viewProfilePaged(userName: String, page: Int, tags : Option[String] = None) = Action { implicit request =>
    usersService.getByName(userName) match {
      case user : Some[UserRecord] =>
        articlesService.getPageForUser(page, userName, tags).fold(
          fail => NotFound(views.html.errors.notFound()),
          succ = articles => Ok(views.html.profile(articles, user.get))
        )
      case None =>
        NotFound(views.html.errors.notFound())
    }
  }
}
