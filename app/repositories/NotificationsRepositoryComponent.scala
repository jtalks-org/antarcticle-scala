package repositories

import scala.slick.jdbc.JdbcBackend
import models.database.{NotificationsSchemaComponent, Profile, Notification}

/**
 * @author Anuar_Nurmakanov
 */
trait NotificationsRepositoryComponent {
  val notificationsRepository: NotificationsRepository

  trait NotificationsRepository {

    def getNotificationsForArticlesOf(articlesAuthorId: Int)(implicit session: JdbcBackend#Session): Seq[Notification]

    def getNotification(id: Int)(implicit session: JdbcBackend#Session): Option[Notification]

    def deleteNotification(id: Int) (implicit session: JdbcBackend#Session)
  }
}

trait NotificationsRepositoryComponentImpl extends NotificationsRepositoryComponent {
  this: NotificationsSchemaComponent with Profile =>

  import profile.simple._

  val notificationsRepository = new NotificationsRepositoryImpl

  val compiledByReceiverId = Compiled((id: Column[Int]) => notifications.filter(id === _.userId))
  val compiledById = Compiled((id: Column[Int]) => notifications.filter(id === _.id))

  class NotificationsRepositoryImpl extends NotificationsRepository {

    def getNotificationsForArticlesOf(articlesAuthorId: Int)(implicit session: JdbcBackend#Session): Seq[Notification] = {
      compiledByReceiverId(articlesAuthorId).list()
    }

    def getNotification(id: Int)(implicit session: JdbcBackend#Session): Option[Notification] = {
      compiledById(id).firstOption
    }

    def deleteNotification(id: Int)(implicit session: JdbcBackend#Session) = {
      compiledById(id).compiledDelete
    }
  }
}
