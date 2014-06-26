package models.database

import java.sql.Timestamp

case class ArticleRecord(id: Option[Int], title: String, content: String,
                   createdAt: Timestamp, updatedAt: Timestamp, description: String,
                   authorId: Int, language: String, sourceId: Option[Int])

case class ArticleToUpdate(title: String, content: String,
                           updatedAt: Timestamp, description: String)

trait ArticlesSchemaComponent  {
  this: Profile with UsersSchemaComponent =>

  import profile.simple._

  class Articles(tag: scala.slick.lifted.Tag) extends Table[ArticleRecord](tag, "articles") {
    // columns
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title", O.NotNull)
    def content = column[String]("content", O.NotNull, O.DBType("text"))
    def createdAt = column[Timestamp]("created_at", O.Nullable)
    def updatedAt = column[Timestamp]("updated_at", O.Nullable)
    def description = column[String]("description", O.NotNull, O.DBType("text"))
    def authorId = column[Int]("author_id", O.NotNull)
    def language = column[String]("language", O.Nullable)
    def sourceId = column[Int]("source_id", O.Nullable)

    // FKs
    def author = foreignKey("article_author_fk", authorId, users)(_.id)

    // projections
    def * = (id.?, title, content, createdAt, updatedAt, description, authorId, language, sourceId.?) <> (ArticleRecord.tupled, ArticleRecord.unapply)
  }

  val articles = TableQuery[Articles]
}
