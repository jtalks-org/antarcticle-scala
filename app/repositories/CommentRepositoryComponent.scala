package repositories

import models.database._
import scala.slick.jdbc.JdbcBackend

/**
  * Provides persistence for article comments
 */
trait CommentRepositoryComponent {

  val commentRepository: CommentRepository

  trait CommentRepository {
    def getByArticle(id: Int)(implicit session: JdbcBackend#Session): List[(CommentRecord, UserRecord)]
    def insert(comment: CommentRecord)(implicit session: JdbcBackend#Session): Int
    def update(id: Int, comment: CommentToUpdate)(implicit session: JdbcBackend#Session): Boolean
    def delete(id: Int)(implicit session: JdbcBackend#Session): Boolean
  }
}

trait CommentRepositoryComponentImpl extends CommentRepositoryComponent {
  this: CommentsSchemaComponent with UsersSchemaComponent with Profile =>

  val commentRepository = new SlickCommentRepository

  class SlickCommentRepository extends CommentRepository {

    import profile.simple._

    implicit class CommentsExtension[E](val q: Query[Comments, E]) {
      def withAuthor = {
        q.leftJoin(users).on(_.userId === _.id)
      }

      def byId(id: Column[Int]): Query[Comments, E] = {
        q.filter(_.id === id)
      }
    }

    def getByArticle(id: Int)(implicit session: JdbcBackend#Session) = {
      comments
        .filter(_.articleId === id)
        .sortBy(_.createdAt.asc)
        .withAuthor
        .list
    }

    def insert(comment: CommentRecord)(implicit session: JdbcBackend#Session) = {
      comments.returning(comments.map(_.id)) += comment
    }

    def update(id: Int, comment: CommentToUpdate)(implicit session: JdbcBackend#Session) = {
     comments.byId(id)
        .map(c => (c.content, c.updatedAt))
        .update(CommentToUpdate.unapply(comment).get) > 0
    }

    def delete(id: Int)(implicit session: JdbcBackend#Session) = {
      comments.byId(id).delete > 0
    }
  }

}
