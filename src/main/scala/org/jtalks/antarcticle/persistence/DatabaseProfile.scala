package org.jtalks.antarcticle.persistence

import scala.slick.driver.ExtendedProfile
import scala.slick.session.Database

/**
 * Database profile
 */
trait DatabaseProfile {
  val profile: ExtendedProfile
  val db: Database

  def close = {}
}