package org.jtalks.antarcticle.persistence

import scala.slick.driver.ExtendedProfile

/**
 * Database profile
 */
trait Profile {
  val profile: ExtendedProfile
}