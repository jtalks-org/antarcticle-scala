package repositories

import models.database._
import scala.slick.jdbc.JdbcBackend

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
  }

  /**
   * Slick user dao implementation based on precompiled queries.
   * For information about precompiled queries refer to
   * <p> http://slick.typesafe.com/doc/2.0.0/queries.html#compiled-queries
   * <p> http://stackoverflow.com/questions/21422394/why-cannot-use-compiled-insert-statement-in-slick
   */
  class SlickUsersRepository extends UsersRepository {

    val byUsernameCompiled = Compiled((username: Column[String]) => users.filter(_.username === username))
    val byTokenCompiled = Compiled((token: Column[String]) => users.filter(_.rememberToken === token))
    val updateTokenCompiled = Compiled((id: Column[Int]) => users.byId(id).map(_.rememberToken))
    val insertUserCompiled = users.returning(users.map(_.id)).insertInvoker

    def getByRememberToken(token: String)(implicit session: JdbcBackend#Session) =
      byTokenCompiled(token).firstOption

    def getByUsername(username: String)(implicit session: JdbcBackend#Session) =
      byUsernameCompiled(username).firstOption

    def updateRememberToken(id: Int, tokenValue: String)(implicit session: JdbcBackend#Session) =
      updateTokenCompiled(id).update(tokenValue) > 0

    def insert(userToInsert: UserRecord)(implicit session: JdbcBackend#Session) =
      insertUserCompiled.insert(userToInsert)
  }
}
