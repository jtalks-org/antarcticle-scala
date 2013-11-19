package org.jtalks.antarcticle

import scala.slick.driver.H2Driver.simple._
import scala.slick.session.Database
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.matchers.ShouldMatchers


object T extends Table[(Option[Int], String)]("T") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def s = column[String]("s")
  def * = id.? ~ s
}

class Test  extends FunSuite with ShouldMatchers with BeforeAndAfter {
  val db = Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver")

  test("test") {
    running { implicit session: Session =>
        T.insert((None, "lol"))
      }
  }

  def running[T](block: => T): T = {
    db.withSession { implicit session: Session =>
      try {
        T.ddl.create
        block
      } finally {
        T.ddl.drop
      }
    }
  }
}
