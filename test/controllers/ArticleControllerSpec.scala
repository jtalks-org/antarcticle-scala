package controllers

import org.specs2.mutable.Specification
import org.specs2.mock.Mockito
import services.{CommentsServiceComponent, ArticlesServiceComponent}
import util.FakeAuthentication
import org.specs2.specification.AfterExample
import com.github.nscala_time.time.Imports._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import models.UserModels.UserModel
import models.ArticleModels.{ArticleDetailsModel, ArticleListModel}
import models.Page
import scala.Some
import scalaz._
import Scalaz._
import security.AnonymousPrincipal

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
  val articleListModel = new ArticleListModel(1, "title", "description", now, userModel, Seq())
  val articleDetailsModel = new ArticleDetailsModel(1, "title", "content", now, userModel, Seq())
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

  "get article" should {
    "fetch an existing article" in {
      articlesService.get(1) returns Some(articleDetailsModel)
      commentsService.getByArticle(1) returns List()

      val page = controller.viewArticle(1)(FakeRequest())

      status(page) must equalTo(200)
      contentType(page) must beSome("text/html")
      contentAsString(page).contains(articleDetailsModel.title) must beTrue
      contentAsString(page).contains(articleDetailsModel.content) must beTrue
      there was one(articlesService).get(1)
    }

    "show error page for illegal article id" in {
      articlesService.get(1) returns None

      val page = controller.viewArticle(1)(FakeRequest())

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
      articlesService.createArticle(article) returns articleDetailsModel.successNel

      val page = controller.postNewArticle(validRequest)

      status(page) must equalTo(200)
      contentType(page) must beSome("text/plain")
      there was one(articlesService).createArticle(article)
    }

    "report an error on bad request" in {
      val page = controller.postNewArticle(badRequest)

      status(page) must equalTo(400)
      contentType(page) must beSome("text/html")
    }

    "report error list on service operation error" in {
      articlesService.createArticle(article) returns "bad request".failureNel

      val page = controller.postNewArticle(validRequest)

      status(page) must equalTo(400)
      contentType(page) must beSome("text/html")
    }
  }
}
