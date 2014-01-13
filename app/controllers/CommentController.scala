package controllers

import play.api.mvc.{Action, Controller}
import services.{CommentServiceComponent, ArticlesServiceComponent}
import play.api.data.Form
import play.api.data.Forms._
import views.html
import models.database.CommentRecord
import security.Authentication

/**
 *
 */
trait CommentController {
  this: Controller with CommentServiceComponent with ArticlesServiceComponent with Authentication  =>

  /**
   * Describes binding between Article model object and web-form
   */
  val commentForm = Form(
    tuple(
      "articleId" -> number,
      "content" -> nonEmptyText
    )
  )

  def postNewComment = Action {
    implicit request => commentForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.templates.formErrors(List("Comment should be non-empty"))),
      comment => {
        commentService.insert(comment._1, comment._2)
        Ok(routes.ArticleController.viewArticle(comment._1).absoluteURL())
      }
    )
  }

  def removeComment(id: Int) = Action {
    implicit request => commentService.removeComment(id)
      Redirect(routes.ArticleController.listAllArticles())
  }
}
