package controllers

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.AfterExample
import services.{UsersServiceComponent, ArticlesServiceComponent}
import util.FakeAuthentication
import play.api.test._
import play.api.test.Helpers._
import models.database.UserRecord
import models.Page
import security.{Entities, Permissions, AuthenticatedUser}
import scalaz._
import Scalaz._
import models.ArticleModels.ArticleListModel

class UserControllerSpec extends Specification with Mockito with AfterExample {

  object controller extends UserController
                      with ArticlesServiceComponent
                      with UsersServiceComponent
                      with FakeAuthentication {
    override val articlesService = mock[ArticlesService]
    override val usersService = mock[UsersService]
    override val usersRepository = mock[UsersRepository]
  }

  import controller._

  def after = {
    org.mockito.Mockito.reset(articlesService)
    org.mockito.Mockito.reset(usersService)
    org.mockito.Mockito.reset(usersRepository)
  }

  "get user profile page" should {

    val username = "user"
    val user = new UserRecord(Some(1), username, true)
    val articles = Page(1,0,List[ArticleListModel]()).successNel
    implicit def principal = {
      val usr = mock[AuthenticatedUser]
      usr.userId returns 1
      usr.username returns username
      usr.can(Permissions.Read, Entities.Article) returns true
      usr
    }

    "fetch articles for profile owner" in {
      usersService.getByName(username) returns Some(user)
      articlesService.getPageForUser(1, username, None) returns articles

      val page = controller.viewProfile(username, 1)(FakeRequest())

      status(page) must equalTo(200)
      contentType(page) must beSome("text/html")
      there was one(articlesService).getPageForUser(1, username, None)
    }

    "return 404 for missing user" in {
      usersService.getByName(username) returns None

      val page = controller.viewProfile(username, 1)(FakeRequest())

      status(page) must equalTo(404)
      contentType(page) must beSome("text/html")
    }

    "support searching by tag" in {
      usersService.getByName(username) returns Some(user)
      articlesService.getPageForUser(1, username, Some("tag")) returns articles

      val page = controller.viewProfile(username, 1, Some("tag"))(FakeRequest())

      status(page) must equalTo(200)
      contentType(page) must beSome("text/html")
      there was one(articlesService).getPageForUser(1, username, Some("tag"))
    }
  }
}
