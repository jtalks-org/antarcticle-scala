package repositories

import models.database._
import scala.slick.jdbc.JdbcBackend
import scala.slick.lifted


trait UsersRepositoryComponent {
  val usersRepository: UsersRepository

  /**
   * Provides basic user-related operations over a database.
   * Database session should be provided by a caller via implicit parameter.
   */
  trait UsersRepository {
    def getByRememberToken(token: String)(implicit session: JdbcBackend#Session): Option[UserRecord]

    def updateRememberToken(id: Int, tokenValue: String)(implicit session: JdbcBackend#Session): Boolean

    def getByUsername(username: String)(implicit session: JdbcBackend#Session): Option[UserRecord]

    def insert(userToInert: UserRecord)(implicit session: JdbcBackend#Session): Int

    def updatePassword(id:Int, password: String, salt: Option[String])(implicit session: JdbcBackend#Session): Unit

    def findByUserName(username: String)(implicit session: JdbcBackend#Session): List[UserRecord]

  }
}

trait UsersRepositoryComponentImpl extends UsersRepositoryComponent {
  this: UsersSchemaComponent with Profile =>

  val usersRepository = new SlickUsersRepository

  import profile.simple._

  /**
   * Query extensions to avoid criteria duplication
   */
  implicit class UsersExtension[C](val q: Query[Users, C]) {
    def byId(id: Column[Int]): Query[Users, C] = {
      q.filter(_.id === id)
    }
    type SColumn = Column[String]
    def byUsername(username: SColumn, f: SColumn => SColumn = col => col ): Query[Users, C] = {
      q.filter(user => f(user.username) === f(username))
    }
  }

  /**
   * Slick user dao implementation based on precompiled queries.
   * For information about precompiled queries refer to
   * <p> http://slick.typesafe.com/doc/2.0.0/queries.html#compiled-queries
   * <p> http://stackoverflow.com/questions/21422394/why-cannot-use-compiled-insert-statement-in-slick
   */
  class SlickUsersRepository extends UsersRepository {

    val byUsernameCompiled = Compiled((username: Column[String]) => users.byUsername(username))
    val byUsernameIgnoreCaseCompiled = Compiled {
      username: Column[String] => users.byUsername(username, {_.toLowerCase})
    }
    val byTokenCompiled = Compiled((token: Column[String]) => users.filter(_.rememberToken === token))
    val updateTokenCompiled = Compiled((id: Column[Int]) => users.byId(id).map(_.rememberToken))
    val insertUserCompiled = users.returning(users.map(_.id)).insertInvoker
    val byIdCompiled = Compiled((id: Column[Int]) => users.byId(id))
    val forUpdateCompiled = Compiled((id: Column[Int]) => users.byId(id).map(u => (u.password, u.salt)))

    def getByRememberToken(token: String)(implicit session: JdbcBackend#Session) =
      byTokenCompiled(token).firstOption

    def getByUsername(username: String)(implicit session: JdbcBackend#Session) =
      byUsernameCompiled(username).firstOption

    def updateRememberToken(id: Int, tokenValue: String)(implicit session: JdbcBackend#Session) =
      updateTokenCompiled(id).update(tokenValue) > 0

    def insert(userToInsert: UserRecord)(implicit session: JdbcBackend#Session) =
      insertUserCompiled.insert(userToInsert)

    def findByUserName(username: String)(implicit session: JdbcBackend#Session): List[UserRecord] =
      byUsernameIgnoreCaseCompiled(username).list

    def updatePassword(id:Int, password: String, salt: Option[String])(implicit session: JdbcBackend#Session) = {
      for (s <- salt) yield forUpdateCompiled(id).update(password, s)
    }
  }
}
