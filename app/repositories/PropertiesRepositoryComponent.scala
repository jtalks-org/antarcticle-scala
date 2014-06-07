package repositories

import models.database.{Property, Profile, PropertiesSchemaComponent}
import scala.slick.jdbc.JdbcBackend
import utils.Implicits._
import org.joda.time.DateTime

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

  class PropertiesRepositoryImpl extends PropertiesRepository {
    val compiledByName = Compiled((name: Column[String]) => properties.filter(name === _.name))
    val updateValueCompiled = Compiled((name: Column[String]) => properties.filter(name === _.name).map(_.value))
    val compiledForInsert = properties.insertInvoker

    def changeProperty(propertyName: String, value: Option[String])(implicit session: JdbcBackend#Session) {
      val editedProperty = getProperty(propertyName)
      editedProperty match {
        case Some(x) =>
          updateValueCompiled(propertyName).update(value)
        case None => {
          val newProperty = Property(None, propertyName, value, value.getOrElse(""), DateTime.now)
          compiledForInsert.insert(newProperty)
        }
      }
    }

    def getProperty(propertyName: String)(implicit session: JdbcBackend#Session) =
      compiledByName(propertyName).firstOption
  }
}
