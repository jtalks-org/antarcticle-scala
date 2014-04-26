package migrations

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.{StaticQuery => Q, JdbcBackend, GetResult}

class Migrations(profile: JdbcProfile) extends MigrationsContainer {

  import profile.simple._

  val addRememberMeTokenToUser = new Migration {
    val version = 1

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.updateNA("alter table users add remember_token varchar(64)").execute
    }
  }

  val changeContentColumnTypes = new Migration {
    val version = 2

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.updateNA("alter table articles modify content longtext").execute
      Q.updateNA("alter table articles modify description longtext").execute
      Q.updateNA("alter table comments modify content longtext").execute
    }
  }

  /**
   *  Non-null timestamps in MySQL by default are assigned with 'on update CURRENT_TIMESTAMP',
   *  we obviously don't need it for 'created_at' fields
   */
  val removeDefaultOnUpdateConstraintFromTimestampField = new Migration {
    val version = 3

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.updateNA("alter table articles change created_at created_at timestamp not null default current_timestamp").execute
      Q.updateNA("alter table comments change created_at created_at timestamp not null default current_timestamp").execute
    }
  }

  val addPasswordColumnToUsersTable = new Migration {
    val version = 4

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.updateNA("alter table users add password varchar(255)").execute
      Q.updateNA("update users set password=''").execute
    }
  }
}