package controllers

import play.api.mvc.{Action, Controller}
import services.{CommentServiceComponent, ArticlesServiceComponent}
import play.api.data.Form
import play.api.data.Forms._
import views.html
import models.database.{CommentToInsert, CommentRecord}

/**
 *
 */
trait CommentController {
  this: Controller with CommentServiceComponent with ArticlesServiceComponent =>

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
      formWithErrors => BadRequest,
      comment => {
        commentService.insert(comment._1, comment._2)
        Ok
      }
    )
  }

  def removeComment(id: Int) = Action {
    implicit request => commentService.removeComment(id)
      Redirect(routes.ArticleController.listAllArticles())
  }
}
