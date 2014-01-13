package util

import models.database.Profile
import services.SlickSessionProvider
import scala.slick.driver.{H2Driver, JdbcProfile}
import scala.slick.jdbc.JdbcBackend.Database

trait TestDatabaseConfiguration extends Profile with SlickSessionProvider {
  def driverInstance = Class.forName("org.h2.Driver").newInstance.asInstanceOf[java.sql.Driver]

  override val profile: JdbcProfile = H2Driver
  override val db: Database = Database.forDriver(driver = driverInstance, url = "jdbc:h2:mem:test1;DATABASE_TO_UPPER=false")
}
