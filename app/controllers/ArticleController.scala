package controllers

import play.api.mvc.{Security, Action, Controller}
import services.ArticlesServiceComponent
import play.api.data.Form
import play.api.data.Forms._
import models.ArticleModels.{Article, ArticleDetailsModel}
import views.html

/**
 * Serves web-based operations on articles
 * todo: handle 404
 */
trait ArticleController {
  this: Controller with ArticlesServiceComponent =>

  /**
   * Describes binding between Article model object and web-form
   */
  val articleForm = Form(
    mapping(
      "id" -> optional(number),
      "title" -> nonEmptyText,
      "content" -> nonEmptyText,
      "tags" -> text
    )((id, title, content, tags) => Article(id, title, content, tags.split(",")))
      ((article: Article) => Some((article.id, article.title, article.content, article.tags.mkString(","))))
  )

  def listAllArticles(page: Int = 0) = Action {
    implicit request => Ok(views.html.articles(articlesService.getPage(page)))
  }

  def viewArticle(id: Int) = Action {
    implicit request => Ok(views.html.article(articlesService.get(id).get))
  }

  def getNewArticlePage = Action {
    implicit request => Ok(views.html.createArticle(articleForm))
  }

  def postNewArticle = Action {
    implicit request => articleForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.createArticle(formWithErrors)),
      article => {
        val created = articlesService.createArticle(article)
        Redirect(routes.ArticleController.viewArticle(created.id))
      }
    )
  }

  def editArticle(id: Int = 0) = Action {
    implicit request => Ok(views.html.editArticle(articleForm.fill(articlesService.get(id).get)))
  }

  def postArticleEdits() = Action {
    implicit request => articleForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.editArticle(formWithErrors)),
      article => {
        articlesService.updateArticle(article)
        Redirect(routes.ArticleController.viewArticle(article.id.get))
      }
    )
  }

  def removeArticle(id: Int) = Action {
    implicit request => articlesService.removeArticle(id)
      Redirect(routes.ArticleController.listAllArticles())
  }
}
