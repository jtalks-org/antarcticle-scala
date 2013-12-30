package models.database

import java.sql.Timestamp
import scala.slick.lifted.ForeignKeyAction

case class CommentToInsert(userId: Int, articleId: Int,
                   content: String, createdAt: Timestamp)

case class CommentToUpdate(id: Option[Int], content: String, updatedAt: Timestamp)

case class Comment(id: Option[Int], userId: Int, articleId: Int,
                   content: String, createdAt: Timestamp, updatedAt: Timestamp)

trait CommentsSchemaComponent {
  this: Profile with UsersSchemaComponent with ArticlesSchemaComponent =>

  import profile.simple._

  /**
   * Comments to articles
   */
  object Comments extends Table[Comment]("comments") {
    // columns
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Int]("user_id", O.NotNull)
    def articleId = column[Int]("article_id", O.NotNull)
    def content = column[String]("content", O.NotNull)
    def createdAt = column[Timestamp]("created_at", O.NotNull)
    def updatedAt = column[Timestamp]("updated_at", O.Nullable)

    // FKs
    def author = foreignKey("comment_author_fk", userId, Users)(_.id)
    // remove comments on article deletion
    def article = foreignKey("comment_article_fk", articleId, Articles)(_.id, onDelete = ForeignKeyAction.Cascade)

    // projections
    def * = id.? ~ userId ~ articleId ~ content ~ createdAt ~ updatedAt <> (Comment.apply _, Comment.unapply _)
    def forUpdate = id.? ~ content ~ updatedAt <> (CommentToUpdate.apply _, CommentToUpdate.unapply _)
    def forInsert = userId ~ articleId ~ content ~ createdAt <>
      (CommentToInsert.apply _, CommentToInsert.unapply _) returning id
  }
}
