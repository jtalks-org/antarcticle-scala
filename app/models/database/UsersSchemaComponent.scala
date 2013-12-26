package models.database

case class UserToInsert(username: String, admin: Boolean = false,
                      firstName: Option[String] = None, lastName: Option[String] = None)

case class UserRecord(id: Option[Int], username: String, admin: Boolean = false,
                firstName: Option[String] = None, lastName: Option[String] = None)

trait UsersSchemaComponent {
  this: Profile =>

  import profile.simple._

  object Users extends Table[UserRecord]("users") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username", O.NotNull)
    def admin = column[Boolean]("admin", O.NotNull)
    def firstName = column[String]("first_name", O.Nullable)
    def lastName = column[String]("last_name", O.Nullable)

    def * = id.? ~ username ~ admin ~ firstName.? ~ lastName.? <> (UserRecord.apply _, UserRecord.unapply _)
    def forInsert = username ~ admin ~ firstName.? ~ lastName.? <> (UserToInsert.apply _, UserToInsert.unapply _) returning id

    //def usernameIdx = index("index_users_on_username", username, unique = true)
  }
}

