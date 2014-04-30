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

  /**
   * Current description building strategy (cutoff after 300 chars) cannot guarantee proper markup preservation
   * for the description. Until better content shortening strategy is developed we're temporary setting
   * description equal to full content.
   */
  val setDescriptionToFullContent = new Migration {
    val version = 4

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.updateNA("UPDATE articles SET description = content").execute
    }
  }

  val addPasswordColumnToUsersTable = new Migration {
    val version = 5

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.updateNA("alter table users add password varchar(255)").execute
      Q.updateNA("update users set password=''").execute
    }
  }

  val addReadByArticleAuthorFlagForArticleComments = new Migration {
    val version = 6

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.updateNA("alter table comments add column read_by_article_author boolean not null default false").execute()
    }

  }

  val removeReadByArticleAuthorFlagForArticleComments = new Migration {
    val version = 7

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.updateNA("alter table comments drop column read_by_article_author").execute()
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

}