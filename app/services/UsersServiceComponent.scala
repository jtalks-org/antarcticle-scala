package services

import scalaz._
import Scalaz._
import repositories.UsersRepositoryComponent
import scalaz._
import models.database.UserRecord
import models.Page
import models.UserModels.UpdateUserRoleModel
import conf.Constants

trait UsersServiceComponent {
  val usersService: UsersService

  trait UsersService {
    def getByName(userName: String): Option[UserRecord]

    def getPage(page: Int, search: Option[String] = None): ValidationNel[String, Page[UserRecord]]

    def updateUserRole(user: UpdateUserRoleModel)
  }

}

trait UsersServiceComponentImpl extends UsersServiceComponent {
  this: SessionProvider with UsersRepositoryComponent =>

  val usersService = new UsersServiceImpl

  class UsersServiceImpl extends UsersService {

    override def getByName(userName: String) = withTransaction { implicit session =>
      usersRepository.getByUsername(userName)
    }

    override def getPage(page: Int, search: Option[String]) = withTransaction { implicit session =>
      val pageSize = Constants.PAGE_SIZE_USERS
      val offset = pageSize * (page - 1)
      val total = usersRepository.countFindUser(search.getOrElse(""))
      total match {
        case it if 1 until Page.getPageCount(it) + 1 contains page =>
          val modelsList = usersRepository.findUserPaged(search.getOrElse(""), offset, pageSize)
          Page(page, total, modelsList).successNel
        case _ => "No such page exists".failureNel
      }
    }

    override def updateUserRole(user: UpdateUserRoleModel) = withTransaction { implicit session =>
      usersRepository.updateUserRole(user.id, user.isAdmin)
    }
  }

}