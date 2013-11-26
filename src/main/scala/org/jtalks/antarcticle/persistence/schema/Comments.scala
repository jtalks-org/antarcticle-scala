package org.jtalks.antarcticle.persistence.schema

import org.jtalks.antarcticle.persistence.Profile
import java.sql.Timestamp

case class Comment(id: Option[Int], userId: Int, articleId: Int,
                   content: String, createdAt: Timestamp, updatedAt: Timestamp)


trait Comments {
  this: Profile with UsersComponent with ArticlesComponent =>

  import profile.simple._

  object Comments extends Table[Comment]("comments") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Int]("user_id", O.NotNull)
    def articleId = column[Int]("article_id", O.NotNull)
    def content = column[String]("content", O.NotNull)
    def createdAt = column[Timestamp]("created_at", O.NotNull)
    def updatedAt = column[Timestamp]("updated_at")

    def author = foreignKey("author_fk", userId, Users)(_.id)
    def article = foreignKey("article_fk", articleId, Articles)(_.id)

    def * = id.? ~ userId ~ articleId ~ content ~ createdAt ~ updatedAt <> (Comment.apply _, Comment.unapply _)
    def autoInc = * returning id
  }
}
