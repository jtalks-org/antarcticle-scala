package services

import scala.slick.jdbc.JdbcBackend.Session
import repositories.CommentRepositoryComponent
import models.database._
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
    def update(id: Int, comment: CommentToUpdate): Boolean
    def removeComment(id: Int): Boolean
  }
}

trait CommentServiceComponentImpl extends CommentServiceComponent {
  this: SessionProvider with CommentRepositoryComponent =>

  val commentService = new CommentServiceImpl

  class CommentServiceImpl extends CommentService {

    def getByArticle(articleId: Int) = withSession { implicit s: Session =>
      commentRepository.getByArticle(articleId).map((toComment _).tupled)
    }

    // todo: real user
    def insert(articleId: Int, content : String) = withTransaction { implicit s: Session =>
        val userId = 1
        val toInsert = CommentRecord(None, userId, articleId, content, DateTime.now)
        val id = commentRepository.insert(toInsert)
        toInsert.copy(id = Some(id))
    }

    def update(id: Int, comment: CommentToUpdate) = withTransaction { implicit s: Session =>
      commentRepository.update(id, comment)
    }

    def removeComment(id: Int) = withTransaction { implicit s: Session =>
      commentRepository.delete(id)
    }

    //TODO: remove UserRecord to UserModel duplication with article service
    private def toComment(record: CommentRecord, user: UserRecord) = {
      Comment(record.id.get, UserModel(user.id.get, user.username), record.articleId,
        record.content, record.createdAt, record.updatedAt)
    }
  }
}
