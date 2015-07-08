package conf

import models.database.Profile
import services.SlickSessionProvider
import scala.slick.driver._
import scala.slick.jdbc.JdbcBackend.Database
import play.api.Logger

trait DatabaseConfiguration extends Profile with SlickSessionProvider {
  this: PropertiesProviderComponent =>

  override val profile: JdbcProfile = propertiesProvider(Keys.DbDriver) match {
    case Some("org.postgresql.Driver") => PostgresDriver
    case Some("com.mysql.jdbc.Driver") => MySQLDriver
    case Some("org.h2.Driver") => H2Driver
    case d => throw new RuntimeException(s"Unsupported JDBC driver: $d")
  }

  override val db: Database = {
    (for {
      driver <- propertiesProvider(Keys.DbDriver)
      url <- propertiesProvider(Keys.DbUrl)
      user <- propertiesProvider(Keys.DbUser)
      password <- propertiesProvider(Keys.DbPassword)
    } yield {
      Logger.info(s"Using database: $url with driver: $driver")
      Database.forURL(
        url = url,
        user = user,
        password = password,
        driver = driver
      )
    }) getOrElse {
      throw new RuntimeException("Incorrect database configuration. " +
        "Some properties not found.")
    }
  }
}
