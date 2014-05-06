package services

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import org.specs2.mock.Mockito
import org.specs2.specification.BeforeExample
import org.specs2.scalaz.ValidationMatchers
import repositories.NotificationsRepositoryComponent
import util.FakeSessionProvider

import java.sql.Timestamp
import security.{AnonymousPrincipal, Authorities, AuthenticatedUser}
import util.FakeSessionProvider._
import models.database.Notification
import security.Result.{Authorized, AuthorizationResult}


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
  val currentUser = new AuthenticatedUser(currentUserId, "user", Authorities.User)
  val anonymousUser = AnonymousPrincipal

  def before: Any = {
    org.mockito.Mockito.reset(notificationsRepository)
  }

  "get notifications" should {
    "return all notifications for authenticated user" in {

      val expectedNotifications = Seq(firstNotification, secondNotification)
      notificationsRepository.getNotificationsForUser(currentUserId) (FakeSessionValue) returns expectedNotifications

      val foundNotifications = notificationsService.getNotificationsForCurrentUser(currentUser)

      foundNotifications mustEqual expectedNotifications
    }

    "return nothing for not authenticated user" in {
      val foundNotifications = notificationsService.getNotificationsForCurrentUser(anonymousUser)

      foundNotifications must beEmpty
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

      val authResult = notificationsService.deleteNotification(notificationId)(currentUser)

      authResult must beSuccessful
      there was one(notificationsRepository).deleteNotification(notificationId, currentUserId)(FakeSessionValue)
    }

    "be silent when notification doesn't exist" in {
      val notificationId = 1
      notificationsRepository.getNotification(notificationId) (FakeSessionValue) returns None

      val authResult = notificationsService.deleteNotification(notificationId)(currentUser)

      authResult must beFailing
    }
  }
}
