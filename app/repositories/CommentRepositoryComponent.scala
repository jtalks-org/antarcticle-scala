package repositories

import models.database._

/**
 * Provides persistence for article comments
 */
trait CommentRepositoryComponent {
  import scala.slick.jdbc.JdbcBackend.Session

  val commentRepository: CommentRepository

  trait CommentRepository {
    def getByArticle(id: Int)(implicit session: Session): List[(CommentRecord, UserRecord)]
    def insert(comment: CommentRecord)(implicit session: Session): Int
    def update(id: Int, comment: CommentToUpdate)(implicit session: Session): Boolean
    def delete(id: Int)(implicit session: Session): Boolean
  }
}

trait CommentRepositoryComponentImpl extends CommentRepositoryComponent {
  this: CommentsSchemaComponent with UsersSchemaComponent with Profile =>

  val commentRepository = new SlickCommentRepository

  class SlickCommentRepository extends CommentRepository {

    import profile.simple._
    import scala.slick.jdbc.JdbcBackend.Session

    implicit class CommentsExtension[E](val q: Query[Comments, E]) {
      def withAuthor = {
        q.leftJoin(users).on(_.userId === _.id)
      }

      def byId(id: Column[Int]): Query[Comments, E] = {
        q.filter(_.id === id)
      }
    }

    def getByArticle(id: Int)(implicit session: Session) = {
      comments
        .filter(_.articleId === id)
        .sortBy(_.createdAt.asc)
        .withAuthor
        .list
    }

    def insert(comment: CommentRecord)(implicit session: Session) = {
      comments.returning(comments.map(_.id)) += comment
    }

    def update(id: Int, comment: CommentToUpdate)(implicit session: Session) = {
     comments.byId(id)
        .map(c => (c.content, c.updatedAt))
        .update(CommentToUpdate.unapply(comment).get) > 0
    }

    def delete(id: Int)(implicit session: Session) = {
      comments.byId(id).delete > 0
    }
  }

}
