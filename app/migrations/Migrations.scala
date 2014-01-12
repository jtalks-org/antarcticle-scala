package migrations

import scala.slick.driver.ExtendedProfile
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import scala.slick.session.Session

class Migrations(profile: ExtendedProfile) extends MigrationsContainer {

  import profile.simple._

  val addRememberMeTokenToUser = new Migration {
    val version = 1

    def run(implicit session: Session): Unit = {
      Q.updateNA("alter table users add remember_token varchar(64)").execute
    }
  }

  val changeContentColumnTypes = new Migration {
    val version = 2

    def run(implicit session: Session): Unit = {
      Q.updateNA("alter table articles modify content longtext").execute
      Q.updateNA("alter table articles modify description longtext").execute
      Q.updateNA("alter table comments modify content longtext").execute
    }
  }
}
