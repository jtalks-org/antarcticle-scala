package models.database

import utils.SecurityUtil

case class UserRecord(id: Option[Int], username: String, password: String, email: String, admin: Boolean = false,
                      salt:Option[String] = None, firstName: Option[String] = None, lastName: Option[String] = None,
                      rememberToken: Option[String] = None, active: Boolean = false, uid: String = SecurityUtil.generateUid)

trait UsersSchemaComponent {
  this: Profile =>

  import profile.simple._

  /**
   * Antarcticle users
   */
  class Users(tag: profile.simple.Tag) extends Table[UserRecord](tag, "users") {
    // columns
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username", O.NotNull)
    def password = column[String]("password", O.NotNull)
    def email = column[String]("email", O.NotNull)
    def active = column[Boolean]("active", O.NotNull)
    def salt = column[String]("salt", O.Nullable)
    def admin = column[Boolean]("admin", O.NotNull)
    def firstName = column[String]("first_name", O.Nullable)
    def lastName = column[String]("last_name", O.Nullable)
    def rememberToken = column[String]("remember_token", O.Nullable)
    def uid = column[String]("uid", O.NotNull)

    // projections
    def * = (id.?, username, password, email, admin, salt.?, firstName.?, lastName.?, rememberToken.?, active, uid) <> (UserRecord.tupled, UserRecord.unapply)
  }

  val users = TableQuery[Users]
}

