package repositories

import org.specs2.mutable.Specification
import utils.DateImplicits._
import org.specs2.time.NoTimeConversions
import com.github.nscala_time.time.Imports._
import models.database.{ArticleToInsert, UserToInsert, Schema}
import util.TestDatabaseConfiguration
import migrations.{MigrationTool, MigrationsContainer}
import org.specs2.specification.BeforeExample

class TagsRepositorySpec extends Specification with NoTimeConversions {
  object repository extends TagsRepositoryComponentImpl with MigrationTool
      with Schema with TestDatabaseConfiguration  {
    val migrationsContainer = new MigrationsContainer {}
  }

  import repository._
  import profile.simple._
  import scala.slick.jdbc.JdbcBackend.Session

  //TODO: get rid of this copypaste
  def populateDb(implicit session: Session) = {
    migrate

    val time = DateTime.now
    tags.map(_.name).insertAll("tag1", "tag2", "tag3")
    users.map(_.username).insertAll("user1", "user2")
    articles.map(a => (a.title, a.content, a.createdAt, a.updatedAt, a.description, a.authorId))
      .insertAll(
        ("New title 1", "<b>content</b>", time + 1.day, time, "description1", 1),
        ("New title 2", "<i>html text</i>", time, time, "description2", 2),
        ("New title 3", "<i>html text</i>", time + 2.days, time, "description3", 2),
        ("New title 4", "<i>html text</i>", time + 4.days, time, "description4", 2)
      )
    articlesTags.map(at => (at.articleId, at.tagId)).insertAll(
      (1,1), (1,2), (2,1), (2,3)
    )
  }

  def withTestDb[T](f: Session => T) = withSession { implicit s: Session =>
    populateDb
    f(s)
  }

  "get tags by names" should {
    "return existing tags" in withTestDb { implicit session: Session =>
      val tags = tagsRepository.getByNames(List("tag1", "tag2"))

      tags(0).name must_== "tag1"
      tags(1).name must_== "tag2"
    }

    "return empty list when tags not found in db" in withTestDb { implicit session: Session =>
      val tags = tagsRepository.getByNames(List("tag123", "tag2312"))

      tags must be empty
    }

    "return empty list when empty names list passed" in withTestDb { implicit session: Session =>
      val tags = tagsRepository.getByNames(Vector())

      tags must be empty
    }
  }

}
