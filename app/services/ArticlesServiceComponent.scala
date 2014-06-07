package services

import org.joda.time.DateTime
import java.sql.Timestamp
import repositories.{TagsRepositoryComponent, UsersRepositoryComponent, ArticlesRepositoryComponent}
import utils.Implicits._
import conf.Constants
import scalaz._
import Scalaz._
import validators.{TagValidator, Validator}
import scala.slick.jdbc.JdbcBackend
import security.Principal
import security.Entities
import security.Permissions._
import security.Result._
import models.database.ArticleToUpdate
import models.{ArticlePage, Page}
import scala.Some
import models.ArticleModels.ArticleDetailsModel
import models.UserModels.UserModel
import security.AuthenticatedUser
import models.ArticleModels.ArticleListModel
import models.database.UserRecord
import models.database.ArticleRecord
import models.ArticleModels.Article

trait ArticlesServiceComponent {
  val articlesService: ArticlesService

  trait ArticlesService {
    def get(id: Int): Option[ArticleDetailsModel]
    def getPage(page: Int, tag: Option[String] = None):  ValidationNel[String, Page[ArticleListModel]]
    def getPageForUser(page: Int, userName: String, tag: Option[String] = None): ValidationNel[String, Page[ArticleListModel]]
    def validate(article: Article):  ValidationNel[String, Article]
    def insert(article: Article)(implicit principal: Principal): AuthorizationResult[ValidationNel[String, ArticleDetailsModel]]
    def updateArticle(article: Article)(implicit principal: Principal): ValidationNel[String, AuthorizationResult[ValidationNel[String, Unit]]]
    def removeArticle(id: Int)(implicit principal: Principal): ValidationNel[String, AuthorizationResult[Unit]]
  }
}

trait ArticlesServiceComponentImpl extends ArticlesServiceComponent {
  this: ArticlesRepositoryComponent with TagsServiceComponent with UsersRepositoryComponent with TagsRepositoryComponent
    with SessionProvider =>

  val articlesService = new ArticlesServiceImpl
  val articleValidator: Validator[Article]
  val tagValidator: TagValidator

  class ArticlesServiceImpl extends ArticlesService {

    def insert(article: Article)(implicit principal: Principal) =
      principal.doAuthorizedOrFail(Create, Entities.Article) { () =>
        withTransaction { implicit session =>
          def createRecord = {
            val creationTime = DateTime.now
            val currentUserId = principal.asInstanceOf[AuthenticatedUser].userId
            articleToInsert(article, creationTime, currentUserId)
          }

          val result = for {
            _ <- articleValidator.validate(article)
            newRecord = createRecord
            id = articlesRepository.insert(newRecord)
            tags <- tagsService.createTagsForArticle(id, article.tags)
            user = usersRepository.getByUsername(principal.asInstanceOf[AuthenticatedUser].username).get
          } yield recordToDetailsModel(newRecord.copy(id = Some(id)), user, tags)

          if (result.isFailure) {
            // article should not be persisted, when tags creation failed
            // reason: tags creation requires article id which is auto-increment column
            session.rollback()
          }

          result
        }
      }

    def validate(article: Article) = articleValidator.validate(article)

    def updateArticle(article: Article)(implicit principal: Principal) = withTransaction { implicit session =>
        //TODO: create NotFound Result to prevent nested ValidationNel?
        article.id.flatMap(articlesRepository.get).map { case (article, _, _) => article }.cata(
          some = persistentArticle => {
            principal.doAuthorizedOrFail(Update, persistentArticle) { () =>
              for {
                _ <- articleValidator.validate(article)
                _ <- tagsService.updateTagsForArticle(persistentArticle.id.get, article.tags)
              } yield {
                articlesRepository.update(persistentArticle.id.get, articleToUpdate(article, DateTime.now))
                ()
              }
            }.successNel
          },
          none = "Article not found".failureNel
        )
    }

    def removeArticle(id: Int)(implicit principal: Principal) = withTransaction { implicit session =>
      articlesRepository.get(id).map { case (article, _, _) => article }.cata(
        some = persistentArticle => {
          principal.doAuthorizedOrFail(Delete, persistentArticle) { () =>
            articlesRepository.remove(id)
            ()
          }.successNel
        },
        none = "Article not found".failureNel
      )
    }

    def get(id: Int) = withSession { implicit session =>
      articlesRepository.get(id).map((recordToDetailsModel _).tupled)
    }

    def getPage(page: Int, tags : Option[String] = None) = withSession { implicit session =>
      fetchPageFromDb(page, None, tags)
    }

    def getPageForUser(page: Int, userName: String, tags : Option[String] = None) = withSession { implicit session =>
      val userId = usersRepository.getByUsername(userName).get.id
      fetchPageFromDb(page, userId, tags)
    }

    private def fetchPageFromDb(page: Int, userId: Option[Int] = None, tagsString : Option[String] = None)(implicit s: JdbcBackend#Session) = {
      val pageSize = Constants.PAGE_SIZE_ARTICLES
      val offset = pageSize * (page - 1)
      val tagsIds = tagsString match {
        case Some(tagsValues) =>
          if (tagsValues == null || tagsValues.isEmpty) {
            None
          } else {
            Some(tagsRepository.getByNames(tagsValues.split(",")).map(_.id))
          }
        case None => None
      }
      val total = userId.cata(
        some = articlesRepository.countForUser(_, tagsIds),
        none = articlesRepository.count(tagsIds)
      )
      total match {
        case it if 1 until ArticlePage.getPageCount(it) + 1 contains page =>
          val list = userId.cata(
            some = articlesRepository.getListForUser(_, offset, pageSize, tagsIds),
            none = articlesRepository.getList(offset, pageSize, tagsIds)
          )
          val modelsList = list.map((recordToListModel _).tupled)
          new ArticlePage(page, total, modelsList).successNel
        case _ => "No such page exists".failureNel
      }
    }

    //TODO: Extract conversions and write tests for them

    private def articleToInsert(article: Article, creationTime: Timestamp, authorId: Int) = {
      ArticleRecord(None, article.title, article.content, creationTime, creationTime, article.description, authorId)
    }

    private def articleToUpdate(article: Article, updatedAt: Timestamp) = {
      ArticleToUpdate(article.title, article.content, updatedAt, article.description)
    }

    private def recordToDetailsModel(articleRecord: ArticleRecord, authorRecord: UserRecord, tags: Seq[String]) = {
      ArticleDetailsModel(articleRecord.id.get, articleRecord.title,
        articleRecord.content, articleRecord.createdAt,
        UserModel(authorRecord.id.get, authorRecord.username), tags)
    }

    private def recordToListModel(articleRecord: ArticleRecord, authorRecord: UserRecord, tags: Seq[String],
                                  commentsCount: Int) = {
      ArticleListModel(articleRecord.id.get, articleRecord.title,
        articleRecord.description, articleRecord.createdAt,
        UserModel(authorRecord.id.get, authorRecord.username), tags, commentsCount)
    }
  }
}
