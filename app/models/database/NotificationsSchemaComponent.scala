package models.database

import java.sql.Timestamp
import scala.slick.model.ForeignKeyAction

case class Notification(id: Option[Int], var userId: Int, articleId: Int, commentId: Int,
                         var title: String, content: String, createdAt: Timestamp)

trait NotificationsSchemaComponent {
  this: Profile with UsersSchemaComponent with ArticlesSchemaComponent with CommentsSchemaComponent =>

  import profile.simple._

  /**
   * Notifications are simple messages with title and short text to attract user attention.
   * Notification can be created when new comments are added to your article, if someone
   * spottes orthographic error und so weiter
   */
  class Notifications(tag: scala.slick.lifted.Tag) extends Table[Notification](tag, "notifications") {
    // columns
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Int]("user_id", O.NotNull)
    def articleId = column[Int]("article_id", O.NotNull)
    def commentId = column[Int]("comment_id", O.NotNull)
    def title = column[String]("title", O.NotNull)
    def content = column[String]("content", O.NotNull)
    def createdAt = column[Timestamp]("created_at", O.Nullable)

    // FKs
    def user = foreignKey("notification_user_fk", userId, users)(_.id, onDelete = ForeignKeyAction.Cascade)
    def article = foreignKey("notification_article_fk", articleId, articles)(_.id, onDelete = ForeignKeyAction.Cascade)
    def comment = foreignKey("notification_comment_fk", commentId, comments)(_.id, onDelete = ForeignKeyAction.Cascade)

    // projections
    def * = (id.?, userId, articleId, commentId, title, content, createdAt) <> (Notification.tupled, Notification.unapply)
  }

  val notifications = TableQuery[Notifications]
}