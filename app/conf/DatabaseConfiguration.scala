package conf

import models.database.Profile
import services.SlickSessionProvider
import scala.slick.driver._
import scala.slick.jdbc.JdbcBackend.Database
import play.api.Logger

trait DatabaseConfiguration extends Profile with SlickSessionProvider {
  this: PropertiesProviderComponent =>

  private def driver = propertiesProvider("ANTARCTICLE_DB_DRIVER") getOrElse "org.h2.Driver"

  //lazy to preserve correct intialization order
  override lazy val profile: JdbcProfile = driver match {
    case "org.postgresql.Driver" => PostgresDriver
    case "com.mysql.jdbc.Driver" => MySQLDriver
    case "org.h2.Driver" => H2Driver
    case d => throw new RuntimeException(s"Unsupported JDBC driver: $d")
  }

  override val db: Database = {
    val url = propertiesProvider("ANTARCTICLE_DB_URL")
      .getOrElse("jdbc:h2:mem:test1;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1")

    Logger.info(s"Using database: $url with driver: $driver")

    Database.forURL(
      url = url,
      user = propertiesProvider("ANTARCTICLE_DB_USER") getOrElse "sa",
      password = propertiesProvider("ANTARCTICLE_DB_PASSWORD") getOrElse "",
      driver = driver
    )
  }
}
