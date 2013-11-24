package org.jtalks.antarcticle.persistence

import scala.slick.session.Database

trait DatabaseProvider {
  val db: Database
  def close = {}
}
