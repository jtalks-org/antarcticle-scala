package controllers

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import services.{CommentsServiceComponent, ArticlesServiceComponent}
import util.FakeAuthentication
import org.specs2.specification.AfterExample
import com.github.nscala_time.time.Imports._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scalaz._
import Scalaz._
import security.AnonymousPrincipal
import security.Result._
import models.Page
import scala.Some
import models.UserModels.UserModel
import models.ArticleModels.ArticleDetailsModel
import models.ArticleModels.ArticleListModel

class ArticleControllerSpec extends Specification with Mockito with AfterExample{

  object controller extends ArticleController
                      with ArticlesServiceComponent
                      with CommentsServiceComponent
                      with FakeAuthentication{
     override val articlesService = mock[ArticlesService]
     override val usersRepository = mock[UsersRepository]
     override val commentsService = mock[CommentsService]
   }

  import controller._

  def after = {
    org.mockito.Mockito.reset(articlesService)
    org.mockito.Mockito.reset(usersRepository)
    org.mockito.Mockito.reset(commentsService)
  }

  val now = DateTime.now.toDate
  val userModel = new UserModel(1, "name")
  val articleId = 1
  val articleListModel = new ArticleListModel(articleId, "title", "description", now, userModel, Seq())
  val articleDetailsModel = new ArticleDetailsModel(articleId, "title", "content", now, userModel, Seq())
  implicit def principal = AnonymousPrincipal

  "list all articles" should {
    "return a page with articles" in {
      articlesService.getPage(2, null) returns new Page(2,1, Seq(articleListModel))

      val page = controller.listAllArticles(2)(FakeRequest())

      status(page) must equalTo(200)
      contentType(page) must beSome("text/html")
      contentAsString(page).contains(articleListModel.title) must beTrue
      contentAsString(page).contains(articleListModel.description) must beTrue
      there was one(articlesService).getPage(2, null)
    }
  }

  "view article" should {
    "fetch an existing article" in {
      articlesService.get(articleId) returns Some(articleDetailsModel)
      commentsService.getByArticle(articleId) returns List()

      val page = controller.viewArticle(articleId)(FakeRequest())

      status(page) must equalTo(200)
      contentType(page) must beSome("text/html")
      contentAsString(page).contains(articleDetailsModel.title) must beTrue
      contentAsString(page).contains(articleDetailsModel.content) must beTrue
      there was one(articlesService).get(articleId)
    }

    "show error page for illegal article id" in {
      articlesService.get(articleId) returns None

      val page = controller.viewArticle(articleId)(FakeRequest())

      status(page) must equalTo(404)
      contentType(page) must beSome("text/html")
    }
  }

  "get new article page" should {
    "return new article form page" in {
      val page = controller.getNewArticlePage(FakeRequest())

      status(page) must equalTo(200)
      contentType(page) must beSome("text/html")
    }
  }

  "post new article" should {

    val validRequest = FakeRequest("POST","/")
      .withFormUrlEncodedBody(("title", "title"), ("content", "content"), ("tags","tag, oneMoreTag"))
    val badRequest = FakeRequest("POST","/")
      .withFormUrlEncodedBody(("content", ""), ("tags","#$%^&"))
    val article = controller.articleForm.bindFromRequest()(validRequest).get

    "save new article if data is valid" in {
      articlesService.insert(article) returns Authorized(articleDetailsModel.successNel)

      val page = controller.postNewArticle(validRequest)

      status(page) must equalTo(200)
      contentType(page) must beSome("text/plain")
      there was one(articlesService).insert(article)
    }

    "report an error on bad request" in {
      val page = controller.postNewArticle(badRequest)

      status(page) must equalTo(400)
      contentType(page) must beSome("text/html")
    }

    "report error list on service operation error" in {
      articlesService.insert(article) returns Authorized("bad request".failureNel)

      val page = controller.postNewArticle(validRequest)

      status(page) must equalTo(400)
      contentType(page) must beSome("text/html")
    }

    "return Unauthorized on authorization failure" in {
      articlesService.insert(article) returns NotAuthorized()

      val page = controller.postNewArticle(validRequest)

      status(page) must equalTo(401)
    }
  }

  "get edit article page" should {
    "fetch an existing article" in {
      articlesService.get(articleId) returns Some(articleDetailsModel)

      val page = controller.editArticle(articleId)(FakeRequest())

      status(page) must equalTo(200)
      contentType(page) must beSome("text/html")
      contentAsString(page).contains(articleDetailsModel.title) must beTrue
      contentAsString(page).contains(articleDetailsModel.content) must beTrue
      there was one(articlesService).get(articleId)
    }

    "show error page for illegal article id" in {
      articlesService.get(articleId) returns None

      val page = controller.editArticle(1)(FakeRequest())

      status(page) must equalTo(404)
      contentType(page) must beSome("text/html")
    }
  }

  "post article edit" should {

    val validRequest = FakeRequest("POST","/")
      .withFormUrlEncodedBody(("id", "" + articleId),("title", "title"), ("content", "content"), ("tags","tag, oneMoreTag"))
    val badRequest = FakeRequest("POST","/")
      .withFormUrlEncodedBody(("content", ""), ("tags","#$%^&"))
    val article = controller.articleForm.bindFromRequest()(validRequest).get

    "save an article if data is valid" in {
      articlesService.updateArticle(article) returns Authorized(().successNel).successNel

      val page = controller.postArticleEdits()(validRequest)

      status(page) must equalTo(200)
      contentType(page) must beSome("text/plain")
      there was one(articlesService).updateArticle(article)
    }

    "report an error on bad request" in {
      val page = controller.postArticleEdits()(badRequest)

      status(page) must equalTo(400)
    }

    "report error list on service operation error" in {
      articlesService.updateArticle(article) returns "bad request".failureNel

      val page = controller.postArticleEdits()(validRequest)

      status(page) must equalTo(400)
      contentType(page) must beSome("text/html")
    }

    //TODO: get rid of this ugly nesting
    "report error list on service operation error 2" in {
      articlesService.updateArticle(article) returns Authorized("bad request".failureNel).successNel

      val page = controller.postArticleEdits()(validRequest)

      status(page) must equalTo(400)
      contentType(page) must beSome("text/html")
    }

    "return Unauthorized on authorization failure" in {
      articlesService.updateArticle(article) returns NotAuthorized().successNel

      val page = controller.postArticleEdits()(validRequest)

      status(page) must equalTo(401)
    }
  }

  "remove article" should {

    "delete requested article" in {
      articlesService.removeArticle(articleId) returns true.successNel

      val page = controller.removeArticle(articleId)(FakeRequest("DELETE", "/"))

      status(page) must equalTo(200)
      contentType(page) must beSome("text/plain")
      there was one(articlesService).removeArticle(articleId)
    }

    "report service failures" in {
      articlesService.removeArticle(articleId) returns "authorization failure".failureNel

      val page = controller.removeArticle(articleId)(FakeRequest("DELETE", "/"))

      status(page) must equalTo(400)
      contentType(page) must beSome("text/html")
      there was one(articlesService).removeArticle(articleId)
    }
  }
}
