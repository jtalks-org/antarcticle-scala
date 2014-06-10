package models.database

/**
 * Database schema layer
 */
trait Schema
  extends UsersSchemaComponent
  with ArticlesSchemaComponent
  with CommentsSchemaComponent
  with TagsSchemaComponent
  with NotificationsSchemaComponent
  with ApplicationPropertiesSchemaComponent {
  this: Profile =>

  lazy val schema = Vector(users, articles, comments, tags, articlesTags, notifications, properties)
}
