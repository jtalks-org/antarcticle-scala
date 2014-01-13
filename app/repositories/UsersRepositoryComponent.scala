package repositories

import models.database.{Profile, UsersSchemaComponent, UserRecord}
import models.database.UserToInsert

trait UsersRepositoryComponent {
  import scala.slick.jdbc.JdbcBackend.Session

  val usersRepository: UsersRepository

  trait UsersRepository {
    def getByRemeberToken(token: String)(implicit session: Session): Option[UserRecord]
    def updateRememberToken(id: Int, tokenValue: String)(implicit session: Session): Boolean
    def getByUsername(username: String)(implicit session: Session): Option[UserRecord]
    def insert(userToInert: UserToInsert)(implicit session: Session): Int
  }
}

trait UsersRepositoryComponentImpl extends UsersRepositoryComponent {
  this: UsersSchemaComponent with Profile =>

  val usersRepository = new SlickUsersRepository

  import profile.simple._
  import scala.slick.jdbc.JdbcBackend.Session

  implicit class UsersExtension[C](val q: Query[Users, C]) {
    def byId(id: Column[Int]): Query[Users, C] = {
      q.filter(_.id === id)
    }
  }

  class SlickUsersRepository extends UsersRepository {
    def getByRemeberToken(token: String)(implicit session: Session) = {
      (for {
        user <- users if user.rememberToken === token
      } yield user).firstOption
    }

    def updateRememberToken(id: Int, tokenValue: String)(implicit session: Session) = {
      users.byId(id).map(_.rememberToken).update(tokenValue) > 0
    }

    def getByUsername(username: String)(implicit session: Session) = {
      users.filter(_.username === username).firstOption
    }

    def insert(userToInsert: UserToInsert)(implicit session: Session) = {
       users.map(u => (u.username, u.admin, u.firstName.?, u.lastName.?))
         .insert(UserToInsert.unapply(userToInsert).get)
    }

  }
}
