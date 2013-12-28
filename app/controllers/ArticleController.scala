package controllers

import play.api.mvc.{Security, Action, Controller}
import services.ArticlesServiceComponent
import play.api.data.Form
import play.api.data.Forms._
import models.ArticleModels.ArticleDetailsModel

/**
 *
 */
trait ArticleController {
  this: Controller with ArticlesServiceComponent =>

  val articleForm = Form(
    tuple(
      "title" -> text,
      "text" -> text
    )
  )

  def listAllArticles(page: Int = 0) = Action {
    implicit request => Ok(views.html.articles(articlesService.getPage(page)))
  }

  def viewArticle(id: Int) = Action {
    implicit request => Ok(views.html.article(articlesService.get(id).get)) // todo: handle 404
  }

  def getNewArticlePage = Action {
    implicit request => Ok(views.html.articleEditor(articleForm))
  }

  def postNewArticle = TODO

  def editArticle(id: Int = 0) = TODO

  def postArticleEdits(id: Int = 0) = TODO

  def removeArticle(id: Int) = TODO
}
