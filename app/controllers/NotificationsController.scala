package controllers

import play.api.mvc.{Action, Controller}
import security.Authentication
import models.database.Notification
import java.sql.Timestamp
import scala.collection.mutable.ArrayBuffer

/**
 *   Temporary notification controller implementation to be used until
 *   proper backend for notifications is implemented
 */
trait NotificationsController {
  this: Controller with Authentication =>

  var data = ArrayBuffer[Notification](
    Notification(Some(1), 1, 1, 1, "Yo mama is so stupid", "she thought a quarterback was a refund", new Timestamp(1)),
    Notification(Some(2), 1, 2, 1, "Yet another notification", "There are so many of them", new Timestamp(100000000000l)),
    Notification(Some(3), 1, 2, 1, "These notifications are fake", "User story is not ready yet", new Timestamp(500000000000l))
  )

  def getNotifications = Action {
    implicit request =>
      Ok(views.html.templates.notifications(data))
  }

  def getNotification(id: Int) = Action {
    implicit request =>
      val notification = data.filter((notification) => notification.id == Some(id)).head
      data = data.filter((notification) => notification.id != Some(id))
      Found(routes.ArticleController.viewArticle(notification.articleId).absoluteURL() + "#" + notification.commentId)
  }

  def dismissNotification(id: Int) = Action {
    implicit request =>
      data = data.filter((notification) => notification.id != Some(id))
      Ok(views.html.templates.notifications(List()))
  }

  def dismissAllNotifications = Action {
    implicit request =>
      data.clear()
      Ok(views.html.templates.notifications(List()))
  }
}