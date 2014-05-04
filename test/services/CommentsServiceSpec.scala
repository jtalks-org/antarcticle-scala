package services

import org.specs2.mutable.Specification
import models.database._
import utils.Implicits._
import org.specs2.time.NoTimeConversions
import util.FakeSessionProvider
import util.FakeSessionProvider.FakeSessionValue
import com.github.nscala_time.time.Imports._
import org.specs2.mock.Mockito
import org.mockito.Matchers
import util.TimeFridge
import org.specs2.specification.BeforeExample
import scalaz._
import Scalaz._
import repositories.CommentsRepositoryComponent
import org.specs2.scalaz.ValidationMatchers
import models.CommentModels.Comment
import security._
import security.Result._

class CommentsServiceSpec extends Specification
  with NoTimeConversions with Mockito with BeforeExample
  with ValidationMatchers {

  object service extends CommentsServiceComponentImpl
    with CommentsRepositoryComponent
    with NotificationsServiceComponent
    with FakeSessionProvider {
    override val commentsRepository = mock[CommentsRepository]
    override val notificationsService = mock[NotificationsService]
  }

  import service._

  def before = {
    org.mockito.Mockito.reset(commentsRepository)
    org.mockito.Mockito.reset(notificationsService)
  }

  val userRecord = UserRecord(1.some, "user1", "password1")
  val commentRecord = CommentRecord(99.some, userRecord.id.get, 1, "x", DateTime.now)

  "get comments for article" should {
    "return comments list" in {
      val comments = Seq((commentRecord, userRecord),
                         (commentRecord.copy(id = 12.some), userRecord))
      commentsRepository.getByArticle(commentRecord.articleId)(FakeSessionValue) returns comments

      val list: Seq[Comment] = commentsService.getByArticle(commentRecord.articleId)

      list must have size 2
    }
  }

  "creating new comment" should {
    implicit def getCurrentUser = {
      val usr = spy(AuthenticatedUser(1, "username", Authorities.User))
      org.mockito.Mockito.doReturn(true)
        .when(usr)
        .can(Matchers.eq(Permissions.Create), Matchers.eq(Entities.Comment))
      usr
    }
    val articleId = 2
    val content = "3n4hbi4ho45y"

    "insert comment in repository" in {
      TimeFridge.withFrozenTime() { dt =>
        val commentRecord = CommentRecord(None, getCurrentUser.userId,
          articleId, content, dt)

        commentsService.insert(articleId, content)

        there was one(commentsRepository).insert(commentRecord)(FakeSessionValue)
      }
    }

    "assign current user as author" in {
      commentsService.insert(articleId, content) match {
        case Authorized(record) => record.userId must_== getCurrentUser.userId
        case _ => ko
      }
    }

    "assign correct article" in {
      commentsService.insert(articleId, content) match {
        case Authorized(record) => record.articleId must_== articleId
        case _ => ko
      }
    }

    "return model with assigned id" in {
      val assignedId = 1
      commentsRepository.insert(any[CommentRecord])(Matchers.eq(FakeSessionValue)) returns assignedId

      commentsService.insert(articleId, content) match {
        case Authorized(record) => record.id must beSome(assignedId)
        case _ => ko
      }
    }

    "fail when user is not authorized to do it" in {
      val currentUser = spy(AuthenticatedUser(1, "username", Authorities.User))
      org.mockito.Mockito.doReturn(false)
        .when(currentUser)
        .can(Matchers.eq(Permissions.Create), Matchers.eq(Entities.Comment))

      commentsService.insert(articleId, content)(currentUser) must beLike {
        case NotAuthorized() => ok
        case _ => ko
      }
    }
  }

  "updating comment" should {
    val commentId = commentRecord.id.get
    val content = "3n4hbi4ho45y"
    implicit def getCurrentUser = {
      val usr = spy(AuthenticatedUser(1, "username", Authorities.User))
      org.mockito.Mockito.doReturn(true)
        .when(usr)
        .can(Permissions.Update, commentRecord)
      usr
    }

    "update comment in repository" in {
      commentsRepository.get(commentId)(FakeSessionValue) returns commentRecord.some

      commentsService.update(commentId, content)

      there was one(commentsRepository).update(Matchers.eq(commentId),
        any[CommentToUpdate])(Matchers.eq(FakeSessionValue))
    }

    "update modification time to now" in {
      commentsRepository.get(commentId)(FakeSessionValue) returns commentRecord.some
      TimeFridge.withFrozenTime() { dt =>
        commentsService.update(commentId, content)

        there was one(commentsRepository).update(commentId,
          CommentToUpdate(content, dt))(FakeSessionValue)
      }
    }

    "be successful" in {
      commentsRepository.get(commentId)(FakeSessionValue) returns commentRecord.some
      commentsRepository.update(anyInt, any[CommentToUpdate])(Matchers.eq(FakeSessionValue)) returns true

      commentsService.update(commentId, content) must beSuccessful
    }

    "fail when comment not found" in {
      commentsRepository.get(commentId)(FakeSessionValue) returns None

      commentsService.update(commentId, content) must beFailing
    }

    "fail authorization when user is not authorized to do it" in {
      val currentUser = mock[AuthenticatedUser]
      currentUser.doAuthorizedOrFail(Matchers.eq(Permissions.Update),
        Matchers.eq(commentRecord))(any[Function0[Unit]]) returns NotAuthorized[Unit]()
      commentsRepository.get(commentId)(FakeSessionValue) returns commentRecord.some

      commentsService.update(commentId, content)(currentUser) must beSuccessful.like {
        case NotAuthorized() => ok
      }
    }
  }

  "comment removal" should {
    implicit def getCurrentUser = {
      val usr = spy(AuthenticatedUser(1, "username", Authorities.User))
      org.mockito.Mockito.doReturn(true)
        .when(usr)
        .can(Matchers.eq(Permissions.Delete), Matchers.eq(commentRecord))
      usr
    }
    val commentId = commentRecord.id.get

    "fail authorization when user is not authorized to do it" in {
      val currentUser = mock[AuthenticatedUser]
      currentUser.doAuthorizedOrFail(Matchers.eq(Permissions.Delete),
        Matchers.eq(commentRecord))(any[Function0[Unit]]) returns NotAuthorized[Unit]()
      commentsRepository.get(commentId)(FakeSessionValue) returns commentRecord.some

      commentsService.removeComment(commentId)(currentUser) must beSuccessful.like {
        case NotAuthorized() => ok
      }
    }

    "fail when comment not exists" in {
      commentsRepository.get(commentId)(FakeSessionValue) returns None

      commentsService.removeComment(commentId) must beFailing
    }

    "remove comment from repository" in {
      commentsRepository.get(commentId)(FakeSessionValue) returns commentRecord.some

      commentsService.removeComment(commentId)

      there was one(commentsRepository).delete(commentId)(FakeSessionValue)
    }
  }
}
