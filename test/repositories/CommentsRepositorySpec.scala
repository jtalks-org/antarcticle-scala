package repositories

import org.specs2.mutable.Specification
import org.specs2.time.NoTimeConversions
import models.database._
import util.TestDatabaseConfigurationWithFixtures
import com.github.nscala_time.time.Imports._
import utils.Implicits._
import scala.Some
import models.database.CommentToUpdate
import scala.Some
import models.database.CommentRecord
import models.database.UserRecord

class CommentsRepositorySpec extends Specification with NoTimeConversions {
  object repository extends TestDatabaseConfigurationWithFixtures with Schema
              with CommentsRepositoryComponentImpl

  import repository._
  import profile.simple._

  class SortedMatcher(s:Seq[Long]) {
    def isSorted = s.view.zip(s.tail).forall(x => x._1 <= x._2)
  }

  implicit def seqToOrderMatcher(s:Seq[Long]) = new SortedMatcher(s)

  def haveArticleWithId(articleId:Int):((CommentRecord, UserRecord)) => Boolean = {
    import scalaz._
    import Scalaz._

    record => {
      Option(record._1) match {
        case s:Some[CommentRecord] => s.cata(some = comment => comment.articleId == articleId, none = false)
        case _ => false
      }
    }
  }

  "get comments by article" should {

    "return comments with author" in withTestDb { implicit session =>
      val comments = commentsRepository.getByArticle(1)

      forall(comments)(c => Option(c._2).isDefined)
    }

    "return comments for correct article" in withTestDb { implicit session =>
      val comments = commentsRepository.getByArticle(1)

      forall(comments)(haveArticleWithId(1))
    }

    "return comments sorted by creation time" in withTestDb { implicit session =>
      val comments = commentsRepository.getByArticle(1)

      comments.map(record => record._1.createdAt.getTime).isSorted must beTrue
    }
  }

  "insert comment" should {

    val time = DateTime.now
    val articleId = 1
    val userId = 1
    val comment = CommentRecord(None, userId, articleId, "This is the awesome article", time + 1.hour, None)

    "return generated id" in withTestDb { implicit session =>

      val commentId = commentsRepository.insert(comment)

      commentId must be_>=(0)
    }

    "perform insertion to db" in withTestDb { implicit session =>
      val oldCount = comments.length.run

      val commentId = commentsRepository.insert(comment)

      comments.length.run must_== (oldCount + 1)
      val actualComment = comments.filter(_.id === commentId).first
      actualComment must_== comment.copy(id = Some(commentId))


    }
  }

  "update comment" should {

    val time = DateTime.now
    val commentText = "Updated comment's content"
    val comment = CommentToUpdate(commentText, time + 2.hour)

    "update existing comment" in withTestDb { implicit session =>
      val commentId = comments.take(1).map(_.id).first

      val result = commentsRepository.update(commentId, comment)

      result must beTrue
      val actualContent = comments.filter(_.id === commentId).map(_.content).first
      actualContent must_== commentText

    }

    "return false when comment not exists" in withTestDb { implicit session =>
      val commentId = 9999
      comments.filter(_.id === commentId).firstOption must beNone

      val result = commentsRepository.update(commentId, comment)

      result must beFalse
    }
  }

  "delete comment" should {

    "remove correct comment" in withTestDb { implicit session =>
      val commentId = comments.take(1).map(_.id).first

      val removed = commentsRepository.delete(commentId)

      removed must beTrue
      comments.filter(_.id === commentId).firstOption must beNone
    }

    "return false when comment not found" in withTestDb { implicit session =>
      val commentId = 9998
      comments.filter(_.id === commentId).firstOption must beNone

      val removed = commentsRepository.delete(commentId)

      removed must beFalse
    }
  }

  "get comment by id" should {

    "return a valid comment" in withTestDb { implicit session =>
      val commentId = comments.take(1).map(_.id).first

      val comment = commentsRepository.get(commentId)

      val actualComment = comments.filter(_.id === commentId).firstOption
      comment must_== actualComment

    }
  }

}
