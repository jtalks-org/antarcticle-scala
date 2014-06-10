package models.database

import java.sql.Timestamp

/**
 * Represent application property, that sets a configuration for whole application.
 * Application properties can be edited by admins only, all other users don't
 * have permissions to change app properties.
 * For example admin can change the name of application and new name will be displayed for all users.
 */
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
