package services

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import org.specs2.mock.Mockito
import org.specs2.specification.BeforeExample
import org.specs2.scalaz.ValidationMatchers
import util.FakeSessionProvider
import repositories.PropertiesRepositoryComponent
import util.FakeSessionProvider._
import models.database.Property
import java.sql.Timestamp
import security.{AnonymousPrincipal, Authorities, AuthenticatedUser}

class PropertiesServiceSpec extends Specification
  with NoTimeConversions with Mockito with BeforeExample with ValidationMatchers{

  object service extends PropertiesServiceComponentImpl with PropertiesRepositoryComponent with FakeSessionProvider {
    val propertiesRepository = mock[PropertiesRepository]
  }

  import service._

  def before: Any = {
    org.mockito.Mockito.reset(propertiesRepository)
  }

  val time = new Timestamp(System.currentTimeMillis)

  val currentUserId = 1
  val authenticatedUser = new AuthenticatedUser(currentUserId, "user", Authorities.User)
  val anonymousUser = AnonymousPrincipal

  "get instance name" should {
    "return instance name saved in database" in {
      val expectedInstanceName = "JTalks"
      val propertyName = "INSTANCE_NAME"
      val property = Property(Some(1), propertyName, Some(expectedInstanceName), "Default value", time)

      propertiesRepository.getProperty(propertyName) (FakeSessionValue) returns Some(property)

      val foundInstanceName = propertiesService.getInstanceName()

      foundInstanceName mustEqual expectedInstanceName
    }

    "return default instance name when it hasn't been set yet" in {
      val propertyName = "INSTANCE_NAME"
      val propertyDefaultValue = "ANTARCTICLE"
      val property = Property(Some(1), propertyName, None, propertyDefaultValue, time)

      propertiesRepository.getProperty(propertyName) (FakeSessionValue) returns Some(property)

      val foundInstanceName = propertiesService.getInstanceName()

      foundInstanceName mustEqual propertyDefaultValue
    }
  }

  "change instance name" should {
    "save new value in database when user is authenticated" in {
      val propertyName = "INSTANCE_NAME"
      val newValue = "New Instance Name"

      val result = propertiesService.changeInstanceName(newValue)(authenticatedUser)

      result must beSuccessful
      there was one(propertiesRepository).changeProperty(propertyName, Some(newValue)) (FakeSessionValue)
    }

    "return validation error when user isn't authenticated" in {
      val propertyName = "INSTANCE_NAME"
      val newValue = "New Instance Name"

      val result = propertiesService.changeInstanceName(newValue)(anonymousUser)

      result must beFailing
      there was no(propertiesRepository).changeProperty(propertyName, Some(newValue)) (FakeSessionValue)
    }
  }
}