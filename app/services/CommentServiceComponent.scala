package services

import scala.slick.session.Session
import repositories.CommentRepositoryComponent
import models.database.{CommentToUpdate, CommentToInsert, Comment}
import org.joda.time.DateTime
import utils.DateImplicits._

/**
 *
 */
trait CommentServiceComponent {
  val commentService: CommentService

  trait CommentService {
    def getByArticle(id: Int): Seq[Comment]

    def insert(articleId: Int, content : String): Comment

    def update(comment: CommentToUpdate): Boolean

    def removeComment(id: Int): Boolean
  }
}

trait CommentServiceComponentImpl extends CommentServiceComponent {
  this: SessionProvider with CommentRepositoryComponent =>

  val commentService = new CommentServiceImpl

  class CommentServiceImpl extends CommentService {

    def getByArticle(articleId: Int): Seq[Comment] = withTransaction {
      implicit s: Session =>
        commentRepository.getByArticle(articleId)
    }

    def insert(articleId: Int, content : String): Comment = withTransaction {
      implicit s: Session =>
        // todo: real user
        val userId = 1
        val id = commentRepository.insert(CommentToInsert(userId, articleId, content, DateTime.now))
        Comment(Some(id), userId, articleId, content, DateTime.now, DateTime.now)
    }

    def update(comment: CommentToUpdate): Boolean = withTransaction {
      implicit s: Session =>
        commentRepository.update(comment)
    }

    def removeComment(id: Int): Boolean = withTransaction {
      implicit s: Session =>
        commentRepository.delete(id)
    }
  }
}