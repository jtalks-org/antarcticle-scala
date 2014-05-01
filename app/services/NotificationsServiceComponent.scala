package services

import repositories.NotificationsRepositoryComponent
import models.database._
import scalaz._
import Scalaz._


/**
 * @author Anuar_Nurmakanov
 */
trait NotificationsServiceComponent {
  val notificationsService: NotificationsService

  trait NotificationsService {
    def getNotificationForCurrentUser: Seq[Notification]

    def getNotification(id: Int): Option[Notification]

    def deleteNotification(id: Int): ValidationNel[String, Unit]
  }
}

trait NotificationsServiceComponentImpl extends NotificationsServiceComponent {
  this: SessionProvider with NotificationsRepositoryComponent =>

  val notificationsService = new NotificationsServiceImpl

  class NotificationsServiceImpl extends NotificationsService{

    override def getNotificationForCurrentUser: Seq[Notification] = withSession { implicit session =>
      notificationsRepository.getNotificationsForArticlesOf(1)
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
