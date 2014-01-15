package controllers

import play.api.mvc.{Action, Controller}
import services.{CommentsServiceComponent, ArticlesServiceComponent}
import play.api.data.Form
import play.api.data.Forms._
import models.ArticleModels.Article
import views.html
import security.Authentication

/**
 * Serves web-based operations on articles
 * todo: handle 404
 */
trait ArticleController {
  this: Controller with ArticlesServiceComponent with CommentsServiceComponent with Authentication =>

  /**
   * Describes binding between Article model object and web-form
   */
  val articleForm = Form(
    mapping(
      "id" -> optional(number),
      "title" -> text,
      "content" -> text,
      "tags" -> text
    )((id, title, content, tags) => Article(id, title, content, tags.split(",").filter(!_.isEmpty)))
      ((article: Article) => Some((article.id, article.title, article.content, article.tags.mkString(","))))
  )

  def listAllArticles(page: Int = 0) = Action { implicit request =>
    Ok(views.html.articles(articlesService.getPage(page)))
  }

  def viewArticle(id: Int) = Action { implicit request =>
    Ok(views.html.article(articlesService.get(id).get, commentsService.getByArticle(id)))
  }

  def getNewArticlePage = Action { implicit request =>
    Ok(views.html.createArticle(articleForm))
  }

  def postNewArticle = Action { implicit request =>
      articleForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.templates.formErrors(List("Invalid request"))),
        article => {
          articlesService.createArticle(article).fold(
            fail = nel => {
              BadRequest(views.html.templates.formErrors(nel.list))
            },
            succ = created => Ok(routes.ArticleController.viewArticle(created.id).absoluteURL())
          )
        }
      )
  }

  def editArticle(id: Int = 0) = Action { implicit request =>
    Ok(views.html.editArticle(articleForm.fill(articlesService.get(id).get)))
  }

  def postArticleEdits() = Action { implicit request =>
      articleForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.editArticle(formWithErrors)),
        article => {
          articlesService.updateArticle(article).fold(
            fail = nel => {
              BadRequest(views.html.templates.formErrors(nel.list))
            },
            succ = created => Ok(routes.ArticleController.viewArticle(created.id.get).absoluteURL())
          )
        }
      )
  }

  def removeArticle(id: Int) = Action { implicit request =>
      articlesService.removeArticle(id)
      Redirect(routes.ArticleController.listAllArticles())
  }
}
