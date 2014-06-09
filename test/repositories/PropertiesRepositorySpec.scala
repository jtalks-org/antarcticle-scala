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

  "update property" should {
    "update value in db" in withTestDb { implicit session =>
      val propertyName = "property_for_update"
      val newPropertyValue = Some("new property value")

      propertiesRepository.updateProperty(propertyName, newPropertyValue)

      val changedProperty = getProperty(propertyName)
      changedProperty should beSome
      changedProperty.get.value must_== newPropertyValue
    }
  }

  "create new property" should {
    "create new property in db" in withTestDb { implicit session =>
      val propertyName = "new property"
      val newPropertyValue = Some("new property value")

      propertiesRepository.createNewProperty(propertyName, newPropertyValue)

      val createdProperty = getProperty(propertyName)
      createdProperty should beSome
      createdProperty.get.value must_== newPropertyValue
    }
  }

  "get property" should {
    "return it by name" in withTestDb { implicit session =>
      val propertyName = "property"
      val expectedPropertyId = Some(1)
      val expectedValue = Some("property for a test")
      val expectedDefaultValue = "default value of property for a test"

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
