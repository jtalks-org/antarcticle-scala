package org.jtalks.antarcticle

import org.scalatest.{fixture, Matchers}
import scala.slick.session.Database
import scala.slick.driver.{H2Driver, ExtendedProfile}
import org.jtalks.antarcticle.persistence.{Schema, Profile, DatabaseProvider}
import scala.slick.session

/**
 * Basic class for repositories tests.
 * Creates schema before each test and drops schema after.
 * Runs each test inside session.
 * Provides trait with configured database.
 */
trait RepositorySpec extends fixture.FunSpec with Matchers {
  val h2db: Database = Database.forURL("jdbc:h2:mem:test1", driver = "org.h2.Driver")
  val h2Profile: ExtendedProfile = H2Driver

  import h2Profile.simple._

  // test body function parameter type
  type FixtureParam = Session

  // wrap each test inside withDb
  override def withFixture(test: OneArgTest) = {
    withDb { implicit session: Session =>
      withFixture(test.toNoArgTest(session))
    }
  }

  // create schema and execute f inside session, and then drop schema
  def withDb[T](f: Session => T): T = {
    h2db withSession { implicit session: Session =>
      try {
        schema.schema.create
        f(session)
      } finally {
        schema.schema.drop
      }
    }
  }

  //TODO: try to get rid of it
  def schema: Schema

  // DatabaseProvider configured with test database
  trait TestDbProvider extends DatabaseProvider with Profile {
    val profile: ExtendedProfile = h2Profile
    val db: session.Database = h2db
  }
}
