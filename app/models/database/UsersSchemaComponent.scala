package models.database

case class UserRecord(id: Option[Int], username: String, admin: Boolean = false,
                firstName: Option[String] = None, lastName: Option[String] = None, rememberToken: Option[String] = None)

trait UsersSchemaComponent {
  this: Profile =>

  import profile.simple._

  /**
   * Antarcticle users
   */
  class Users(tag: Tag) extends Table[UserRecord](tag, "users") {
    // columns
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username", O.NotNull)
    def admin = column[Boolean]("admin", O.NotNull)
    def firstName = column[String]("first_name", O.Nullable)
    def lastName = column[String]("last_name", O.Nullable)
    def rememberToken = column[String]("remember_token", O.Nullable)

    // projections
    def * = (id.?, username, admin, firstName.?, lastName.?, rememberToken.?) <> (UserRecord.tupled, UserRecord.unapply)
  }

  val users = TableQuery[Users]
}

