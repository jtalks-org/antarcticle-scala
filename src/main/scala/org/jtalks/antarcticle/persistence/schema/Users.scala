package org.jtalks.antarcticle.persistence.schema

import org.jtalks.antarcticle.persistence.Profile

case class User(id: Option[Int], username: String, admin: Boolean = false,
                firstName: Option[String] = None, lastName: Option[String] = None)

trait UsersComponent {
  this: Profile =>

  import profile.simple._

  object Users extends Table[User]("users") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username", O.NotNull)
    def admin = column[Boolean]("admin", O.NotNull)
    def firstName = column[String]("first_name", O.Nullable)
    def lastName = column[String]("last_name", O.Nullable)

    def * = id.? ~ username ~ admin ~ firstName.? ~ lastName.? <> (User.apply _, User.unapply _)
    def autoInc = * returning id

    def usernameIdx = index("index_users_on_username", username, unique = true)
  }
}

