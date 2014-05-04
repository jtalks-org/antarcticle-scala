package repositories

import scala.slick.jdbc.JdbcBackend
import models.database.{NotificationsSchemaComponent, Profile, Notification}

/**
 * @author Anuar_Nurmakanov
 */
trait NotificationsRepositoryComponent {
  val notificationsRepository: NotificationsRepository

  trait NotificationsRepository {

    def insertNotification(notification: Notification)(implicit session: JdbcBackend#Session)

    def getNotificationsForUser(userId: Int)(implicit session: JdbcBackend#Session): Seq[Notification]

    def getNotification(id: Int)(implicit session: JdbcBackend#Session): Option[Notification]

    def deleteNotification(notificationId: Int, userId: Int) (implicit session: JdbcBackend#Session)

    def deleteNotificationsForUser(userId: Int) (implicit session: JdbcBackend#Session)
  }
}

trait NotificationsRepositoryComponentImpl extends NotificationsRepositoryComponent {
  this: NotificationsSchemaComponent with Profile =>

  import profile.simple._

  val notificationsRepository = new NotificationsRepositoryImpl

  val compiledForInsert = notifications.insertInvoker
  val compiledByUser = Compiled((id: Column[Int]) => notifications.filter(id === _.userId))
  val compiledById = Compiled((id: Column[Int]) => notifications.filter(id === _.id))
  val compiledByIdAndUser =
    Compiled((id: Column[Int], userId: Column[Int]) => notifications.filter(id === _.id).filter(userId === _.userId))

  class NotificationsRepositoryImpl extends NotificationsRepository {

    def insertNotification(notification: Notification)(implicit session: JdbcBackend#Session) =
      compiledForInsert.insert(notification)

    def getNotificationsForUser(userId: Int)(implicit session: JdbcBackend#Session) = compiledByUser(userId).list()

    def getNotification(id: Int)(implicit session: JdbcBackend#Session) = compiledById(id).firstOption

    def deleteNotification(notificationId: Int, userId: Int)(implicit session: JdbcBackend#Session) =
      compiledByIdAndUser(notificationId, userId).delete

    def deleteNotificationsForUser(userId: Int)(implicit session: JdbcBackend#Session) =
      compiledByUser(userId).delete

  }
}
