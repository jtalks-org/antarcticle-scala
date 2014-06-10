package controllers

import org.specs2.specification.AfterExample
import org.specs2.mutable.Specification
import services.{ApplicationPropertiesServiceComponent, ArticlesServiceComponent, CommentsServiceComponent}
import util.FakeAuthentication
import org.specs2.mock.Mockito
import play.api.test.{WithApplication, FakeRequest}
import play.api.test.Helpers._
import scalaz._
import Scalaz._
import security.{AuthenticatedUser, AnonymousPrincipal}
import models.database.CommentRecord
import scala.Some
import org.joda.time.DateTime
import models.UserModels.UserModel
import models.ArticleModels.ArticleDetailsModel
import security.Result._


class CommentControllerSpec extends Specification with Mockito with AfterExample {

  object controller extends CommentController
                      with CommentsServiceComponent
                      with ArticlesServiceComponent
                      with ApplicationPropertiesServiceComponent
                      with FakeAuthentication
                      with PropertiesProvider {
    override val usersRepository = mock[UsersRepository]
    override val commentsService = mock[CommentsService]
    override val articlesService = mock[ArticlesService]
    override val propertiesService = mock[ApplicationPropertiesService]
  }

  import controller._

  def after = {
    org.mockito.Mockito.reset(usersRepository)
    org.mockito.Mockito.reset(commentsService)
    org.mockito.Mockito.reset(articlesService)
    controller.reset()
  }

  val comment = "content"
  val request = FakeRequest("POST","/").withFormUrlEncodedBody(("content", comment))
  val commentId = 1
  val articleId = 2
  implicit def principal = AnonymousPrincipal

  "post new comment" should {

    val commentRecord = new CommentRecord(Some(1), 1, 1, comment, null)

    "create a comment from valid data" in {
      commentsService.insert(articleId, comment) returns Authorized(commentRecord)

      val page = controller.postNewComment(articleId)(request)

      status(page) must equalTo(200)
      contentType(page) must beSome("text/plain")
      there was one(commentsService).insert(articleId, comment)
    }

    "report validation errors" in {
      val page = controller.postNewComment(articleId)(FakeRequest("POST","/"))

      status(page) must equalTo(400)
      contentType(page) must beSome("text/html")
      there was no(commentsService).insert(articleId, comment)
    }

    "return Unauthorized on authorization failure" in {
      commentsService.insert(articleId, comment) returns NotAuthorized()

      val page = controller.postNewComment(articleId)(request)

      status(page) must equalTo(401)
    }
  }

  "remove comment" should {

    "delete requested comment" in {
      commentsService.removeComment(commentId) returns Authorized(()).successNel

      val page = controller.removeComment(articleId,commentId)(FakeRequest("DELETE", "/"))

      status(page) must equalTo(200)
      contentType(page) must beSome("text/plain")
      there was one(commentsService).removeComment(commentId)
    }

    "ignore already deleted comment" in {
      commentsService.removeComment(commentId) returns "Comment not found".failureNel

      val page = controller.removeComment(articleId ,commentId)(FakeRequest("DELETE", "/"))

      status(page) must equalTo(200)
      contentType(page) must beSome("text/plain")
      there was one(commentsService).removeComment(commentId)
    }

    "return Unauthorized on authorization failure" in {
      commentsService.removeComment(commentId) returns NotAuthorized().successNel

      val page = controller.removeComment(articleId,commentId)(FakeRequest("DELETE", "/"))

      status(page) must equalTo(401)
    }
  }

  "edit comment page" should {

    val now = DateTime.now.toDate
    val userModel = new UserModel(1, "name")
    val articleDetailsModel = new ArticleDetailsModel(articleId, "title", "content", now, userModel, Seq())

    "have article content and comments" in new WithApplication  {
      controller.setPrincipal(new AuthenticatedUser(1,"", null))
      articlesService.get(articleId) returns Some(articleDetailsModel)
      commentsService.getByArticle(articleId) returns List()

      val page = controller.editComment(articleId, commentId)(FakeRequest()).run

      status(page) must equalTo(200)
      contentType(page) must beSome("text/html")
    }

    "return 404 if no comment found for edit" in new WithApplication {
      controller.setPrincipal(new AuthenticatedUser(1,"", null))
      articlesService.get(articleId) returns None

      val page = controller.editComment(articleId, commentId)(FakeRequest()).run

      status(page) must equalTo(404)
      contentType(page) must beSome("text/html")
    }

    "redirect non-authenticated user to login page" in new WithApplication {
      val page = controller.editComment(articleId, commentId)(FakeRequest()).run

      status(page) must equalTo(303)
    }
  }

  "post comment edit" should {

    "update a comment from valid data" in {
      commentsService.update(commentId, comment) returns Authorized(()).successNel

      val page = controller.postCommentEdits(articleId, commentId)(request)

      status(page) must equalTo(200)
      contentType(page) must beSome("text/plain")
      there was one(commentsService).update(commentId, comment)
    }

    "report validation errors" in {
      val page = controller.postCommentEdits(articleId, commentId)(FakeRequest("POST","/"))

      status(page) must equalTo(400)
      contentType(page) must beSome("text/html")
      there was no(commentsService).update(commentId, comment)
    }

    "handle service layer failures" in {
      commentsService.update(commentId, comment) returns "service failure".failureNel

      val page = controller.postCommentEdits(articleId, commentId)(request)

      status(page) must equalTo(400)
      contentType(page) must beSome("text/html")
    }

    "return Unauthorized on authorization failure" in {
      commentsService.update(commentId, comment) returns NotAuthorized().successNel

      val page = controller.postCommentEdits(articleId, commentId)(request)

      status(page) must equalTo(401)
    }
  }
}
