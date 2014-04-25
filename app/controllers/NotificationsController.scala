package controllers

import play.api.mvc.{Action, Controller}
import security.Authentication

/**
 *
 */
trait NotificationsController {
  this: Controller with Authentication =>

  def notificationsForCurrentUser() = Action {
    implicit request =>
      //todo: implement me
      Ok(views.html.templates.notifications())
  }

  def removeNotification(id: Int) = Action {
    implicit request =>
      //todo: implement me
      Ok("")
  }
}