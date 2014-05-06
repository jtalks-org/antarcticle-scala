package services

import repositories.NotificationsRepositoryComponent
import scala.slick.jdbc.JdbcBackend
import security.{Entities, Principal, AuthenticatedUser}
import models.database.CommentRecord
import models.database.Notification
import security.Permissions.{Delete, Update}
import security.Result.AuthorizationResult
import scalaz._
import Scalaz._


/**
 *
 */
trait NotificationsServiceComponent {
  val notificationsService: NotificationsService

  trait NotificationsService {
    def createNotification(cr: CommentRecord)(implicit principal: Principal, session: JdbcBackend#Session)

    def getNotificationsForCurrentUser(implicit principal: Principal): Seq[Notification]

    def getAndDeleteNotification(id: Int)(implicit principal: Principal): Option[Notification]

    def deleteNotification(notificationId: Int)(implicit principal: Principal): ValidationNel[String, AuthorizationResult[Unit]]

    def deleteNotificationsForCurrentUser()(implicit principal: Principal)
  }

}

trait NotificationsServiceComponentImpl extends NotificationsServiceComponent {
  this: SessionProvider with NotificationsRepositoryComponent =>

  val notificationsService = new NotificationsServiceImpl

  class NotificationsServiceImpl extends NotificationsService {


    def createNotification(cr: CommentRecord)(implicit principal: Principal, session : JdbcBackend#Session) {
        // todo: pattern matching by auth
        // todo: don't create notifications for own comments
        notificationsRepository.insertNotification(
          new Notification(None, cr.userId, cr.articleId, cr.id.get, "", cr.content.take(150), cr.createdAt))
    }

    def getNotificationsForCurrentUser(implicit principal: Principal) = withSession {
      implicit session =>
        principal.isAuthenticated match {
          case true => {
            val currentUserId = principal.asInstanceOf[AuthenticatedUser].userId
            notificationsRepository.getNotificationsForUser(currentUserId)
          }
          case false =>
            Seq()
        }
    }

    def getAndDeleteNotification(id: Int)(implicit principal: Principal) = withSession {
      implicit session =>
        // todo: pattern matching by auth
        val notification = notificationsRepository.getNotification(id)
        this.deleteNotification(id)
        notification
    }

    def deleteNotification(id: Int)(implicit principal: Principal) = withTransaction {
      implicit session =>
        val notification = notificationsRepository.getNotification(id)
        notification match {
          case Some(n) =>
            principal.doAuthorizedOrFail(Delete, n){ () =>
              val currentUserId = principal.asInstanceOf[AuthenticatedUser].userId
              notificationsRepository.deleteNotification(id, currentUserId)
              ()
            }.successNel
          case None => "Deleted notification doesn't exist".failureNel
        }
    }

    def deleteNotificationsForCurrentUser()(implicit principal: Principal) = withTransaction {
      implicit session =>
        // todo: pattern matching by auth
        val currentUserId = principal.asInstanceOf[AuthenticatedUser].userId
        notificationsRepository.deleteNotificationsForUser(currentUserId)
    }
}
}
