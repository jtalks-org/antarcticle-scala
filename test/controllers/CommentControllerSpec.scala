package controllers

import org.specs2.specification.AfterExample
import org.specs2.mutable.Specification
import services.{ArticlesServiceComponent, CommentsServiceComponent}
import util.FakeAuthentication
import org.specs2.mock.Mockito
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scalaz._
import Scalaz._
import security.AnonymousPrincipal
import models.database.CommentRecord
import models.database.CommentRecord
import scala.Some


class CommentControllerSpec extends Specification with Mockito with AfterExample {

  object controller extends CommentController
                      with CommentsServiceComponent
                      with ArticlesServiceComponent
                      with FakeAuthentication{
    override val usersRepository = mock[UsersRepository]
    override val commentsService = mock[CommentsService]
    override val articlesService = mock[ArticlesService]
  }

  import controller._

  def after = {
    org.mockito.Mockito.reset(usersRepository)
    org.mockito.Mockito.reset(commentsService)
    org.mockito.Mockito.reset(articlesService)
  }

  val comment = "content"
  val request = FakeRequest("POST","/").withFormUrlEncodedBody(("content", comment))
  implicit def principal = AnonymousPrincipal

  "post new comment" should {

    val articleId = 1
    val commentRecord = new CommentRecord(Some(1), 1, 1, comment, null)

    "create a comment from valid data" in {
      commentsService.insert(articleId, comment) returns commentRecord.successNel

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

    "handle service layer failures" in {
      commentsService.insert(articleId, comment) returns "service failure".failureNel

      val page = controller.postNewComment(articleId)(request)

      status(page) must equalTo(400)
      contentType(page) must beSome("text/html")
    }
  }
}
