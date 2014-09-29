package services

import utils.Implicits._
import org.joda.time.DateTime
import repositories.{ArticlesRepositoryComponent, NotificationsRepositoryComponent}
import scala.slick.jdbc.JdbcBackend
import security.{Principal, AuthenticatedUser}
import models.database.{CommentRecord, Notification}
import scalaz._
import Scalaz._
import models.ArticleModels.ArticleDetailsModel


/**
 *
 */
trait NotificationsServiceComponent {
  val notificationsService: NotificationsService

  trait NotificationsService {
    def createNotificationForArticleTranslation(translation: ArticleDetailsModel)(implicit principal: Principal, session: JdbcBackend#Session)

    def createNotification(cr: CommentRecord)(implicit principal: Principal, session: JdbcBackend#Session)

    def getNotificationsForCurrentUser(implicit principal: Principal): Seq[Notification]

    def getAndDeleteNotification(id: Int)(implicit principal: Principal): ValidationNel[String, Option[Notification]]

    def deleteNotification(notificationId: Int)(implicit principal: Principal): ValidationNel[String, String]

    def deleteNotificationsForCurrentUser()(implicit principal: Principal): ValidationNel[String, String]
  }

}

trait NotificationsServiceComponentImpl extends NotificationsServiceComponent {
  this: SessionProvider with NotificationsRepositoryComponent with ArticlesRepositoryComponent =>

  val notificationsService = new NotificationsServiceImpl

  class NotificationsServiceImpl extends NotificationsService {

    def createNotificationForArticleTranslation(translation: ArticleDetailsModel)(implicit principal: Principal, session: JdbcBackend#Session) {
      principal match {
        case user: AuthenticatedUser =>
          for {
            (article,_,_) <- articlesRepository.get(translation.sourceId)
            notification <- if (translation.author.id != article.authorId) {
              Some(new Notification(None, article.authorId, translation.id, None, translation.title,
                translation.content.take(150), DateTime.now)
              )
            } else None
          } yield notificationsRepository.insertNotification(notification)
      }
    }

    def createNotification(cr: CommentRecord)(implicit principal: Principal, session: JdbcBackend#Session) {
      principal match {
        case user: AuthenticatedUser =>
          val article = articlesRepository.get(cr.articleId).get._1
          if (cr.userId != article.authorId)
            notificationsRepository.insertNotification(
              new Notification(None, article.authorId, cr.articleId, cr.id, article.title,
                cr.content.take(150), cr.createdAt))
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
