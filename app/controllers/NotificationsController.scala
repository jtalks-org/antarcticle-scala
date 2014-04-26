package controllers

import play.api.mvc.{Action, Controller}
import security.Authentication
import models.database.Notification
import java.sql.Timestamp

/**
 *
 */
trait NotificationsController {
  this: Controller with Authentication =>

  def getNotifications() = Action {
    implicit request =>
      //todo: implement me
      val data = List(
        Notification(None, 1, 1, 1, "Yo mama is so stupid", "she thought a quarterback was a refund", new Timestamp(1)),
        Notification(None, 1, 2, 1, "Yet another notification", "There are so many of them", new Timestamp(100000)),
        Notification(None, 1, 2, 1, "These notifications are fake", "User story is not ready yet", new Timestamp(500000))
      )
      Ok(views.html.templates.notifications(data))
  }

  def removeNotification(id: Int) = Action {
    implicit request =>
      //todo: implement me
      Ok("")
  }

  def removeAllNotifications() = Action {
    implicit request =>
      //todo: implement me
      Ok("")
  }
}