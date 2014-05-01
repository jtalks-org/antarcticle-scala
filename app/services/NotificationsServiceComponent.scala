package services

import repositories.NotificationsRepositoryComponent
import models.database._
import scalaz._
import Scalaz._
import security.{AuthenticatedUser, Principal}


/**
 * @author Anuar_Nurmakanov
 */
trait NotificationsServiceComponent {
  val notificationsService: NotificationsService

  trait NotificationsService {
    def getNotificationForCurrentUser(implicit principal: Principal): Seq[Notification]

    def getNotification(id: Int): Option[Notification]

    def deleteNotification(id: Int): ValidationNel[String, Unit]
  }
}

trait NotificationsServiceComponentImpl extends NotificationsServiceComponent {
  this: SessionProvider with NotificationsRepositoryComponent =>

  val notificationsService = new NotificationsServiceImpl

  class NotificationsServiceImpl extends NotificationsService{

    override def getNotificationForCurrentUser(implicit principal: Principal): Seq[Notification] = withSession { implicit session =>
      val currentUserId = principal.asInstanceOf[AuthenticatedUser].userId
      notificationsRepository.getNotificationsForArticlesOf(currentUserId)
    }

    override def getNotification(id: Int): Option[Notification] = withSession { implicit session =>
      notificationsRepository.getNotification(id)
    }

    override def deleteNotification(id: Int) = withTransaction{ implicit session =>
      notificationsRepository.getNotification(id) match {
        case None => "Notification not found".failureNel
        case Some(n) => {
          notificationsRepository.deleteNotification(id)
        }.successNel
      }

    }
  }
}
