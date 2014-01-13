package repositories

import models.database._
import models.database.CommentRecord
import models.database.CommentToInsert
import models.database.CommentToUpdate

/**
 * Provides persistence for article comments
 */
trait CommentRepositoryComponent {
  import scala.slick.jdbc.JdbcBackend.Session

  val commentRepository: CommentRepository

  trait CommentRepository {

    def getByArticle(id: Int)(implicit session: Session): List[CommentRecord]

    def insert(comment: CommentToInsert)(implicit session: Session): Int

    def update(id: Int, comment: CommentToUpdate)(implicit session: Session): Boolean

    def delete(id: Int)(implicit session: Session): Boolean
  }
}

trait CommentRepositoryComponentImpl extends CommentRepositoryComponent {
  this: CommentsSchemaComponent with Profile =>

  val commentRepository = new SlickCommentRepository

  class SlickCommentRepository extends CommentRepository {

    import profile.simple._
    import scala.slick.jdbc.JdbcBackend.Session

    def getByArticle(id: Int)(implicit session: Session) = {
      comments
        .filter(_.articleId === id)
        .sortBy(_.createdAt.asc)
        .list
    }

    def insert(comment: CommentToInsert)(implicit session: Session) = {
      comments.map(c => (c.userId, c.articleId, c.content, c.createdAt))
        .returning(comments.map(_.id)) += CommentToInsert.unapply(comment).get
    }

    def update(id: Int, comment: CommentToUpdate)(implicit session: Session) = {
     comments
        .filter(_.id === id)
        .map(c => (c.content, c.updatedAt))
        .update(CommentToUpdate.unapply(comment).get) > 0
    }

    def delete(id: Int)(implicit session: Session) = {
      comments.where(_.id === id).delete > 0
    }
  }

}
