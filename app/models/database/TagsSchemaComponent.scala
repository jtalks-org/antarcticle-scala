package models.database


trait TagsSchemaComponent {
  this: Profile with ArticlesSchemaComponent =>

  import profile.simple._

  object Tags extends Table[(Option[Int], String)]("tags") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.NotNull)

    def * = id.? ~ name
  }

  object ArticleTag extends Table[(Int, Int)]("article_tags") {
    def articleId = column[Int]("article_id", O.NotNull)
    def tagId = column[Int]("tag_id", O.NotNull)

    def article = foreignKey("article_fk", articleId, Articles)(_.id)
    def tag = foreignKey("tag_fk", tagId, Tags)(_.id)

    def * = articleId ~ tagId
  }
}
