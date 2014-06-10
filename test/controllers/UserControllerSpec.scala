package controllers

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import org.specs2.specification.AfterExample
import services.{ApplicationPropertiesServiceComponent, UsersServiceComponent, ArticlesServiceComponent}
import util.FakeAuthentication
import play.api.test._
import play.api.test.Helpers._
import models.{UserPage, ArticlePage}
import security._
import scalaz._
import Scalaz._
import security.AuthenticatedUser
import models.database.UserRecord
import models.ArticleModels.ArticleListModel
import scala.Some
import security.Authorities.Admin
import play.api.libs.json.Json
import org.mockito.Matchers

class UserControllerSpec extends Specification with Mockito with AfterExample {

  object controller extends UserController
                      with ArticlesServiceComponent
                      with UsersServiceComponent
                      with ApplicationPropertiesServiceComponent
                      with PropertiesProvider
                      with FakeAuthentication
                       {
    override val articlesService = mock[ArticlesService]
    override val usersService = mock[UsersService]
    override val usersRepository = mock[UsersRepository]
    override val propertiesService = mock[ApplicationPropertiesService]
  }

  import controller._

  def after = {
    org.mockito.Mockito.reset(articlesService)
    org.mockito.Mockito.reset(usersService)
    org.mockito.Mockito.reset(usersRepository)
  }

  val username = "user"
  val password = "password"
  val user = new UserRecord(Some(1), username, password, true)
  implicit def principal = {
    val usr = mock[AuthenticatedUser]
    usr.userId returns 1
    usr.username returns username
    usr.authority returns Admin
    usr.can(Permissions.Read, Entities.Article) returns true
    usr.can(Permissions.Manage, Entities.Users) returns true
    usr
  }

  "get user profile page" should {
    val articles = new ArticlePage(1, 0,List[ArticleListModel]()).successNel

    "fetch articles for profile owner" in {
      usersService.getByName(username) returns Some(user)
      articlesService.getPageForUser(1, username, None) returns articles

      val page = controller.viewProfilePaged(username, 1)(FakeRequest())

      status(page) must equalTo(200)
      contentType(page) must beSome("text/html")
      there was one(articlesService).getPageForUser(1, username, None)
    }

    "return 404 for missing user" in {
      usersService.getByName(username) returns None

      val page = controller.viewProfilePaged(username, 1)(FakeRequest())

      status(page) must equalTo(404)
      contentType(page) must beSome("text/html")
    }

    "return 404 for non-existing page" in {
      usersService.getByName(username) returns Some(user)
      articlesService.getPageForUser(1, username, None) returns "Not found".failureNel

      val page = controller.viewProfilePaged(username, 1)(FakeRequest())

      status(page) must equalTo(404)
      contentType(page) must beSome("text/html")
    }

    "support searching by tag" in {
      usersService.getByName(username) returns Some(user)
      articlesService.getPageForUser(1, username, Some("tag")) returns articles

      val page = controller.viewProfilePaged(username, 1, Some("tag"))(FakeRequest())

      status(page) must equalTo(200)
      contentType(page) must beSome("text/html")
      there was one(articlesService).getPageForUser(1, username, Some("tag"))
    }
  }

  "get user list" should {
    val users = new UserPage(1, 0,List[UserRecord]()).successNel
    val someName = Some(username)

    "list users paged" in new WithApplication {
      controller.setPrincipal(principal)
      usersService.getPage(1, someName) returns users

      val page = controller.listUsersPaged(someName, 1)(FakeRequest()).run

      status(page) must equalTo(200)
      contentType(page) must beSome("text/html")
    }

    "return 404 for missing page" in new WithApplication {
      controller.setPrincipal(principal)
      usersService.getPage(1, someName) returns "Not found".failureNel

      val page = controller.listUsersPaged(someName, 1)(FakeRequest()).run

      status(page) must equalTo(404)
      contentType(page) must beSome("text/html")
    }

    "redirect to login page for anonymous user" in new WithApplication {
      controller.setPrincipal(AnonymousPrincipal)

      val page = controller.listUsersPaged(someName, 1)(FakeRequest()).run

      status(page) must equalTo(303)
    }

    "show 403 page for insufficient permissions" in new WithApplication {
      controller.setPrincipal(AuthenticatedUser(1, "", Authorities.User))

      val page = controller.listUsersPaged(someName, 1)(FakeRequest()).run

      status(page) must equalTo(403)
      contentType(page) must beSome("text/html")
    }
  }

  "update user role" should {
    val authenticatedPrincipal = AuthenticatedUser(1, "", Authorities.User)

    "update requested role" in {
      controller.setPrincipal(authenticatedPrincipal)
      usersService.updateUserRole(any)(Matchers.eq(authenticatedPrincipal)) returns Result.Authorized(true.successNel)
      val request = new FakeRequest(Helpers.POST, "/", FakeHeaders(), Json.toJson(Map("role" -> "admin")))

      val page = controller.postChangedUserRole(1)(request)

      status(page) must equalTo(200)
    }

    "validate incoming data" in {
      controller.setPrincipal(AuthenticatedUser(1, "", Authorities.User))
      usersService.updateUserRole(any)(Matchers.eq(authenticatedPrincipal)) returns Result.Authorized(true.successNel)
      val request = new FakeRequest(Helpers.POST, "/", FakeHeaders(), Json.toJson(Map("lol" -> "wut")))

      val page = controller.postChangedUserRole(1)(request)

      status(page) must equalTo(400)
    }

    "check if role is valid" in {
      controller.setPrincipal(AuthenticatedUser(1, "", Authorities.User))
      usersService.updateUserRole(any)(Matchers.eq(authenticatedPrincipal)) returns Result.Authorized(true.successNel)
      val request = new FakeRequest(Helpers.POST, "/", FakeHeaders(), Json.toJson(Map("role" -> "stas")))

      val page = controller.postChangedUserRole(1)(request)

      status(page) must equalTo(400)
    }

    "reject submissions from anonymous" in {
      controller.setPrincipal(AnonymousPrincipal)
      usersService.updateUserRole(any)(Matchers.eq(AnonymousPrincipal)) returns Result.NotAuthorized()
      val request = new FakeRequest(Helpers.POST, "/", FakeHeaders(), Json.toJson(Map("role" -> "admin")))

      val page = controller.postChangedUserRole(1)(request)

      status(page) must equalTo(401)
    }

    "check user permissions" in {
      controller.setPrincipal(authenticatedPrincipal)
      usersService.updateUserRole(any)(Matchers.eq(authenticatedPrincipal)) returns Result.NotAuthorized()
      val request = new FakeRequest(Helpers.POST, "/", FakeHeaders(), Json.toJson(Map("role" -> "admin")))

      val page = controller.postChangedUserRole(1)(request)

      status(page) must equalTo(401)
    }
  }
}
