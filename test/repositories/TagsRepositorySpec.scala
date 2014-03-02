package repositories

import org.specs2.mutable.Specification
import utils.Implicits._
import org.specs2.time.NoTimeConversions
import com.github.nscala_time.time.Imports._
import models.database._
import util.TestDatabaseConfigurationWithFixtures

class TagsRepositorySpec extends Specification with NoTimeConversions {
  object repository extends TestDatabaseConfigurationWithFixtures with Schema
    with TagsRepositoryComponentImpl

  import repository._
  import profile.simple._

  "get tags by names" should {
    "return existing tags" in withTestDb { implicit session =>
      val tags = tagsRepository.getByNames(List("tag1", "tag2"))

      tags(0).name must_== "tag1"
      tags(1).name must_== "tag2"
    }

    "return empty list when tags not found in db" in withTestDb { implicit session =>
      val tags = tagsRepository.getByNames(List("tag123", "tag2312"))

      tags must be empty
    }

    "return empty list when empty names list passed" in withTestDb { implicit session =>
      val tags = tagsRepository.getByNames(Vector())

      tags must be empty
    }
  }

  "get tag by name" should {
    "return existing tag" in withTestDb { implicit session =>
      val tag = tagsRepository.getByName(Some("tag1"))

      tag.get.name must_== "tag1"
      tag.get.id must_== 1
    }

    "handle None" in withTestDb { implicit session =>
      tagsRepository.getByName(None) must_== None
    }

    "handle nonexistent tag" in withTestDb { implicit session =>
      tagsRepository.getByName(Some("lolwut")) must_== None
    }
  }
}
