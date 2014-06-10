package repositories

import models.database.{ApplicationProperty, Profile, ApplicationPropertiesSchemaComponent}
import scala.slick.jdbc.JdbcBackend
import utils.Implicits._
import org.joda.time.DateTime

trait ApplicationPropertiesRepositoryComponent {
  val propertiesRepository: ApplicationPropertiesRepository

  trait ApplicationPropertiesRepository {
    def updateProperty(propertyName: String, value: Option[String])(implicit session: JdbcBackend#Session)

    def createNewProperty(propertyName: String, value: Option[String])(implicit session: JdbcBackend#Session)

    def getProperty(propertyName: String)(implicit session: JdbcBackend#Session): Option[ApplicationProperty]
  }
}

trait ApplicationPropertiesRepositoryComponentImpl extends ApplicationPropertiesRepositoryComponent {
  this: ApplicationPropertiesSchemaComponent with Profile =>
  val propertiesRepository = new ApplicationPropertiesRepositoryImpl

  import profile.simple._

  class ApplicationPropertiesRepositoryImpl extends ApplicationPropertiesRepository {
    val compiledByName = Compiled((name: Column[String]) => properties.filter(name === _.name))
    val updateValueCompiled = Compiled((name: Column[String]) => properties.filter(name === _.name).map(_.value))
    val compiledForInsert = properties.insertInvoker

    def updateProperty(propertyName: String, value: Option[String])(implicit session: JdbcBackend#Session) {
      updateValueCompiled(propertyName).update(value)
    }

    def createNewProperty(propertyName: String, value: Option[String])(implicit session: JdbcBackend#Session) {
      val newProperty = ApplicationProperty(None, propertyName, value, value.getOrElse(""), DateTime.now)
      compiledForInsert.insert(newProperty)
    }

    def getProperty(propertyName: String)(implicit session: JdbcBackend#Session) =
      compiledByName(propertyName).firstOption
  }
}
