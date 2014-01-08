package repositories

import org.specs2.mutable.Specification
import utils.DateImplicits._
import org.specs2.time.NoTimeConversions
import com.github.nscala_time.time.Imports._
import models.database.{ArticleToInsert, UserToInsert, Schema}
import util.TestDatabaseConfiguration
import migrations.{MigrationTool, MigrationsContainer}

class TagsRepositorySpec extends Specification with NoTimeConversions {
  object repository extends TagsRepositoryComponentImpl with MigrationTool with Schema with TestDatabaseConfiguration {
    val migrationsContainer = new MigrationsContainer {}
  }


  import repository._
  import profile.simple._

  //TODO: get rid of this copypaste
  def populateDb(implicit session: Session) = {
    migrate

    val time = DateTime.now
    val users = List(UserToInsert("user1"), UserToInsert("user2"))
    val articles = List(
      ArticleToInsert("New title 1", "<b>content</b>", time + 1.day, time, "description1", 1),
      ArticleToInsert("New title 2", "<i>html text</i>", time, time, "description2", 2),
      ArticleToInsert("New title 3", "<i>html text</i>", time + 2.days, time, "description3", 2),
      ArticleToInsert("New title 4", "<i>html text</i>", time + 4.days, time, "description4", 2)
    )
    val tags = List("tag1", "tag2", "tag3")
    val articlesTags = List((1,1), (1,2), (2,1), (2,3))

    Tags.forInsert.insertAll(tags : _*)
    Users.forInsert.insertAll(users : _*)
    val articlesIds = Articles.forInsert.insertAll(articles: _*)
    ArticlesTags.insertAll(articlesTags : _*)
    (users, articles, articlesIds)
  }

  "get tags by names" should {
    "return existing tags" in withSession { implicit session: Session =>
      populateDb

      val tags = tagsRepository.getByNames(List("tag1", "tag2"))

      tags(0).name must_== "tag1"
      tags(1).name must_== "tag2"
    }

    "return empty list when tags not found in db" in withSession { implicit session: Session =>
      populateDb

      val tags = tagsRepository.getByNames(List("tag123", "tag2312"))

      tags must be empty
    }

    "return empty list when empty names list passed" in withSession { implicit session: Session =>
      populateDb

      val tags = tagsRepository.getByNames(Vector())

      tags must be empty
    }
  }

}
