package migrations

import models.ArticleModels.Language

import scala.slick.driver.JdbcProfile
import scala.slick.jdbc.{StaticQuery => Q, JdbcBackend}

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
   * Non-null timestamps in MySQL by default are assigned with 'on update CURRENT_TIMESTAMP',
   * we obviously don't need it for 'created_at' fields
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

  // migration 7 is just a rollback of migration 6, so we can safely drop both

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

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.updateNA("alter table users add salt varchar(64)").execute()
      Q.updateNA("alter table users convert to character set utf8 collate utf8_bin").execute()
    }
  }

  val addProperties = new Migration {
    val version: Int = 11

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.updateNA("CREATE TABLE IF NOT EXISTS properties ( " +
        " id int(11) NOT NULL AUTO_INCREMENT," +
        " name varchar(100) NOT NULL," +
        " value varchar(200) NOT NULL," +
        " default_value varchar(200) NOT NULL," +
        " created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
        " PRIMARY KEY(id)" +
        ");").execute()
    }
  }

  val addInstanceNameProperty = new Migration {
    val version: Int = 12

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.updateNA("INSERT INTO properties VALUES(1, 'INSTANCE_NAME', 'ANTARCTICLE', 'ANTARCTICLE', CURRENT_TIMESTAMP);").execute()
    }
  }

  val properValueTypeForApplicationProperties = new Migration {
    val version: Int = 13

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.updateNA("ALTER TABLE properties MODIFY value TEXT").execute
      Q.updateNA("ALTER TABLE properties MODIFY default_value TEXT").execute
    }
  }

  val addArticleLanguages = new Migration {
    val version: Int = 14

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.updateNA("ALTER TABLE articles ADD (language varchar(30), source_id int)").execute()
      Q.updateNA("UPDATE articles SET source_id = id").execute()
      Q.updateNA(s"UPDATE articles SET language = '${Language.Russian}'").execute()
    }
  }

  val mergeDuplicateUserRecords = new Migration {
    val version: Int = 15

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.queryNA[String]("SELECT username FROM users GROUP BY username HAVING COUNT(*) > 1") foreach { login =>
        val ids = Q.queryNA[String](s"SELECT id FROM users WHERE username='$login'").list()
        val masterId = ids.head
        println(s"Found ${ids.size} duplicate account records for user $login, merging...")
        ids.tail.foreach(duplicateId => {
          Q.updateNA(s"UPDATE articles SET author_id='$masterId' WHERE author_id='$duplicateId'").execute()
          Q.updateNA(s"UPDATE comments SET user_id='$masterId' WHERE user_id='$duplicateId'").execute()
          Q.updateNA(s"UPDATE notifications SET user_id='$masterId' WHERE user_id='$duplicateId'").execute()
          Q.updateNA(s"DELETE FROM users WHERE id='$duplicateId'").execute()
          println(s"Merged user id=$duplicateId into id=$masterId")
        })
        println(s"All duplicate records for user $login merged")
      }
    }
  }

  val mergeDuplicateTags = new Migration {
    val version: Int = 17

    def run(implicit session: JdbcBackend#Session): Unit = {
      Q.queryNA[String]("SELECT name FROM tags GROUP BY name HAVING COUNT(*) > 1") foreach { tag =>
        val ids = Q.queryNA[String](s"SELECT id FROM tags WHERE name='$tag'").list()
        val masterId = ids.head
        println(s"Found ${ids.tail.size} duplicates for tag '$tag', merging...")
        ids.tail.foreach(duplicateId => {
          Q.updateNA(s"DELETE FROM articles_tags WHERE tag_id='$duplicateId'").execute()
          Q.updateNA(s"DELETE FROM tags WHERE id='$duplicateId'").execute()
          println(s"Merged tag id=$duplicateId into id=$masterId")
        })
      }
      Q.updateNA("ALTER IGNORE TABLE articles_tags ADD UNIQUE INDEX idx_name (tag_id, article_id)")
      Q.updateNA(s"UPDATE tags SET name = lower(name)").execute()
    }
  }
}