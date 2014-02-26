package controllers

import play.api.mvc.{Action, Controller}
import services.{ArticlesServiceComponent, CommentsServiceComponent}
import play.api.data.Form
import play.api.data.Forms._
import security.Authentication
import security.Result._
import models.database.CommentRecord
import models.ArticleModels.ArticleDetailsModel

/**
 * Handles all web operations related to article comments
 */
trait CommentController {
  this: Controller with CommentsServiceComponent with ArticlesServiceComponent with Authentication  =>

  /**
   * Describes binding between Article model object and web-form
   */
  val commentForm = Form(
    "content" -> nonEmptyText
  )

  def postNewComment(articleId: Int) = Action { implicit request =>
    commentForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.templates.formErrors(List("Comment should be non-empty"))),
      comment => {
        commentsService.insert(articleId, comment) match {
          case Authorized(created) =>
            Ok(routes.ArticleController.viewArticle(articleId).absoluteURL() + "#" + created.id.get)
          case NotAuthorized() =>
            Unauthorized("You are not authorized to create comments")
        }
      }
    )
  }

  def removeComment(articleId: Int, commentId: Int) = Action { implicit request =>
    commentsService.removeComment(commentId)
    Ok(routes.ArticleController.viewArticle(articleId).absoluteURL())
  }

  def editComment(articleId: Int, commentId: Int) = Action { implicit request =>
    articlesService.get(articleId) match {
      case article : Some[ArticleDetailsModel] =>
        Ok(views.html.editComment(article.get, commentsService.getByArticle(articleId), commentId))
      case None =>
        NotFound(views.html.errors.notFound())
    }
  }

  def postCommentEdits(articleId: Int, commentId: Int) = Action { implicit request =>
    commentForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.templates.formErrors(List("Comment should be non-empty"))),
      content => {
        commentsService.update(commentId, content).fold(
          fail = nel => {
            BadRequest(views.html.templates.formErrors(nel.list))
          },
          succ = edited => Ok(routes.ArticleController.viewArticle(articleId).absoluteURL() + "#" + commentId)
        )
      }
    )
  }
}
