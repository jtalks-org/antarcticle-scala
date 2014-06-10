package controllers

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.AfterExample
import services.ApplicationPropertiesServiceComponent
import util.FakeAuthentication
import play.api.test._
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.FakeHeaders
import security.{Principal, AnonymousPrincipal, Authorities, AuthenticatedUser}
import security.Result.{Authorized, NotAuthorized}


class ApplicationPropertiesControllerSpec extends Specification with Mockito with AfterExample {

  object controller extends ApplicationPropertiesController
  with ApplicationPropertiesServiceComponent
  with FakeAuthentication {
    override val usersRepository = mock[UsersRepository]
    override val propertiesService = mock[ApplicationPropertiesService]
  }

  import controller._

  def after: Any = {
    org.mockito.Mockito.reset(usersRepository)
    org.mockito.Mockito.reset(propertiesService)
  }

  val currentUserId = 1
  val authenticatedUser = new AuthenticatedUser(currentUserId, "user", Authorities.User)
  val anonymousUser = AnonymousPrincipal


  "update instance name" should {

    val request = new FakeRequest(Helpers.POST, "/", FakeHeaders(), Json.toJson(Map("instanceName" -> "New Antarcticle")))
    val badRequest = new FakeRequest(Helpers.POST, "/", FakeHeaders(), Json.toJson(Map("value" -> "value")))

    "pass when incoming json is correct" in {
      propertiesService.changeInstanceName(anyString)(any[Principal]) returns Authorized(Unit)

      val page = controller.postChangedInstanceName()(request)

      status(page) must equalTo(200)
    }

    "show error for bad request" in {
      val page = controller.postChangedInstanceName()(badRequest)

      status(page) must equalTo(400)
    }

    "show error when user isn't admin" in {
      propertiesService.changeInstanceName(anyString)(any[Principal]) returns NotAuthorized()

      val page = controller.postChangedInstanceName()(request)

      status(page) must equalTo(401)
    }
  }
}
