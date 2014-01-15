package repositories

import models.database._
import scala.slick.jdbc.JdbcBackend

trait UsersRepositoryComponent {
  val usersRepository: UsersRepository

  trait UsersRepository {
    def getByRemeberToken(token: String)(implicit session: JdbcBackend#Session): Option[UserRecord]
    def updateRememberToken(id: Int, tokenValue: String)(implicit session: JdbcBackend#Session): Boolean
    def getByUsername(username: String)(implicit session: JdbcBackend#Session): Option[UserRecord]
    def insert(userToInert: UserRecord)(implicit session: JdbcBackend#Session): Int
  }
}

trait UsersRepositoryComponentImpl extends UsersRepositoryComponent {
  this: UsersSchemaComponent with Profile =>

  val usersRepository = new SlickUsersRepository

  import profile.simple._

  implicit class UsersExtension[C](val q: Query[Users, C]) {
    def byId(id: Column[Int]): Query[Users, C] = {
      q.filter(_.id === id)
    }
  }

  class SlickUsersRepository extends UsersRepository {
    def getByRemeberToken(token: String)(implicit session: JdbcBackend#Session) = {
      (for {
        user <- users if user.rememberToken === token
      } yield user).firstOption
    }

    def updateRememberToken(id: Int, tokenValue: String)(implicit session: JdbcBackend#Session) = {
      users.byId(id).map(_.rememberToken).update(tokenValue) > 0
    }

    def getByUsername(username: String)(implicit session: JdbcBackend#Session) = {
      users.filter(_.username === username).firstOption
    }

    def insert(userToInsert: UserRecord)(implicit session: JdbcBackend#Session) = {
       users.returning(users.map(_.id)).insert(userToInsert)
    }

  }
}
