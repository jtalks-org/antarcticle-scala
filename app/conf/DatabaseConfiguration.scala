package conf

import models.database.Profile
import services.SlickSessionProvider
import scala.slick.driver._
import scala.slick.jdbc.JdbcBackend.Database
import play.api.Logger

import scala.util.{Failure, Success, Try}

trait DatabaseConfiguration extends Profile with SlickSessionProvider {
  this: PropertiesProviderComponent =>

  override lazy val profile: JdbcProfile = propertiesProvider(Keys.DbDriver) match {
    case Some("org.postgresql.Driver") => PostgresDriver
    case Some("com.mysql.jdbc.Driver") => MySQLDriver
    case Some("org.h2.Driver") => H2Driver
    case d => throw new RuntimeException(s"Unsupported JDBC driver: $d")
  }

  override val db: Database = {
    val driver = propertiesProvider(Keys.DbDriver).getOrElse(throw new RuntimeException("Database driver is not set"))
    val url = propertiesProvider(Keys.DbUrl).getOrElse(throw new RuntimeException("Database url is not set"))
    val user = propertiesProvider(Keys.DbUser).getOrElse(throw new RuntimeException("Database user is not set"))
    val password = propertiesProvider(Keys.DbPassword).getOrElse{
      Logger.warn("Database password is not set. Empty password is used"); ""
    }

    Logger.info(s"Using database: $url with driver: $driver")
    Try {
      Database.forURL(
        url = url,
        user = user,
        password = password,
        driver = driver
      )
    } match {
      case Success(d) => d
      case Failure(e) =>
        Logger.error("Could not connect to database", e)
        throw e
    }
  }
}
