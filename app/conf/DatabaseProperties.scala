package conf

import scala.util.Try

case class DatabaseProperties(url: String, user: String, password: String, driver: String)

trait DatabasePropertiesProvider {
  def getProperties: Try[DatabaseProperties]
}

class JndiDatabasePropertiesProvider extends DatabasePropertiesProvider {
  private lazy val jndi = new JndiProperties

  private def get(key: String) = Try(jndi[String](key))

  def getProperties = for {
    url <- get("ANTARCTICLE_DB_URL")
    user <- get("ANTARCTICLE_DB_USER")
    password <- get("ANTARCTICLE_DB_PASSWORD")
    driver <- get("ANTARCTICLE_DB_DRIVER")
  } yield DatabaseProperties(url, user, password, driver)
}

object DefaultProvider extends DatabasePropertiesProvider {
  def getProperties = Try(DatabaseProperties("jdbc:h2:mem:test1;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1",
    "sa", "", "org.h2.Driver"))
}