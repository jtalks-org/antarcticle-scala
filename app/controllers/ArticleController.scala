package controllers

import models.Page
import play.api.mvc.{AnyContent, Action, Controller}
import services.{CommentsServiceComponent, ArticlesServiceComponent}
import play.api.data.Form
import play.api.data.Forms._
import models.ArticleModels.{ArticleDetailsModel, Article, Language}
import security.{AuthenticatedUser, Authentication}
import security.Result._
import utils.RFC822
import scalaz._
import Scalaz._

/**
 * Serves web-based operations on articles
 */
trait ArticleController extends IndexController {
  this: Controller with ArticlesServiceComponent with CommentsServiceComponent with PropertiesProvider with Authentication =>

  /**
   * Describes binding between Article model object and web-form
   */
  val articleForm = Form(
    mapping(
      "id" -> optional(number),
      "title" -> text,
      "content" -> text,
      "tags" -> text,
      "language" -> text,
      "sourceId" -> optional(number)
    )((id, title, content, tags, language, sourceId) => Article(id, title, content, tags.split(",").map(_.trim).filter(!_.isEmpty), language, sourceId))
      ((article: Article) => Some((article.id, article.title, article.content, article.tags.mkString(","), article.language.toString(), article.sourceId)))
  )

  override def index() = allArticles()

  def allArticles() = listArticles(None)

  def listArticles(tags: Option[String]) = listArticlesPaged(tags)

  def listArticlesPaged(tags: Option[String], page: Int = 1)  = Action {implicit request =>
    articlesService.getPage(page, tags).fold(
      fail => NotFound(views.html.errors.notFound()),
      succ = articlesPage => Ok(views.html.articles(articlesPage, tags))
    )
  }

  def viewArticle(id: Int) = Action { implicit request =>
    articlesService.get(id) match {
      case Some(article : ArticleDetailsModel) => Ok(views.html.article(article, commentsService.getByArticle(id)))
      case _ => NotFound(views.html.errors.notFound())
    }
  }

  def getNewArticlePage = withUser { user => implicit request =>
      Ok(views.html.createArticle(articleForm, None))
  }

  def getTranslateArticlePage(articleId: Int) = withUser { user => implicit request =>
    articlesService.get(articleId) match {
      case Some(article : ArticleDetailsModel) =>
        val translation: Article = Article(None, "", "", article.tags, Language.Russian, Some(article.sourceId))
        Ok(views.html.createArticle(articleForm.fill(translation), some(article)))
      case _ => NotFound(views.html.errors.notFound())
    }
  }

  def previewArticle = Action { implicit request =>
    articleForm.bindFromRequest.fold(
      formWithErrors => errors(NonEmptyList("Incorrect request data")),
      article => {
        currentPrincipal match {
          case user : AuthenticatedUser =>
            articlesService.validate(article).fold(
              fail => Ok(views.html.templates.articlePreview(article, user.username, fail.list)),
              succ => Ok(views.html.templates.articlePreview(article, user.username, List()))
            )
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

  def editArticle(id: Int = 0) = withUser { user => implicit request =>
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
          case Authorized(_) => Ok(routes.ArticleController.viewArticle(article.id.get).absoluteURL())
          case NotAuthorized() => Unauthorized("Not authorized to update this article")
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
        case Authorized(_) => Ok(routes.ArticleController.allArticles().absoluteURL())
        case NotAuthorized() => Unauthorized("Not authorized to remove this article")
      }
    )
  }

  def fullRssFeed():Action[AnyContent] = rssFeed(None)

  def userRssFeed(user: String):Action[AnyContent] = rssFeed(Some(user))

  def rssFeed(user: Option[String]) = Action { implicit request =>
    user.cata(
      some = username => articlesService.getPageForUser(1, username),
      none = articlesService.getPage(1)
    ).fold(
      fail => NotFound(views.html.errors.notFound()),
      succ = articlesPage => {
        Ok(
          <rss version="2.0">
            <channel>
              <title>articles.javatalks.ru{user.cata(username => s" [$username]", "")}</title>
              <description>JavaTalks Articles{user.cata(username => s" by $username", "")}</description>
              <link>{routes.ArticleController.allArticles().absoluteURL()}</link>
              {
                for (article <- articlesPage.list) yield {
                  <item>
                    <title>{article.title}</title>
                    <link>{routes.ArticleController.viewArticle(article.id).absoluteURL()}</link>
                    <description>{article.description}</description>
                    <pubDate>{RFC822(article.createdAt)}</pubDate>
                    <author>{article.author.username}</author>
                  </item>
                }
              }
            </channel>
          </rss>
        ).as("application/rss+xml;charset=UTF-8")
      }
    )
  }
}
