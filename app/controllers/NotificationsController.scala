package controllers

import play.api.mvc.{Action, Controller}
import security.Authentication
import models.database.Notification
import services.NotificationsServiceComponent
import scalaz.NonEmptyList
import security.Result.{NotAuthorized, Authorized}


/**
 *  Serves request for user notifications.
 *  All requests are AJAX, so this controller does not generate full-featured pages.
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
      notificationsService.getAndDeleteNotification(id).fold(
        fail = errors,
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
      val result = notificationsService.deleteNotification(id);
      result.fold(
        fail = errors,
        succ =  {
          case Authorized(_) => Ok("")
          case NotAuthorized() => Unauthorized("Not authorized to remove this notification")
        }
      )
  }

  def dismissAllNotifications = Action {
    implicit request =>
      notificationsService.deleteNotificationsForCurrentUser() match {
        case Authorized(_) => Ok("")
        case NotAuthorized() => Unauthorized("Not authorized to remove this notification")
      }
  }

  private def errors(errors: NonEmptyList[String]) = {
    BadRequest(views.html.templates.formErrors(errors.list))
  }
}