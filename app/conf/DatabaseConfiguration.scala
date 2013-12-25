package conf

import models.database.Profile
import services.SlickSessionProvider
import scala.slick.driver.{H2Driver, MySQLDriver, PostgresDriver, ExtendedProfile}
import scala.slick.session.Database
import javax.naming.NamingException

trait DatabaseConfiguration extends Profile with SlickSessionProvider {
  import play.api.Logger

  val DatabaseProperties(url, user, password, driver) = (new JndiDatabasePropertiesProvider).getProperties.recoverWith {
    case ex: NamingException =>
      Logger.info("JNDI configuration not found")
      DefaultProvider.getProperties
  }.get


  override val profile: ExtendedProfile = driver match {
    case "org.postgresql.Driver" => PostgresDriver
    case "com.mysql.jdbc.Driver" => MySQLDriver
    case "org.h2.Driver" => H2Driver
    case d => throw new RuntimeException(s"Unsupported JDBC driver: $d")
  }

  override val db: Database = scala.slick.session.Database.forURL(
    url = url,
    user = user,
    password = password,
    driver = driver)
}
