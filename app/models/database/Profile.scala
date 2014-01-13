package models.database

import scala.slick.driver.JdbcProfile

trait Profile {
  val profile: JdbcProfile
}
