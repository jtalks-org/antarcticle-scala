package repositories

import org.specs2.mutable.Specification
import utils.Implicits._
import org.specs2.time.NoTimeConversions
import com.github.nscala_time.time.Imports._
import models.database._
import util.TestDatabaseConfigurationWithFixtures
import scala.slick.jdbc.JdbcBackend

class TagsRepositorySpec extends Specification with NoTimeConversions {

  object repository extends TestDatabaseConfigurationWithFixtures with Schema
  with TagsRepositoryComponentImpl {

    import profile.simple._

    override def fixtures(implicit session: JdbcBackend#Session) = {
      val time = DateTime.now

      tags.map(_.name) ++= Seq("tag1", "tag2", "tag3")

      users ++= Seq(UserRecord(None, "user1", "fakePassword", "mail01@mail.zzz"))

      articles.map(a => (a.title, a.content, a.createdAt, a.updatedAt, a.description, a.authorId)) ++= Seq(
        ("New title 1", "<b>content</b>", time + 1.day, time, "description1", 1),
        ("New title 2", "<i>html text</i>", time, time, "description2", 1)
      )

      articlesTags ++= Seq((1, 1), (1, 2), (2, 1), (2, 3))
    }
  }

  import repository._
  import profile.simple._

  "get all tags" should {
    "return all available tags" in withTestDb {
      implicit session =>
        val tags = tagsRepository.getAllTags()
        tags.map(_.name)forall Seq("tag1", "tag2", "tag3").contains must beTrue
    }
  }

  "get tags by names" should {
    "return existing tags" in withTestDb {
      implicit session =>
        val tags = tagsRepository.getByNames(List("tag1", "tag2"))

        tags(0).name must_== "tag1"
        tags(1).name must_== "tag2"
    }

    "return empty list when tags not found in db" in withTestDb {
      implicit session =>
        val tags = tagsRepository.getByNames(List("tag123", "tag2312"))

        tags must be empty
    }

    "return empty list when empty names list passed" in withTestDb {
      implicit session =>
        val tags = tagsRepository.getByNames(List())

        tags must be empty
    }

    "perform correct escaping for terminal symbols" in withTestDb {
      implicit session =>
        val tags = tagsRepository.getByNames(List("'`||\\//\"^%$~"))

        tags must be empty
    }
  }

  "get tag by name" should {
    "return existing tag" in withTestDb {
      implicit session =>
        val tag = tagsRepository.getByName(Some("tag1"))

        tag.get.name must_== "tag1"
        tag.get.id must_== 1
    }

    "handle None" in withTestDb {
      implicit session =>
        tagsRepository.getByName(None) must_== None
    }

    "handle nonexistent tag" in withTestDb {
      implicit session =>
        tagsRepository.getByName(Some("lolwut")) must_== None
    }
  }

  "remove article tags" should {
    "delete all article-tags references for a given article" in withTestDb {
      implicit session =>
        tagsRepository.removeArticleTags(1)

        val records = articlesTags.filter(_.articleId === 1).length.run
        records must_== 0
    }

    "ignore other article-tags" in withTestDb {
      implicit session =>
        tagsRepository.removeArticleTags(1)

        val records = articlesTags.filter(_.articleId === 2).length.run
        records must_== 2
    }

    "not remove tags themselves" in withTestDb {
      implicit session =>
        tagsRepository.removeArticleTags(1)

        val records = tags.length.run
        records must_== 3
    }
  }

  "insert tags" should {
    "insert given tags" in withTestDb {
      implicit session =>
        tagsRepository.insertTags(Seq("tag4"))

        val records = tags.length.run
        records must_== 4
    }

    "omit duplicates when inserting tags, case insensitive" in withTestDb {
      implicit session =>
        tagsRepository.insertTags(Seq("tag4", "tag4", "Tag4"))

        val records = tags.length.run
        records must_== 4
    }
  }

  "insert article-tags" should {
    "insert given article-tags" in withTestDb {
      implicit session =>
        tagsRepository.insertArticleTags(Seq((2, 2)))

        val records = articlesTags.length.run
        records must_== 5
    }

    "omit duplicates when inserting article-tags" in withTestDb {
      implicit session =>
        tagsRepository.insertArticleTags(Seq((2, 2), (2, 2)))

        val records = articlesTags.length.run
        records must_== 5
    }
  }
}
