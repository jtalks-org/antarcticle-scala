package controllers

import play.api.mvc.{Action, Controller}
import security.Authentication
import models.database.Notification
import services.NotificationsServiceComponent
import security.Result

/**
 *   Temporary notification controller implementation to be used until
 *   proper backend for notifications is implemented
 */
trait NotificationsController {
  this: Controller with NotificationsServiceComponent with Authentication =>

  def getNotifications = Action {
    implicit request =>
      val notifications = notificationsService.getNotificationsForCurrentUser
      Ok(views.html.templates.notifications(notifications))
  }

  def getNotification(id: Int) = Action {
    implicit request =>
      notificationsService.getAndDeleteNotification(id) match {
        case Some(notification : Notification) =>
          Found(routes.ArticleController.viewArticle(notification.articleId).absoluteURL() + "#" + notification.commentId)
        case None => NotFound(views.html.errors.notFound())
      }
  }

  def dismissNotification(id: Int) = Action {
    implicit request =>
      notificationsService.deleteNotification(id)
      Ok("")
  }

  def dismissAllNotifications = Action {
    implicit request =>
      notificationsService.deleteNotificationsForCurrentUser()
      Ok(views.html.templates.notifications(List()))
  }
}