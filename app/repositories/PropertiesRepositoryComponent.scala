package repositories

import models.database.{Property, Profile, PropertiesSchemaComponent}
import scala.slick.jdbc.JdbcBackend

trait PropertiesRepositoryComponent {
  val propertiesRepository: PropertiesRepository

  trait PropertiesRepository {
    def changeProperty(propertyName: String, value: Option[String])(implicit session: JdbcBackend#Session)

    def getProperty(propertyName: String)(implicit session: JdbcBackend#Session): Option[Property]
  }
}

trait PropertiesRepositoryComponentImpl extends PropertiesRepositoryComponent {
  this: PropertiesSchemaComponent with Profile =>
  val propertiesRepository = new PropertiesRepositoryImpl

  import profile.simple._

  val compiledByName = Compiled((name: Column[String]) => properties.filter(name === _.name))
  val updateValueCompiled = Compiled((name: Column[String]) => properties.filter(name === _.name).map(_.value))

  class PropertiesRepositoryImpl extends PropertiesRepository {
    def changeProperty(propertyName: String, value: Option[String])(implicit session: JdbcBackend#Session): Unit = {
      val editedProperty = getProperty(propertyName)
      editedProperty match {
        case Some(x) =>
          updateValueCompiled(propertyName).update(value)
        case None =>
      }
    }

    def getProperty(propertyName: String)(implicit session: JdbcBackend#Session): Option[Property] = {
      compiledByName(propertyName).firstOption
    }
  }
}
