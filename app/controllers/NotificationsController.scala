package controllers

import play.api.mvc.{Action, Controller}
import security.Authentication
import models.database.Notification
import java.sql.Timestamp
import scala.collection.mutable.ArrayBuffer
import services.NotificationsServiceComponent
import models.ArticleModels.ArticleDetailsModel
import security.Result.{Authorized, NotAuthorized}

/**
 *   Temporary notification controller implementation to be used until
 *   proper backend for notifications is implemented
 */
trait NotificationsController {
  this: Controller with NotificationsServiceComponent with Authentication =>

  def getNotifications = Action {
    implicit request =>
      val notifications = notificationsService.getNotificationForCurrentUser
      Ok(views.html.templates.notifications(notifications))
  }

  def getNotification(id: Int) = Action {
    implicit request =>
      notificationsService.getNotification(id) match {
        case Some(notification : Notification) => Found(routes.ArticleController.viewArticle(notification.articleId).absoluteURL() + "#" + notification.commentId)
        case None => NotFound(views.html.errors.notFound())
      }
  }

  def dismissNotification(id: Int) = Action {
    implicit request =>
      notificationsService.deleteNotification(id).fold(
        fail = _ => NotFound(views.html.errors.notFound()),
        succ = _ => Ok(routes.NotificationsController.getNotifications.absoluteURL())//TODO temporary
      )
  }

  def dismissAllNotifications = Action {
    implicit request =>
      Ok(views.html.templates.notifications(List()))
  }
}