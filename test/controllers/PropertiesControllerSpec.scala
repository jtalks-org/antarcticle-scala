package controllers

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.AfterExample
import services.PropertiesServiceComponent
import util.FakeAuthentication
import play.api.test._
import play.api.libs.json.Json


class PropertiesControllerSpec extends Specification with Mockito with AfterExample {

  object controller extends PropertiesController
  with PropertiesServiceComponent
  with FakeAuthentication {
    override val usersRepository = mock[UsersRepository]
    override val propertiesService = mock[PropertiesService]
  }

  import controller._

  def after: Any = {
    org.mockito.Mockito.reset(usersRepository)
    org.mockito.Mockito.reset(propertiesService)
  }


  "update instance name" should {

    "update it" in {
      val newInstanceName: String = "New Antarcticle"
      val request = new FakeRequest(Helpers.POST, "/", FakeHeaders(), Json.toJson(Map("instanceName" -> newInstanceName)))

      val page = controller.postChangedInstanceName()(request)

      there was one(propertiesService).changeInstanceName(newInstanceName)
    }
  }
}
