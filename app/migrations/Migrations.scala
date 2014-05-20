package migrations

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.{StaticQuery => Q, JdbcBackend, GetResult}

/**
 * This class contains all database schema migrations, which are applied to the database
 * on software upgrade only. On new installations database is created in up-to-date state
 * and migrations are NOT performed.
 *
 * Under no conditions should version numbers be changed, as these numbers are also present
 * in every database, indicating it's version. It is, however, more or less safe to drop old
 * migrations if upgrades from really old versions are not required.
 */
class Migrations(profile: JdbcProfile) extends MigrationsContainer {

  import profile.simple._

  val addPasswordColumnToUsersTable = new Migration {
    val version = 5

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.updateNA("alter table users add password varchar(255)").execute
      Q.updateNA("update users set password=''").execute
    }
  }

  val addCommentsNotifications = new Migration {
    val version = 8

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.updateNA("CREATE TABLE IF NOT EXISTS notifications ( " +
        " id int(11) NOT NULL AUTO_INCREMENT," +
        " user_id int(11) NOT NULL," +
        " article_id int(11) NOT NULL," +
        " comment_id int(11) NOT NULL," +
        " title varchar(90) NOT NULL," +
        " content text NOT NULL," +
        " created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        " PRIMARY KEY(id)," +
        " CONSTRAINT notification_user_fk FOREIGN KEY (user_id) REFERENCES users (id)," +
        " CONSTRAINT notification_article_fk FOREIGN KEY (article_id) REFERENCES articles (id)," +
        " CONSTRAINT notification_comment_fk FOREIGN KEY (comment_id) REFERENCES comments (id)" +
        ");").execute()
    }
  }

  val cascadesForNotifications = new Migration {
    val version = 9

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.updateNA("ALTER TABLE notifications DROP FOREIGN KEY notification_user_fk").execute()
      Q.updateNA("ALTER TABLE notifications DROP FOREIGN KEY notification_article_fk").execute()
      Q.updateNA("ALTER TABLE notifications DROP FOREIGN KEY notification_comment_fk").execute()
      Q.updateNA("ALTER TABLE notifications ADD CONSTRAINT notification_user_fk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE").execute()
      Q.updateNA("ALTER TABLE notifications ADD CONSTRAINT notification_article_fk FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE").execute()
      Q.updateNA("ALTER TABLE notifications ADD CONSTRAINT notification_comment_fk FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE").execute()
    }
  }

  val addSaltColumnForUsers = new Migration {
    val version = 10

    def run(implicit  session: JdbcBackend#Session): Unit = {
      Q.updateNA("alter table users add salt varchar(64)").execute()
      Q.updateNA("alter table users convert to character set utf8 collate utf8_bin").execute()
    }
  }

}