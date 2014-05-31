package controllers

import play.api.mvc.{Action, Controller}
import services.{PropertiesServiceComponent, ArticlesServiceComponent, CommentsServiceComponent}
import play.api.data.Form
import play.api.data.Forms._
import security.Authentication
import security.Result._
import models.ArticleModels.ArticleDetailsModel
import scalaz._
import Scalaz._

/**
 * Handles all web operations related to article comments
 */
trait CommentController {
  this: Controller with CommentsServiceComponent with ArticlesServiceComponent with PropertiesServiceComponent with Authentication  =>

  /**
   * Describes binding between Article model object and web-form
   */
  val commentForm = Form(
    "content" -> nonEmptyText
  )

  def postNewComment(articleId: Int) = Action { implicit request =>
    def insertComment(comment: String) = {
      commentsService.insert(articleId, comment) match {
        case Authorized(created) =>
          Ok(routes.ArticleController.viewArticle(articleId).absoluteURL() + "#" + created.id.get)
        case NotAuthorized() =>
          Unauthorized("You are not authorized to create comments")
      }
    }

    commentForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.templates.formErrors(List("Comment should be non-empty"))),
      comment => insertComment(comment)
    )
  }

  def removeComment(articleId: Int, commentId: Int) = Action { implicit request =>
    def refreshPage = Ok(routes.ArticleController.viewArticle(articleId).absoluteURL())
    commentsService.removeComment(commentId).fold(
      fail = _ => refreshPage,
      succ = {
        case NotAuthorized() => Unauthorized("You are not authorized to remove this comment")
        case Authorized(_) => refreshPage
      }
    )
  }

  def editComment(articleId: Int, commentId: Int) = Action { implicit request =>
    val instanceName = propertiesService.getInstanceName()
    articlesService.get(articleId) match {
      case article : Some[ArticleDetailsModel] =>
        Ok(views.html.editComment(article.get, commentsService.getByArticle(articleId), commentId, instanceName))
      case None =>
        NotFound(views.html.errors.notFound(instanceName))
    }
  }

  def postCommentEdits(articleId: Int, commentId: Int) = Action { implicit request =>
    def updateComment(content: String) = {
      commentsService.update(commentId, content).fold(
        fail = nel => BadRequest(views.html.templates.formErrors(nel.list)),
        succ = {
          case NotAuthorized() => Unauthorized("You are not authorized to edit this comment")
          case Authorized(_) => Ok(routes.ArticleController.viewArticle(articleId).absoluteURL() + "#" + commentId)
        }
      )
    }

    commentForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.templates.formErrors(List("Comment should be non-empty"))),
      content => updateComment(content)
    )
  }
}
