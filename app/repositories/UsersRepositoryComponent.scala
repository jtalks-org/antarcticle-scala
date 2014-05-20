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

    //todo: wat? why do we need two of them?
    def getByUsername(username: String)(implicit session: JdbcBackend#Session): Option[UserRecord]

    def findByUserName(username: String)(implicit session: JdbcBackend#Session): List[UserRecord]

    def findUserPaged(username: String, offset: Int, portionSize: Int)(implicit session: JdbcBackend#Session): List[UserRecord]

    def countFindUser(username: String)(implicit session: JdbcBackend#Session): Int

    def insert(userToInert: UserRecord)(implicit session: JdbcBackend#Session): Int

    def updateRememberToken(id: Int, tokenValue: String)(implicit session: JdbcBackend#Session): Boolean

    def updateUserRole(id: Int, isAdmin: Boolean)(implicit session: JdbcBackend#Session): Boolean
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
    type SColumn = Column[String]

    def byId(id: Column[Int]): Query[Users, C] = {
      q.filter(_.id === id)
    }

    def byUsername(username: SColumn, f: SColumn => SColumn = col => col ): Query[Users, C] = {
      q.filter(user => f(user.username) === f(username))
    }

    def stringFieldsMatch(search: SColumn): Query[Users, C] = {
      q.filter(user => user.username.like(search) || user.firstName.like(search) || user.lastName.like(search))
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
    val userSearchCount = Compiled((search: Column[String]) => users.stringFieldsMatch(search).length)
    val updateTokenCompiled = Compiled((id: Column[Int]) => users.byId(id).map(_.rememberToken))
    val updateUserRoleCompiled = Compiled((id: Column[Int]) => users.byId(id).map(_.admin))
    val insertUserCompiled = users.returning(users.map(_.id)).insertInvoker

    def getByRememberToken(token: String)(implicit session: JdbcBackend#Session) =
      byTokenCompiled(token).firstOption

    def getByUsername(username: String)(implicit session: JdbcBackend#Session) =
      byUsernameCompiled(username).firstOption

    def findUserPaged(search: String, offset: Int, portionSize: Int)(implicit session: JdbcBackend#Session) =
      // todo: cannot be compiled: https://github.com/slick/slick/pull/764
      users.stringFieldsMatch(s"%$search%").drop(offset).take(portionSize).list

    def countFindUser(search: String)(implicit session: JdbcBackend#Session) = userSearchCount(s"%$search%").run

    def updateUserRole(id: Int, isAdmin: Boolean)(implicit session: JdbcBackend#Session) =
       updateUserRoleCompiled(id).update(isAdmin) > 0

    def updateRememberToken(id: Int, tokenValue: String)(implicit session: JdbcBackend#Session) =
      updateTokenCompiled(id).update(tokenValue) > 0

    def insert(userToInsert: UserRecord)(implicit session: JdbcBackend#Session) =
      insertUserCompiled.insert(userToInsert)

    def findByUserName(username: String)(implicit session: JdbcBackend#Session) =
      byUsernameIgnoreCaseCompiled(username).list
  }
}
