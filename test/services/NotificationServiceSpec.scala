package services

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import org.specs2.mock.Mockito
import org.specs2.specification.BeforeExample
import org.specs2.scalaz.ValidationMatchers
import repositories.NotificationsRepositoryComponent
import util.FakeSessionProvider

import java.sql.Timestamp
import security.{Authorities, AuthenticatedUser}
import util.FakeSessionProvider._
import models.database.Notification


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
  val currentUserId = 1
  val currentUser = new AuthenticatedUser(currentUserId,"user", Authorities.User)

  def before: Any = {
    org.mockito.Mockito.reset(notificationsRepository)
  }

  "get notifications" should {
    "return all notifications for current user" in {

      val expectedNotifications = Seq(firstNotification, secondNotification)
      notificationsRepository.getNotificationsForUser(currentUserId) (FakeSessionValue) returns expectedNotifications

      val foundNotifications = notificationsService.getNotificationsForCurrentUser(currentUser)

      foundNotifications mustEqual expectedNotifications
    }
  }

  "get notification" should {
    "return it by id" in {
      val notificationId = 1
      val expectedNotification = Some(firstNotification)
      notificationsRepository.getNotification(notificationId) (FakeSessionValue) returns expectedNotification

      val foundNotification = notificationsService.getAndDeleteNotification(notificationId)(currentUser)

      foundNotification mustEqual expectedNotification
    }
  }

  "dismiss notification" should {
    "delete it when it exists" in {
      val notificationId = 1
      val expectedNotification = Some(firstNotification)
      notificationsRepository.getNotification(notificationId) (FakeSessionValue) returns expectedNotification

      notificationsService.deleteNotification(notificationId)(currentUser)

      there was one(notificationsRepository).deleteNotification(notificationId, currentUserId)(FakeSessionValue)
    }

    "be silent when notification doesn't exist" in {
      val notificationId = 1
      notificationsRepository.getNotification(notificationId) (FakeSessionValue) returns None

      notificationsService.deleteNotification(notificationId)(currentUser)

      there was one(notificationsRepository).deleteNotification(notificationId, currentUserId)(FakeSessionValue)
    }
  }
}
