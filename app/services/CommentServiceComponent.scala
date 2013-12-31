package services

import scala.slick.session.Session
import repositories.CommentRepositoryComponent
import models.database.{CommentToUpdate, CommentToInsert, CommentRecord}
import org.joda.time.DateTime
import utils.DateImplicits._
import models.CommentModels.Comment
import models.UserModels.UserModel

/**
 *
 */
trait CommentServiceComponent {
  val commentService: CommentService

  trait CommentService {
    def getByArticle(id: Int): List[Comment]

    def insert(articleId: Int, content : String): CommentRecord

    def update(comment: CommentToUpdate): Boolean

    def removeComment(id: Int): Boolean
  }
}

trait CommentServiceComponentImpl extends CommentServiceComponent {
  this: SessionProvider with CommentRepositoryComponent =>

  val commentService = new CommentServiceImpl

  class CommentServiceImpl extends CommentService {

    def getByArticle(articleId: Int) = withTransaction {
      implicit s: Session =>
        //todo: real user
        commentRepository.getByArticle(articleId).map((x : CommentRecord) =>
          Comment(x.id.get, UserModel(1, "admin"), x.articleId, x.content, x.createdAt,x.updatedAt))
    }

    def insert(articleId: Int, content : String) = withTransaction {
      implicit s: Session =>
        // todo: real user
        val userId = 1
        val id = commentRepository.insert(CommentToInsert(userId, articleId, content, DateTime.now))
        CommentRecord(Some(id), userId, articleId, content, DateTime.now, null)
    }

    def update(comment: CommentToUpdate) = withTransaction {
      implicit s: Session =>
        commentRepository.update(comment)
    }

    def removeComment(id: Int) = withTransaction {
      implicit s: Session =>
        commentRepository.delete(id)
    }
  }
}