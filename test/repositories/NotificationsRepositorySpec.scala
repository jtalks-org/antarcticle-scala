package repositories

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import util.TestDatabaseConfigurationWithFixtures
import models.database.Schema
import security.AuthenticatedUser
import security.Authorities.User

/**
 * @author Anuar_Nurmakanov
 */
class NotificationsRepositorySpec extends Specification with NoTimeConversions {
  object repository extends TestDatabaseConfigurationWithFixtures with Schema
  with NotificationsRepositoryComponentImpl

  import repository._
  import profile.simple._

  "get notifications by recipient id" should {
    "return all exist notifications" in withTestDb { implicit session =>
      val userId = 2

      val notifications = notificationsRepository.getNotificationsForUser(userId)

      notifications must have size 2
    }
  }

  "get notifications by recipient id" should {
    "return all notification content" in withTestDb{ implicit session =>
      val userId = 3
      val expectedArticleId = 2
      val expectedCommentId = 2
      val expectedTitle = "Have you checked a content of your notification?"
      val expectedContent = "Bullshit, do it now."

      val notifications = notificationsRepository.getNotificationsForUser(userId)

      notifications must have size 1
      val notification = notifications(0)
      notification.articleId must_== expectedArticleId
      notification.commentId must_== expectedCommentId
      notification.content must_== expectedContent
      notification.title must_== expectedTitle
    }
  }

  "get notifications by recipient id" should {
    "return nothing when recipient doesn't have notifications" in withTestDb { implicit session =>
      val userId = 4

      val notifications = notificationsRepository.getNotificationsForUser(userId)

      notifications must beEmpty
    }
  }

  "delete notification" should {
    "delete it in database" in withTestDb { implicit session =>
      val deletedNotificationId = 3
      val recipientId = 1

      notificationsRepository.deleteNotification(deletedNotificationId, recipientId)

      notificationsCount(recipientId) must_==  0
    }
  }

  "get notification" should {
    "return it by id" in withTestDb { implicit session =>
      val notificationId = 1
      val expectedArticleId = 2
      val expectedCommentId = 2
      val expectedTitle = "Be careful, it's JTalks, baby"
      val expectedContent = "Don't deny it, you met it."

      val notification = notificationsRepository.getNotification(notificationId)
      notification should beSome

      notification.get.articleId must_== expectedArticleId
      notification.get.commentId must_== expectedCommentId
      notification.get.content must_== expectedContent
      notification.get.title must_== expectedTitle
    }
  }

  def notificationsCount(recipientId: Int)(implicit session: Session) = notifications.filter(_.userId == recipientId).length.run
}
