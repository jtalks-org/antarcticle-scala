package controllers

import play.api.mvc.{Action, Controller}
import security.Authentication
import models.database.Notification
import services.NotificationsServiceComponent


/**
 *  Serves request for user notifications.
 *  All requests are AJAX, so this controller does not generate full-featured pages.
 */
trait NotificationsController {
  this: Controller with NotificationsServiceComponent with PropertiesProvider with Authentication =>

  def getNotifications = Action {
    implicit request =>
      val notifications = notificationsService.getNotificationsForCurrentUser
      Ok(views.html.templates.notifications(notifications))
  }

  def getNotification(id: Int) = Action {
    implicit request =>
      notificationsService.getAndDeleteNotification(id).fold(
        fail = nel => Forbidden(""),
        succ =  {
          case Some(notification : Notification) =>
            Found(routes.ArticleController.viewArticle(
              notification.articleId).absoluteURL() + "#" + notification.commentId
            )
          case None => NotFound(views.html.errors.notFound())
        }
      )
  }

  def dismissNotification(id: Int) = Action {
    implicit request =>
      val result = notificationsService.deleteNotification(id)
      result.fold(
        fail = nel => Forbidden(""),
        succ = nel => Ok("")
      )
  }

  def dismissAllNotifications = Action {
    implicit request =>
      val result = notificationsService.deleteNotificationsForCurrentUser()
      result.fold(
        fail = nel => Forbidden(""),
        succ = nel => Ok(views.html.templates.notifications())
      )
  }
}