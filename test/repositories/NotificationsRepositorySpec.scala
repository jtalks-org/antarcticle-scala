package repositories

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import util.TestDatabaseConfigurationWithFixtures
import models.database.Schema

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

      val notifications = notificationsRepository.getNotificationsForArticlesOf(userId)

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

      val notifications = notificationsRepository.getNotificationsForArticlesOf(userId)

      notifications must have size 1
      val notification = notifications(0)
      notification.articleId must_== expectedArticleId
      notification.commentId must_== expectedCommentId
      notification.content must_== expectedContent
      notification.title must_== expectedTitle
    }
  }
}
