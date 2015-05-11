package models.database

case class Tag(id: Int, name: String)

trait TagsSchemaComponent {
  this: Profile with ArticlesSchemaComponent =>

  import profile.simple._

  /**
   * Tag names table
   */
  class Tags(tag: scala.slick.lifted.Tag) extends Table[(Option[Int], String)](tag, "tags") {
    // columns
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.NotNull)

    // projections
    def * = (id.?, name)
  }
  val tags = TableQuery[Tags]

  /**
   * Table for many to many association between articles and tags
   */
  class ArticlesTags(tag: scala.slick.lifted.Tag) extends Table[(Int, Int)](tag, "articles_tags") {
    // columns
    def articleId = column[Int]("article_id", O.NotNull)
    def tagId = column[Int]("tag_id", O.NotNull)

    // FKs
    // remove association with tags on article deletion
    def article = foreignKey("article_fk", articleId, articles)(_.id, onDelete = ForeignKeyAction.Cascade)
    // but don't touch tags
    def tagFk = foreignKey("tag_fk", tagId, tags)(_.id, onDelete = ForeignKeyAction.Restrict)

    // projections
    def * = (articleId, tagId)
  }

  val articlesTags = TableQuery[ArticlesTags]
}
