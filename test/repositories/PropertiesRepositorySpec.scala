package repositories


import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import models.database._
import util.TestDatabaseConfigurationWithFixtures

class PropertiesRepositorySpec extends Specification with NoTimeConversions {
  object repository extends TestDatabaseConfigurationWithFixtures with Schema with PropertiesRepositoryComponentImpl

  import repository._

  "get property" should {
    "return it by name" in withTestDb { implicit session =>
      val propertyName = "changed_property"
      val expectedPropertyId = Some(1)
      val expectedValue = "changed property for a test"
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

}
