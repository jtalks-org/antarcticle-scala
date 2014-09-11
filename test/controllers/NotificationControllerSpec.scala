package controllers

import services.{ApplicationPropertiesServiceComponent, NotificationsServiceComponent}
import security.{Principal, Authorities}
import java.sql.Timestamp

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import util.FakeAuthentication
import org.specs2.specification.AfterExample
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scalaz._
import Scalaz._
import security.AuthenticatedUser
import scala.Some
import models.database.Notification

class NotificationControllerSpec extends Specification with Mockito with AfterExample {
  object controller extends NotificationsController
    with NotificationsServiceComponent
    with ApplicationPropertiesServiceComponent
    with FakeAuthentication
    with PropertiesProvider {
    override val notificationsService = mock[NotificationsService]
    override val usersRepository = mock[UsersRepository]
    override val propertiesService = mock[ApplicationPropertiesService]
  }

  import controller._

  def after = {
    org.mockito.Mockito.reset(usersRepository)
  }

  val currentUserId = 1
  val authenticatedUser = new AuthenticatedUser(currentUserId, "user", Authorities.User)

  val time = new Timestamp(System.currentTimeMillis)
  val notification = Notification(Some(1), 1, 2, Some(1), "", "", time)


  "get notifications" should {
    "return all exist notifications" in {
      val notifications = Seq(notification)
      notificationsService.getNotificationsForCurrentUser(any[Principal]) returns notifications

      val page = controller.getNotifications()(FakeRequest())

      status(page) must equalTo(200)
      contentType(page) must beSome("text/html")
      contentAsString(page).contains(notification.title) must beTrue
      contentAsString(page).contains(notification.content) must beTrue
    }
  }

  "dismiss notification" should {
    "show error when user isn't authenticated" in {
      notificationsService.deleteNotification(anyInt)(any[Principal]) returns "Not authenticated".failNel
      val notificationId = 1

      val page = controller.dismissNotification(notificationId)(FakeRequest())

      status(page) must equalTo(403)
    }

    "be success when user's authenticated" in {
      notificationsService.deleteNotification(anyInt)(any[Principal]) returns "".successNel
      val notificationId = 1

      val page = controller.dismissNotification(notificationId)(FakeRequest())

      status(page) must equalTo(200)
    }
  }

  "dismiss notifications" should {
    "show error when user isn't authenticated" in {
      notificationsService.deleteNotificationsForCurrentUser()(any[Principal]) returns "".failureNel

      val page = controller.dismissAllNotifications()(FakeRequest())

      status(page) must equalTo(403)
    }

    "be success when user's authenticated" in {
      notificationsService.deleteNotificationsForCurrentUser()(any[Principal]) returns "".successNel

      val page = controller.dismissAllNotifications()(FakeRequest())

      status(page) must equalTo(200)
    }
  }
}
