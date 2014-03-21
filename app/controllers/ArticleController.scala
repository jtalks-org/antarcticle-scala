package controllers

import play.api.mvc.{Action, Controller}
import services.{CommentsServiceComponent, ArticlesServiceComponent}
import play.api.data.Form
import play.api.data.Forms._
import models.ArticleModels.{ArticleDetailsModel, Article}
import security.{AuthenticatedUser, Authentication}
import security.Result._
import scalaz._

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
    )((id, title, content, tags) => Article(id, title, content, tags.split(",").map(_.trim).filter(!_.isEmpty)))
      ((article: Article) => Some((article.id, article.title, article.content, article.tags.mkString(","))))
  )

  val tagSearchForm = Form(
    mapping(
      "tag" ->  text
    )((tag)=>tag)((tag)=>Some(tag))
  )

  def listArticles() = listArticlesTaggedAndPaged(None, 1)

  def listArticlesPaged(tags: String, page: Int) = listArticlesTaggedAndPaged(Some(tags), page)

  def listArticlesTemp(page: Int) = listArticlesTaggedAndPaged(None, page)

  private def listArticlesTaggedAndPaged(tags: Option[String], page: Int) = Action {implicit request =>

    articlesService.getPage(page, tags).fold(
      fail => NotFound(views.html.errors.notFound()),
      succ = articlesPage => Ok(views.html.articles(articlesPage))
    )
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

  def previewArticle = Action { implicit request =>
    articleForm.bindFromRequest.fold(
      formWithErrors => errors(NonEmptyList("Incorrect request data")),
      article => {
        currentPrincipal match {
          case user : AuthenticatedUser =>
            Ok(views.html.templates.articlePreview(article, user.username))
          case _ =>
            Unauthorized("Please login first to create article previews")
        }
      }
    )
  }

  def postNewArticle = Action { implicit request =>
      articleForm.bindFromRequest.fold(
        formWithErrors => errors(NonEmptyList("Incorrect request data")),
        article => {
          articlesService.insert(article) match {
            case Authorized(result) =>
              result.fold(
                fail = nel => errors(nel),
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
    def updateArticle(article: Article) = {
      articlesService.updateArticle(article).fold(
        fail = errors,
        succ = {
          case Authorized(result) =>
            result.fold(
              fail = errors,
              succ = _ => Ok(routes.ArticleController.viewArticle(article.id.get).absoluteURL())
            )
          case NotAuthorized() =>
            Unauthorized("Not authorized to update this article")
        }
      )
    }

    articleForm.bindFromRequest.fold(
      formWithErrors => BadRequest,
      article => updateArticle(article)
    )
  }

  private def errors(errors: NonEmptyList[String]) = {
    BadRequest(views.html.templates.formErrors(errors.list))
  }

  def removeArticle(id: Int) = Action { implicit request =>
      articlesService.removeArticle(id).fold(
        fail = errors,
        succ =  {
          case Authorized(_) => Ok(routes.ArticleController.listArticles().absoluteURL())
          case NotAuthorized() => Unauthorized("Not authorized to remove this article")
        }
      )
  }

  def searchByTags() = Action { implicit request =>
    tagSearchForm.bindFromRequest().fold(
      formWithErrors => BadRequest("Invalid request"),
      tag => {
        articlesService.getPage(1, Some(tag)).fold(
          fail = nel => {
            BadRequest(nel.list.mkString("<br>"))
          },
          succ = articlesPage => Ok(views.html.articles(articlesPage))
        )
      }
    )
  }
}
