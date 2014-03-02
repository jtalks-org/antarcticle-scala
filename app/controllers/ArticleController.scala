package controllers

import play.api.mvc.{Action, Controller}
import services.{CommentsServiceComponent, ArticlesServiceComponent}
import play.api.data.Form
import play.api.data.Forms._
import models.ArticleModels.{ArticleDetailsModel, Article}
import security.Authentication
import security.Result._

/**
 * Serves web-based operations on articles
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

  val tagSearchForm = Form(
    mapping(
      "tag" ->  text
    )((tag)=>tag)((tag)=>Some(tag))
  )

  def listAllArticles(page: Int) = Action { implicit request =>
    Ok(views.html.articles(articlesService.getPage(page)))
  }

  def viewArticle(id: Int) = Action { implicit request =>
    articlesService.get(id) match {
      case Some(article : ArticleDetailsModel) => Ok(views.html.article(article, commentsService.getByArticle(id)))
      case _ => NotFound(views.html.errors.notFound())
    }
  }

  def getNewArticlePage = Action { implicit request =>
    Ok(views.html.createArticle(articleForm))
  }

  def postNewArticle = Action { implicit request =>
      articleForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.templates.formErrors(List("Invalid request"))),
        article => {
          articlesService.insert(article) match {
            case Authorized(result) =>
              result.fold(
                fail = nel => {
                  BadRequest(views.html.templates.formErrors(nel.list))
                },
                succ = created => Ok(routes.ArticleController.viewArticle(created.id).absoluteURL())
              )
            case NotAuthorized() =>
              Unauthorized("Not authorized to create articles")
          }
        }
      )
  }

  def editArticle(id: Int = 0) = Action { implicit request =>
    articlesService.get(id) match {
      case Some(article : ArticleDetailsModel) => Ok(views.html.editArticle(articleForm.fill(article)))
      case _ => NotFound(views.html.errors.notFound())
    }
  }

  def postArticleEdits() = Action { implicit request =>
      articleForm.bindFromRequest.fold(
        formWithErrors => BadRequest,
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
      articlesService.removeArticle(id).fold(
        fail = nel => {
          BadRequest(views.html.templates.formErrors(nel.list))
        },
        succ = created => Ok(routes.ArticleController.listAllArticles().absoluteURL())
      )
  }

  def searchByTag() = Action { implicit request =>
    tagSearchForm.bindFromRequest().fold(
      formWithErrors => BadRequest("Invalid request"),
      tag => {
        articlesService.searchByTag(tag).fold(
          fail = nel => {
            BadRequest(nel.list.mkString("<br>"))
          },
          //TODO provide a real implementation
          succ = result => Ok(routes.ArticleController.listAllArticles().absoluteURL())
        )
      }
    )
  }
}
