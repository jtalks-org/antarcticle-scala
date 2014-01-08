package repositories

import models.database.{Profile, UsersSchemaComponent, UserRecord}
import scala.slick.session.Session
import models.database.UserToInsert

trait UsersRepositoryComponent {
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

  implicit class UsersExtension[C](val q: Query[Users.type, C]) {
    def byId(id: Column[Int]): Query[Users.type, C] = {
      q.filter(_.id === id)
    }
  }

  class SlickUsersRepository extends UsersRepository {
    def getByRemeberToken(token: String)(implicit session: Session) = {
      (for {
        user <- Users if user.rememberToken === token
      } yield user).firstOption
    }

    def updateRememberToken(id: Int, tokenValue: String)(implicit session: Session) = {
      Query(Users).byId(id).map(_.rememberToken).update(tokenValue) > 0
    }

    def getByUsername(username: String)(implicit session: Session) = {
      Query(Users).filter(_.username === username).firstOption
    }

    def insert(userToInsert: UserToInsert)(implicit session: Session) = {
       Users.forInsert.insert(userToInsert)
    }

  }
}
