package repositories

import scala.slick.jdbc.JdbcBackend
import models.database.{NotificationsSchemaComponent, Profile, TagsSchemaComponent, Notification}

/**
 * @author Anuar_Nurmakanov
 */
trait NotificationsRepositoryComponent {
  val notificationsRepository: NotificationsRepository
  /**
   * Provides basic comment-related operations over a database.
   * Database session should be provided by a caller via implicit parameter.
   */
  trait NotificationsRepository {

    def getNotificationsForArticlesOf(articlesAuthorId: Int)(implicit session: JdbcBackend#Session): Seq[Notification]

    def deleteNotification(id: Int) (implicit session: JdbcBackend#Session)
  }
}

trait NotificationsRepositoryComponentImpl extends NotificationsRepositoryComponent {
  this: NotificationsSchemaComponent with Profile =>

  val notificationsRepository = new NotificationsRepositoryImpl

  class NotificationsRepositoryImpl extends NotificationsRepository {

    def getNotificationsForArticlesOf(articlesAuthorId: Int)(implicit session: JdbcBackend#Session): Seq[Notification] = {
      Seq()
    }

    def deleteNotification(id: Int)(implicit session: JdbcBackend#Session) = {
    }
  }
}
