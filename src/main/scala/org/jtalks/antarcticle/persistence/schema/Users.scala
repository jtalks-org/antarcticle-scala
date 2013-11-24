package org.jtalks.antarcticle.persistence.schema

import org.jtalks.antarcticle.persistence.Profile

case class User(id: Option[Int], username: String)

trait UsersComponent {
  this: Profile =>

  import profile.simple._

  object Users extends Table[User]("users") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username")
    def * = id.? ~ username <> (User.apply _, User.unapply _)

    def autoInc = * returning id
  }
}

