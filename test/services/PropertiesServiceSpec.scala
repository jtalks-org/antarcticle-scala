package services

import java.sql.Timestamp

import models.database.ApplicationProperty
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.scalaz.ValidationMatchers
import org.specs2.specification.BeforeEach
import repositories.ApplicationPropertiesRepositoryComponent
import security.Result.Authorized
import security.{AnonymousPrincipal, AuthenticatedUser, Authorities}
import util.FakeSessionProvider
import util.FakeSessionProvider._

class PropertiesServiceSpec extends Specification with Mockito with BeforeEach with ValidationMatchers{

  object service extends ApplicationPropertiesServiceComponentImpl with ApplicationPropertiesRepositoryComponent with FakeSessionProvider {
    val propertiesRepository = mock[ApplicationPropertiesRepository]
  }

  import service._

  def before: Any = {
    org.mockito.Mockito.reset(propertiesRepository)
  }

  val time = new Timestamp(System.currentTimeMillis)

  val currentUserId = 1
  val adminUser = new AuthenticatedUser(currentUserId, "admin", Authorities.Admin)
  val user =  new AuthenticatedUser(currentUserId, "admin", Authorities.User)
  val anonymousUser = AnonymousPrincipal

  "get instance name" should {
    "return instance name saved in database" in {
      val expectedInstanceName = "JTalks"
      val propertyName = "INSTANCE_NAME"
      val property = ApplicationProperty(Some(1), propertyName, Some(expectedInstanceName), "Default value", time)

      propertiesRepository.getProperty(propertyName) (FakeSessionValue) returns Some(property)

      val foundInstanceName = propertiesService.getInstanceName()

      foundInstanceName mustEqual expectedInstanceName
    }

    "return default instance name when it hasn't been set yet" in {
      val propertyName = "INSTANCE_NAME"
      val propertyDefaultValue = "ANTARCTICLE"
      val property = ApplicationProperty(Some(1), propertyName, None, propertyDefaultValue, time)

      propertiesRepository.getProperty(propertyName) (FakeSessionValue) returns Some(property)

      val foundInstanceName = propertiesService.getInstanceName()

      foundInstanceName mustEqual propertyDefaultValue
    }
  }

  "change instance name" should {
    "create new property when it doesn't exist yet" in {
      val propertyName = "INSTANCE_NAME"
      val newValue = "New Instance Name"
      propertiesRepository.getProperty(propertyName) (FakeSessionValue) returns None

      val result = propertiesService.writeProperty(ApplicationPropertyNames.instanceNameProperty, newValue)(adminUser)

      there was one(propertiesRepository).createNewProperty(propertyName, Some(newValue)) (FakeSessionValue)
      result match {
        case Authorized(_) => ok
        case _ => ko
      }
    }

    "update property when it's already exist" in {
      val propertyName = "INSTANCE_NAME"
      val newValue = "New Instance Name"
      val existProperty = ApplicationProperty(None, propertyName, Some("value"), "value", time)
      propertiesRepository.getProperty(propertyName) (FakeSessionValue) returns Some(existProperty)

      val result = propertiesService.writeProperty(ApplicationPropertyNames.instanceNameProperty, newValue)(adminUser)

      there was one(propertiesRepository).updateProperty(propertyName, Some(newValue)) (FakeSessionValue)
      result match {
        case Authorized(_) => ok
        case _ => ko
      }
    }

    "return not authorized when user isn't admin" in {
      val propertyName = "INSTANCE_NAME"
      val newValue = "New Instance Name"

      val result = propertiesService.writeProperty(ApplicationPropertyNames.instanceNameProperty, newValue)(user)

      there was no(propertiesRepository).updateProperty(propertyName, Some(newValue)) (FakeSessionValue)
      there was no(propertiesRepository).createNewProperty(propertyName, Some(newValue)) (FakeSessionValue)
      result match {
        case Authorized(_) => ko
        case _ => ok
      }
    }

    "return not authorized when user is anonymous" in {
      val propertyName = "INSTANCE_NAME"
      val newValue = "New Instance Name"

      val result = propertiesService.writeProperty(ApplicationPropertyNames.instanceNameProperty, newValue)(anonymousUser)

      there was no(propertiesRepository).updateProperty(propertyName, Some(newValue)) (FakeSessionValue)
      there was no(propertiesRepository).createNewProperty(propertyName, Some(newValue)) (FakeSessionValue)
      result match {
        case Authorized(_) => ko
        case _ => ok
      }
    }
  }
}
