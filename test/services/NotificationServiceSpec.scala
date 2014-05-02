package services

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import org.specs2.mock.Mockito
import org.specs2.specification.BeforeExample
import org.specs2.scalaz.ValidationMatchers
import repositories.NotificationsRepositoryComponent
import util.FakeSessionProvider

import models.database._
import scala.slick.driver.H2Driver
import java.sql.Timestamp
import security.{Authorities, AuthenticatedUser, AuthenticatedPrincipal}
import util.FakeSessionProvider._
import models.database.Notification


/**
 * @author Anuar_Nurmakanov
 */
class NotificationServiceSpec extends  Specification
  with NoTimeConversions with Mockito with BeforeExample
  with ValidationMatchers {

  object service extends NotificationsServiceComponentImpl
    with NotificationsRepositoryComponent
    with FakeSessionProvider {
    override val notificationsRepository = mock[NotificationsRepository]
  }

  import service._

  val time = new Timestamp(System.currentTimeMillis)

  val firstNotification = Notification(None, 1, 2, 1, "", "", time)
  val secondNotification = Notification(None, 1, 2, 1, "", "", time)

  override protected def before: Any = {
    org.mockito.Mockito.reset(notificationsRepository)
  }

  "get notifications" should {
    "return all notifications for current user" in {
      val currentUserId = 1
      val currentUser = new AuthenticatedUser(currentUserId,"user", Authorities.User)
      val expectedNotifications = Seq(firstNotification, secondNotification)
      notificationsRepository.getNotificationsForArticlesOf(currentUserId) (FakeSessionValue) returns expectedNotifications

      val foundNotifications = notificationsService.getNotificationForCurrentUser(currentUser)

      foundNotifications mustEqual expectedNotifications
    }
  }

  "get notification" should {
    "return it by id" in {
      val notificationId = 1
      val expectedNotification = Some(firstNotification)
      notificationsRepository.getNotification(notificationId) (FakeSessionValue) returns expectedNotification

      val foundNotification = notificationsService.getNotification(notificationId)

      foundNotification mustEqual expectedNotification
    }
  }
}
