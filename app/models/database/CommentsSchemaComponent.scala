package models.database

import java.sql.Timestamp

case class CommentToInsert(userId: Int, articleId: Int,
                   content: String, createdAt: Timestamp)

case class CommentToUpdate(content: String, updatedAt: Timestamp)

case class Comment(id: Option[Int], userId: Int, articleId: Int,
                   content: String, createdAt: Timestamp, updatedAt: Timestamp)

trait CommentsSchemaComponent {
  this: Profile with UsersSchemaComponent with ArticlesSchemaComponent =>

  import profile.simple._

  object Comments extends Table[Comment]("comments") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Int]("user_id", O.NotNull)
    def articleId = column[Int]("article_id", O.NotNull)
    def content = column[String]("content", O.NotNull)
    def createdAt = column[Timestamp]("created_at", O.NotNull)
    def updatedAt = column[Timestamp]("updated_at")

    def author = foreignKey("comment_author_fk", userId, Users)(_.id)
    def article = foreignKey("comment_article_fk", articleId, Articles)(_.id)

    def * = id.? ~ userId ~ articleId ~ content ~ createdAt ~ updatedAt <> (Comment.apply _, Comment.unapply _)
    def forUpdate = content ~ updatedAt <> (CommentToUpdate.apply _, CommentToUpdate.unapply _)
    def forInsert = userId ~ articleId ~ content ~ createdAt <>
      (CommentToInsert.apply _, CommentToInsert.unapply _) returning id
  }
}
