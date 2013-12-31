package migrations

import scala.slick.driver.ExtendedProfile
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import scala.slick.session.Session

class Migrations(profile: ExtendedProfile) extends MigrationsContainer {
  import profile.simple._

  // ---------EXAMPLE:
  // val MigrateV1toV2 = new Migration {
  //   val version: Int = 2

  //   def run(implicit session: Session): Unit = {
  //     Q.updateNA("create table test (id int not null)").execute
  //   }
  // }
}
