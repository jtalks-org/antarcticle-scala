package repositories

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import models.database._
import util.TestDatabaseConfigurationWithFixtures

class CommentsRepositorySpec extends Specification with NoTimeConversions {
  object repository extends TestDatabaseConfigurationWithFixtures with Schema
              with CommentsRepositoryComponentImpl


  "get comments by article" should {
    "return comments with author" in pending
    "return comments for correct article" in pending
    "return comments sorted by creation time" in pending
  }

  "insert comment" should {
    "return generated id" in pending
    "perform insertion to db" in pending
  }

  "update comment" should {
    "update existing comment" in pending
    "return false when comment not exists" in pending
  }

  "delete comment" should {
    "remove correct comment" in pending
    "return false when comment not found" in pending
  }

}
