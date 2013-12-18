package models.database

import java.sql.Timestamp
import play.api.db.slick.Config.driver.simple._

case class Comment(id: Option[Int], userId: Int, articleId: Int,
                   content: String, createdAt: Timestamp, updatedAt: Timestamp)

trait CommentsComponent {
  this: Profile with UsersComponent with ArticlesComponent =>

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
    def autoInc = * returning id
  }
}
