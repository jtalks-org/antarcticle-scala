package models.database

import java.sql.Timestamp

case class Property (id: Option[Int], name: String, var value: Option[String], defaultValue: String, createdAt: Timestamp)

trait PropertiesSchemaComponent {
  this: Profile =>

  import profile.simple._

  class Properties(tag: scala.slick.lifted.Tag) extends Table[Property](tag, "properties") {
    // columns
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name", O.NotNull)
    def value = column[Option[String]]("value", O.Nullable)
    def defaultValue = column[String]("default_value", O.NotNull)
    def createdAt = column[Timestamp]("created_at", O.Nullable)

    def * = (id.?, name, value, defaultValue, createdAt) <> (Property.tupled, Property.unapply)
  }

  val properties = TableQuery[Properties]
}
