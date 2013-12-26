package models.database

import scala.slick.lifted.ForeignKeyAction

trait TagsSchemaComponent {
  this: Profile with ArticlesSchemaComponent =>

  import profile.simple._

  /**
   * Tag names table
   */
  object Tags extends Table[(Option[Int], String)]("tags") {
    // columns
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.NotNull)

    // projections
    def * = id.? ~ name
    def forInsert = name returning id
  }

  /**
   * Table for many to many association between articles and tags
   */
  object ArticlesTags extends Table[(Int, Int)]("articles_tags") {
    // columns
    def articleId = column[Int]("article_id", O.NotNull)
    def tagId = column[Int]("tag_id", O.NotNull)

    // FKs
    // remove association with tags on article deletion
    def article = foreignKey("article_fk", articleId, Articles)(_.id, onDelete = ForeignKeyAction.Cascade)
    // but don't touch tags
    def tag = foreignKey("tag_fk", tagId, Tags)(_.id, onDelete = ForeignKeyAction.Restrict)

    // projections
    def * = articleId ~ tagId
  }
}
