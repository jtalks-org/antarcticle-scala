package services

import repositories.CommentsRepositoryComponent
import models.database._
import org.joda.time.DateTime
import utils.Implicits._
import scalaz._
import Scalaz._
import models.CommentModels.Comment
import models.UserModels.UserModel
import security.{Entities, AuthenticatedUser, Principal}
import security.Permissions.Create

/**
 *
 */
trait CommentsServiceComponent {
  val commentsService: CommentsService

  trait CommentsService {
    def getByArticle(id: Int): List[Comment]
    def insert(articleId: Int, content : String)(implicit principal: Principal): ValidationNel[String, CommentRecord]
    def update(id: Int, comment: CommentToUpdate): Boolean
    def removeComment(id: Int): Boolean
  }
}

trait CommentsServiceComponentImpl extends CommentsServiceComponent {
  this: SessionProvider with CommentsRepositoryComponent =>

  val commentsService = new CommentsServiceImpl

  class CommentsServiceImpl extends CommentsService {

    def getByArticle(articleId: Int) = withSession { implicit session =>
      commentsRepository.getByArticle(articleId).map((toComment _).tupled)
    }

    def insert(articleId: Int, content : String) (implicit principal: Principal) = principal match {
      case currentUser: AuthenticatedUser if currentUser.can(Create, Entities.Comment) =>
        withTransaction { implicit session =>
        val toInsert = CommentRecord(None, currentUser.userId, articleId, content, DateTime.now)
        val id = commentsRepository.insert(toInsert)
        toInsert.copy(id = Some(id)).successNel
      }
      case _ => "Authorization failure".failureNel
    }

    def update(id: Int, comment: CommentToUpdate) = withTransaction { implicit session =>
      commentsRepository.update(id, comment)
    }

    def removeComment(id: Int) = withTransaction { implicit session =>
      commentsRepository.delete(id)
    }

    //TODO: remove UserRecord to UserModel duplication with article service
    private def toComment(record: CommentRecord, user: UserRecord) = {
      Comment(record.id.get, UserModel(user.id.get, user.username), record.articleId,
        record.content, record.createdAt, record.updatedAt)
    }
  }
}
