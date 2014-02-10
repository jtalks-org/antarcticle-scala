package controllers

import play.api.mvc.{Action, Controller}
import services.{ArticlesServiceComponent, CommentsServiceComponent}
import play.api.data.Form
import play.api.data.Forms._
import security.Authentication

/**
 *
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
        commentsService.insert(articleId, comment).fold(
          fail = nel => {
            BadRequest(views.html.templates.formErrors(nel.list))
          },
          succ = created => Ok(routes.ArticleController.viewArticle(articleId).absoluteURL() + "#" + created.id.get)
        )

      }
    )
  }

  def removeComment(articleId: Int, commentId: Int) = Action { implicit request =>
    commentsService.removeComment(commentId)
    Ok(routes.ArticleController.viewArticle(articleId).absoluteURL())
  }

  def editComment(articleId: Int, commentId: Int) = Action { implicit request =>
    Ok(views.html.editComment(
      articlesService.get(articleId).get,
      commentsService.getByArticle(articleId),
      commentId))
  }

  def portCommentEdits(articleId: Int, commentId: Int) = Action { implicit request =>
    commentForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.templates.formErrors(List("Comment should be non-empty"))),
      content => {
        commentsService.update(commentId, content).fold(
          fail = nel => {
            BadRequest(views.html.templates.formErrors(nel.list))
          },
          succ = created => Ok(routes.ArticleController.viewArticle(articleId).absoluteURL() + "#" + commentId)
        )
      }
    )
  }
}
