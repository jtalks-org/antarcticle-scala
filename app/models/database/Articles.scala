package models.database

import java.sql.Timestamp
import play.api.db.slick.Config.driver.simple._

case class ArticleRecord(id: Option[Int], title: String, content: String,
                   createdAt: Timestamp, updatedAt: Timestamp, description: String, authorId: Int)

case class ArticleToInsert(title: String, content: String, createdAt: Timestamp,
                           updatedAt: Timestamp, description: String, authorId: Int)

case class ArticleToUpdate(title: String, content: String,
                           updatedAt: Timestamp, description: String)

trait ArticlesComponent  {
  this: Profile with UsersComponent =>

  import profile.simple._

  object Articles extends Table[ArticleRecord]("articles") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title", O.NotNull)
    def content = column[String]("content", O.NotNull)
    def createdAt = column[Timestamp]("created_at", O.NotNull)
    def updatedAt = column[Timestamp]("updated_at")
    def description = column[String]("description")
    def authorId = column[Int]("author_id", O.NotNull)

    def author = foreignKey("article_author_fk", authorId, Users)(_.id)

    def * = id.? ~ title ~ content ~ createdAt ~ updatedAt ~ description ~ authorId <> (ArticleRecord.apply _, ArticleRecord.unapply _)

    def forUpdate = title ~ content ~ updatedAt ~ description <> (ArticleToUpdate.apply _, ArticleToUpdate.unapply _)

    def forInsert = title ~ content ~ createdAt ~ updatedAt ~ description ~ authorId <> (ArticleToInsert.apply _, ArticleToInsert.unapply _) returning id

    def authorIdx = index("index_articles_on_user_id", authorId)
  }

}
