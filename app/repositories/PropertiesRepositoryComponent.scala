package repositories

import models.database.{Property, Profile, PropertiesSchemaComponent}

trait PropertiesRepositoryComponent {
  val propertiesRepository: PropertiesRepository

  trait PropertiesRepository {
    def changeProperty(propertyName: String, value: String)

    def getProperty(propertyName: String): Property
  }
}

trait PropertiesRepositoryComponentImpl extends PropertiesRepositoryComponent {
  this: PropertiesSchemaComponent with Profile =>

  class PropertiesRepositoryImpl extends PropertiesRepository {
    def changeProperty(propertyName: String, value: String): Unit = {
    }

    def getProperty(propertyName: String): Property = {
      Property(null, null, null, null, null)
    }
  }
}
