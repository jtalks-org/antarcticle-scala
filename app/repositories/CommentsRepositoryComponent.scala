package repositories

import models.database._
import scala.slick.jdbc.JdbcBackend

trait CommentsRepositoryComponent {

  val commentsRepository: CommentsRepository

  /**
   * Provides basic comment-related operations over a database.
   * Database session should be provided by a caller via implicit parameter.
   */
  trait CommentsRepository {
    def getByArticle(id: Int)(implicit session: JdbcBackend#Session): Seq[(CommentRecord, UserRecord)]

    def insert(comment: CommentRecord)(implicit session: JdbcBackend#Session): Int

    def update(id: Int, comment: CommentToUpdate)(implicit session: JdbcBackend#Session): Boolean

    def delete(id: Int)(implicit session: JdbcBackend#Session): Boolean

    def get(id: Int)(implicit session: JdbcBackend#Session): Option[CommentRecord]
  }

}

trait CommentsRepositoryComponentImpl extends CommentsRepositoryComponent {
  this: CommentsSchemaComponent with UsersSchemaComponent with Profile =>

  val commentsRepository = new SlickCommentsRepository

  import profile.simple._

  /**
   * Query extensions to avoid criteria duplication
   */
  implicit class CommentsExtension[E](val q: Query[Comments, E]) {
    def withAuthor = {
      q.leftJoin(users).on(_.userId === _.id)
    }

    def byId(id: Column[Int]): Query[Comments, E] = {
      q.filter(_.id === id)
    }
  }

  /**
   * Slick comment dao implementation based on precompiled queries.
   * For information about precompiled queries refer to
   * <p> http://slick.typesafe.com/doc/2.0.0/queries.html#compiled-queries
   * <p> http://stackoverflow.com/questions/21422394/why-cannot-use-compiled-insert-statement-in-slick
   */
  class SlickCommentsRepository extends CommentsRepository {

    val insertCompiled = comments.returning(comments.map(_.id)).insertInvoker
    val updateCompiled = Compiled((id: Column[Int]) => comments.byId(id).map(c => (c.content, c.updatedAt)))
    val byIdCompiled = Compiled((id: Column[Int]) => comments.byId(id))

    def insert(comment: CommentRecord)(implicit session: JdbcBackend#Session) = insertCompiled.insert(comment)

    def update(id: Int, comment: CommentToUpdate)(implicit session: JdbcBackend#Session) =
      updateCompiled(id).update(CommentToUpdate.unapply(comment).get) > 0

    def delete(id: Int)(implicit session: JdbcBackend#Session) = byIdCompiled(id).delete > 0

    def get(id: Int)(implicit session: JdbcBackend#Session) = byIdCompiled(id).firstOption

    // todo: for some reason query with tuple return type cannot be compiled, investigate it
    def getByArticle(id: Int)(implicit session: JdbcBackend#Session) = {
      comments
        .filter(_.articleId === id)
        .sortBy(_.createdAt.asc)
        .withAuthor
        .list
    }
  }

}
