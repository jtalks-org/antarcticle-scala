package controllers

import play.api.mvc.{Action, Controller}
import services.CommentsServiceComponent
import play.api.data.Form
import play.api.data.Forms._
import security.Authentication

/**
 *
 */
trait CommentController {
  this: Controller with CommentsServiceComponent with Authentication  =>

  /**
   * Describes binding between Article model object and web-form
   */
  val commentForm = Form(
    tuple(
      "articleId" -> number,
      "content" -> nonEmptyText
    )
  )

  def postNewComment = Action { implicit request =>
    commentForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.templates.formErrors(List("Comment should be non-empty"))),
      comment => {
        commentsService.insert(comment._1, comment._2).fold(
          fail = nel => {
            BadRequest(views.html.templates.formErrors(nel.list))
          },
          succ = created => Ok(routes.ArticleController.viewArticle(comment._1).absoluteURL())
        )

      }
    )
  }

  def removeComment(id: Int) = Action { implicit request =>
    commentsService.removeComment(id)
    Redirect(routes.ArticleController.listAllArticles())
  }
}
