package services

import scalaz._
import repositories.UsersRepositoryComponent
import models.database.UserRecord

trait UsersServiceComponent {
  val usersService: UsersService

  trait UsersService {
    def getByName(userName: String): Option[UserRecord]
  }
}

trait UsersServiceComponentImpl extends UsersServiceComponent {
  this: SessionProvider with UsersRepositoryComponent =>

  val usersService = new UsersServiceImpl

  class UsersServiceImpl extends UsersService {

    override def getByName(userName: String) = withTransaction { implicit session =>
      usersRepository.getByUsername(userName)
    }
  }
}