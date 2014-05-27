package repositories


import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import models.database._
import util.TestDatabaseConfigurationWithFixtures
import scala.Some

class PropertiesRepositorySpec extends Specification with NoTimeConversions {
  object repository extends TestDatabaseConfigurationWithFixtures with Schema with PropertiesRepositoryComponentImpl

  import repository._
  import profile.simple._

  "change property" should {
    "set value when it hasn't already set" in withTestDb { implicit session =>
      val propertyName = "not_changed_property"
      val propertyValue = Some("new property value")

      propertiesRepository.changeProperty(propertyName, propertyValue)

      val changedProperty = getProperty(propertyName)
      changedProperty should beSome
      changedProperty.get.value must_== propertyValue
    }

    "set new value when it's already defined" in withTestDb { implicit session =>
      val propertyName = "already_changed_property"
      val newPropertyValue = Some("new property value of already set property")

      propertiesRepository.changeProperty(propertyName, newPropertyValue)

      val changedProperty = getProperty(propertyName)
      changedProperty should beSome
      changedProperty.get.value must_== newPropertyValue
    }
  }

  "get property" should {
    "return it by name" in withTestDb { implicit session =>
      val propertyName = "changed_property"
      val expectedPropertyId = Some(1)
      val expectedValue = Some("changed property for a test")
      val expectedDefaultValue = "default value of changed property for a test"

      val foundProperty = propertiesRepository.getProperty(propertyName)

      foundProperty should beSome
      foundProperty.get.id must_== expectedPropertyId
      foundProperty.get.value must_== expectedValue
      foundProperty.get.defaultValue must_== expectedDefaultValue
    }

    "return nothing when property doesn't exist" in withTestDb { implicit session =>
      val propertyName = "non_exist_property"

      val foundProperty = propertiesRepository.getProperty(propertyName)

      foundProperty should beNone
    }
  }

  def getProperty(name: String)(implicit session: Session) = properties.filter(_.name === name).firstOption
}
