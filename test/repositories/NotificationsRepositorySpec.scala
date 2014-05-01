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
}
