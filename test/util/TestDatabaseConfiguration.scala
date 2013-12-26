package util

import models.database.Profile
import services.SlickSessionProvider
import scala.slick.session.Database
import scala.slick.driver.{H2Driver, ExtendedProfile}

trait TestDatabaseConfiguration extends Profile with SlickSessionProvider {
  def driverInstance = Class.forName("org.h2.Driver").newInstance.asInstanceOf[java.sql.Driver]

  override val profile: ExtendedProfile = H2Driver
  override val db: Database = Database.forDriver(driver = driverInstance, url = "jdbc:h2:mem:test1")
}
