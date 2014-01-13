package models.database

/**
 * Database schema layer
 */
trait Schema
  extends ArticlesSchemaComponent
  with CommentsSchemaComponent
  with UsersSchemaComponent
  with TagsSchemaComponent {

  this: Profile =>

  lazy val schema = Vector(users, articles, comments, tags, articlesTags)
}
