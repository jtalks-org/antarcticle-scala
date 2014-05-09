package services

import repositories.NotificationsRepositoryComponent
import scala.slick.jdbc.JdbcBackend
import security.{Principal, AuthenticatedUser}
import models.database.CommentRecord
import models.database.Notification
import security.Permissions.Delete
import security.Result.{NotAuthorized, Authorized, AuthorizationResult}
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

    def getAndDeleteNotification(id: Int)(implicit principal: Principal): ValidationNel[String, Option[Notification]]

    def deleteNotification(notificationId: Int)(implicit principal: Principal): ValidationNel[String, String]

    def deleteNotificationsForCurrentUser()(implicit principal: Principal): ValidationNel[String, String]
  }

}

trait NotificationsServiceComponentImpl extends NotificationsServiceComponent {
  this: SessionProvider with NotificationsRepositoryComponent =>

  val notificationsService = new NotificationsServiceImpl

  class NotificationsServiceImpl extends NotificationsService {


    def createNotification(cr: CommentRecord)(implicit principal: Principal, session: JdbcBackend#Session) {
      principal match {
        case user: AuthenticatedUser =>
          val currentUserId = user.userId
          if (cr.userId != currentUserId)
            notificationsRepository.insertNotification(
              new Notification(None, cr.userId, cr.articleId, cr.id.get, "", cr.content.take(150), cr.createdAt))
      }
    }

    def getNotificationsForCurrentUser(implicit principal: Principal) = withSession {
      implicit session =>
        principal match {
          case user: AuthenticatedUser => notificationsRepository.getNotificationsForUser(user.userId)
          case _ => Seq()
        }
    }

    def getAndDeleteNotification(id: Int)(implicit principal: Principal) = withSession {
      implicit session =>
        principal match {
          case user: AuthenticatedUser  => {
            val notification = notificationsRepository.getNotification(id)
            this.deleteNotification(id)
            notification
          }.successNel
          case _ => "Authentication required".failureNel
        }
    }

    def deleteNotification(id: Int)(implicit principal: Principal) = withTransaction {
      implicit session =>
        principal match {
          case user: AuthenticatedUser  =>
            notificationsRepository.deleteNotification(id, user.userId)
            "".successNel
          case _ => "Authentication required".failureNel
        }
    }

    def deleteNotificationsForCurrentUser()(implicit principal: Principal) = withTransaction {
      implicit session =>
        principal match {
          case user: AuthenticatedUser =>
            notificationsRepository.deleteNotificationsForUser(user.userId)
            "".successNel
          case _ => "Authentication required".failureNel
        }

    }
  }

}
