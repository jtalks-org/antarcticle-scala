package models.database

import scala.slick.jdbc.meta.MTable

trait Schema
  extends ArticlesSchemaComponent
  with CommentsSchemaComponent
  with UsersSchemaComponent
  with TagsSchemaComponent {

  this: Profile =>

  import profile.simple._

  def createSchema(implicit session: Session) = {
    def createIfNotExists[T](table: Table[T])(implicit session: Session) = {
      if (MTable.getTables(table.tableName).list.isEmpty) {
        table.ddl.create
      }
    }

    createIfNotExists(Users)
    createIfNotExists(Articles)
    createIfNotExists(Comments)
    createIfNotExists(Tags)
  }
}