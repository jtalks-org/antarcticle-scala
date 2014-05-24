package services

import scalaz._
import Scalaz._
import repositories.UsersRepositoryComponent
import scalaz._
import models.database.UserRecord
import models.{UserPage, Page}
import models.UserModels.UpdateUserRoleModel
import conf.Constants
import security.Permissions.{Create, Manage}
import security.Entities.Users
import security.{Entities, Principal}
import security.Result.AuthorizationResult

trait UsersServiceComponent {
  val usersService: UsersService

  trait UsersService {
    def getByName(userName: String): Option[UserRecord]

    def getPage(page: Int, search: Option[String] = None): ValidationNel[String, Page[UserRecord]]

    def updateUserRole(user: UpdateUserRoleModel)(implicit principal: Principal): AuthorizationResult[ValidationNel[String, Boolean]]
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
        case it if 1 until UserPage.getPageCount(it) + 1 contains page =>
          val modelsList = usersRepository.findUserPaged(search.getOrElse(""), offset, pageSize)
          new UserPage(page, total, modelsList).successNel
        case _ => "No such page exists".failureNel
      }
    }

    override def updateUserRole(user: UpdateUserRoleModel)(implicit principal: Principal) =
      principal.doAuthorizedOrFail(Manage, Entities.Users) { () =>
        withTransaction {
          implicit session =>
            usersRepository.updateUserRole(user.id, user.isAdmin).successNel
        }
      }
  }
}