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

// temporary
object DefaultProvider extends DatabasePropertiesProvider {
  def getProperties = Try(DatabaseProperties("jdbc:postgresql:antarcticle",
    "postgres", "postgres", "org.postgresql.Driver"))
}