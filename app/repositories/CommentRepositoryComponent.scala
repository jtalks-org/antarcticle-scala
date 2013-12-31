package repositories

import scala.slick.session.Session
import models.database._
import models.database.CommentRecord
import models.database.CommentToInsert

/**
 * Provides persistence for article comments
 */
trait CommentRepositoryComponent {
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

    def getByArticle(id: Int)(implicit session: Session) = {
      Query(Comments)
        .filter(_.articleId === id)
        .sortBy(_.createdAt.asc)
        .list
    }

    def insert(comment: CommentToInsert)(implicit session: Session) = {
        Comments.forInsert.insert(comment)
    }

    def update(id: Int, comment: CommentToUpdate)(implicit session: Session) = {
     Comments
        .filter(_.id === id)
        .map(_.forUpdate)
        .update(comment) > 0
    }

    def delete(id: Int)(implicit session: Session) = {
      Comments.where(_.id === id).delete > 0
    }
  }

}
