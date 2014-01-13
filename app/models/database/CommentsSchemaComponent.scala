package models.database

import java.sql.Timestamp
import scala.slick.model.ForeignKeyAction

case class CommentToInsert(userId: Int, articleId: Int,
                   content: String, createdAt: Timestamp)

case class CommentToUpdate(content: String, updatedAt: Timestamp)

case class CommentRecord(id: Option[Int], userId: Int, articleId: Int,
                   content: String, createdAt: Timestamp, updatedAt: Option[Timestamp])

trait CommentsSchemaComponent {
  this: Profile with UsersSchemaComponent with ArticlesSchemaComponent =>

  import profile.simple._

  /**
   * Comments to articles
   */
  class Comments(tag: Tag) extends Table[CommentRecord](tag, "comments") {
    // columns
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Int]("user_id", O.NotNull)
    def articleId = column[Int]("article_id", O.NotNull)
    def content = column[String]("content", O.NotNull, O.DBType("text"))
    def createdAt = column[Timestamp]("created_at", O.NotNull)
    def updatedAt = column[Timestamp]("updated_at", O.Nullable)

    // FKs
    def author = foreignKey("comment_author_fk", userId, users)(_.id)
    // remove comments on article deletion
    def article = foreignKey("comment_article_fk", articleId, articles)(_.id, onDelete = ForeignKeyAction.Cascade)

    // projections
    def * = (id.?, userId, articleId, content, createdAt, updatedAt.?) <> (CommentRecord.tupled, CommentRecord.unapply)
  }

  val comments = TableQuery[Comments]
}
