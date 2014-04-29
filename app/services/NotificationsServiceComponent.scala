package services

import models.database.Notification
import scala.slick.jdbc.JdbcBackend
import repositories.{NotificationsRepositoryComponent}


/**
 * @author Anuar_Nurmakanov
 */
trait NotificationsServiceComponent {
  val notificationsService: NotificationsService

  trait NotificationsService {
    def getNotificationForArticlesOf(articlesAuthorId: Int)(implicit s: JdbcBackend#Session): Seq[Notification]
  }
}

trait NotificationsServiceComponentImpl extends NotificationsServiceComponent {
  this: SessionProvider with NotificationsRepositoryComponent =>

  val notificationsService = new NotificationsServiceImpl

  class NotificationsServiceImpl extends NotificationsService{
    def getNotificationForArticlesOf(articlesAuthorId: Int)(implicit s: JdbcBackend#Session): Seq[Notification] = {
      Seq()
    }
  }
}
