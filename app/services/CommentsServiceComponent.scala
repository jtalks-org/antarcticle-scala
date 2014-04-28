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
import security.Permissions._
import security.Result._

/**
 *  Performs transactional operations upon articles' comments
 */
trait CommentsServiceComponent {
  val commentsService: CommentsService

  trait CommentsService {
    def getAllNotReadComments(recipientUserId: Int)
    def getByArticle(id: Int): Seq[Comment]
    def insert(articleId: Int, content: String)(implicit principal: Principal): AuthorizationResult[CommentRecord]
    def update(id: Int, content: String)(implicit principal: Principal): ValidationNel[String, AuthorizationResult[Unit]]
    def removeComment(id: Int)(implicit principal: Principal): ValidationNel[String, AuthorizationResult[Unit]]
  }
}

trait CommentsServiceComponentImpl extends CommentsServiceComponent {
  this: SessionProvider with CommentsRepositoryComponent =>

  val commentsService = new CommentsServiceImpl

  class CommentsServiceImpl extends CommentsService {


    override def getAllNotReadComments(recipientUserId: Int) = withSession { implicit  session =>

    }

    def getByArticle(articleId: Int) = withSession { implicit session =>
      commentsRepository.getByArticle(articleId).map((toComment _).tupled)
    }

    def insert(articleId: Int, content: String)(implicit principal: Principal) =
      principal.doAuthorizedOrFail(Create, Entities.Comment) { () =>
        withTransaction { implicit session =>
          //TODO: avoid explicit cast. pass AuthenticatedUser as function param?
          val currentUserId = principal.asInstanceOf[AuthenticatedUser].userId
          val toInsert = CommentRecord(None, currentUserId, articleId, content, DateTime.now)
          val id = commentsRepository.insert(toInsert)
          toInsert.copy(id = Some(id))
        }
      }

    def update(id: Int, content: String)(implicit principal: Principal) = withTransaction { implicit session =>
      commentsRepository.get(id) match {
        case Some(comment) =>
          val toUpdate = CommentToUpdate(content, DateTime.now)
          principal.doAuthorizedOrFail(Update, comment){ () =>
            commentsRepository.update(id, toUpdate)
            ()
          }.successNel
        case None => "Comment not found".failureNel
      }
    }

    def removeComment(id: Int)(implicit principal: Principal) = withTransaction { implicit session =>
      commentsRepository.get(id) match {
        case Some(comment) =>
          principal.doAuthorizedOrFail(Delete, comment){ () =>
            commentsRepository.delete(id)
            ()
          }.successNel
        case None => "Comment not found".failureNel
      }
    }


    //TODO: remove UserRecord to UserModel duplication with article service
    private def toComment(record: CommentRecord, user: UserRecord) = {
      Comment(record.id.get, UserModel(user.id.get, user.username), record.articleId,
        record.content, record.createdAt, record.updatedAt)
    }
  }
}
