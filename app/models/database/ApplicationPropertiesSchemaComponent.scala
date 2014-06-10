package models.database

import java.sql.Timestamp

case class ApplicationProperty (id: Option[Int], name: String, var value: Option[String], defaultValue: String, createdAt: Timestamp)

trait ApplicationPropertiesSchemaComponent {
  this: Profile =>

  import profile.simple._

  class ApplicationProperties(tag: scala.slick.lifted.Tag) extends Table[ApplicationProperty](tag, "properties") {
    // columns
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.NotNull)
    def value = column[Option[String]]("value", O.Nullable)
    def defaultValue = column[String]("default_value", O.NotNull)
    def createdAt = column[Timestamp]("created_at", O.Nullable)

    def * = (id.?, name, value, defaultValue, createdAt) <> (ApplicationProperty.tupled, ApplicationProperty.unapply)
  }

  val properties = TableQuery[ApplicationProperties]
}
