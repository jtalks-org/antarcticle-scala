package models.database

import scala.slick.jdbc.meta.MTable

/**
 * Database schema layer
 */
trait Schema
  extends ArticlesSchemaComponent
  with CommentsSchemaComponent
  with UsersSchemaComponent
  with TagsSchemaComponent {

  this: Profile =>

  import profile.simple._

  lazy val schema = Vector(Users, Articles, Comments, Tags,
    ArticlesTags)
}
