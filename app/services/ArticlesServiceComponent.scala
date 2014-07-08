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
import models.ArticleModels._
import models.UserModels.UserModel
import security.AuthenticatedUser
import models.database.UserRecord
import models.database.ArticleRecord

trait ArticlesServiceComponent {
  val articlesService: ArticlesService

  trait ArticlesService {
    def get(id: Int): Option[ArticleDetailsModel]
    def getTranslations(id: Option[Int]): ValidationNel[String, List[Translation]]
    def getPage(page: Int, tag: Option[String] = None):  ValidationNel[String, Page[ArticleListModel]]
    def getPageForUser(page: Int, userName: String, tag: Option[String] = None): ValidationNel[String, Page[ArticleListModel]]
    def validate(article: Article):  ValidationNel[String, Article]
    def insert(article: Article)(implicit principal: Principal): AuthorizationResult[ValidationNel[String, ArticleDetailsModel]]
    def updateArticle(article: Article)(implicit principal: Principal): ValidationNel[String, AuthorizationResult[Unit]]
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
            translations <- getTranslations(article.sourceId)
            newRecord <- {
              if (translations.find(t => t.language == article.language).isEmpty) createRecord.successNel
              else "Article on selected language has been already created".failureNel
            }
            id = articlesRepository.insert(newRecord)
            tags <- tagsService.createTagsForArticle(id, article.tags)
            user = usersRepository.getByUsername(principal.asInstanceOf[AuthenticatedUser].username).get
            sourceId = if (newRecord.sourceId.isDefined) newRecord.sourceId else some(id)
          } yield recordToDetailsModel(newRecord.copy(id = Some(id), sourceId = sourceId), user, tags,
              Translation(id, article.language) :: translations)

          if (result.isFailure) {
            // article should not be persisted, when tags creation failed
            // reason: tags creation requires article id which is auto-increment column
            session.rollback()
          }

          result
        }
      }

    def getTranslations(id: Option[Int]): ValidationNel[String, List[Translation]] = withTransaction { implicit session =>
      id.cata(
        some = some => articlesRepository.getTranslations(some).map{
          case (uid, lang) => Translation(uid, lang)
        }.successNel,
        none = List().successNel
      )
    }

    def validate(article: Article) = articleValidator.validate(article)

    def updateArticle(article: Article)(implicit principal: Principal) = withTransaction { implicit session =>

      def validateArticleAndTranslation(persistedArticle: ArticleRecord): Validation[NonEmptyList[String], Unit] = {
        for {
          _ <- articleValidator.validate(article)
          translations <- getTranslations(persistedArticle.sourceId)
          result <- translations.find(t => t.id != article.id.get && t.language == article.language).cata(
            some = some => "Article on selected language has been already created".failureNel,
            none = ().successNel
          )
        } yield result
      }

      def getArticleFromRepository: Validation[NonEmptyList[String], ArticleRecord] = {
        (for {
          id <- article.id
          (articleRecord, _, _) <- articlesRepository.get(id)
        } yield articleRecord) match {
          case Some(a) => a.successNel
          case None => "Article not found".failureNel
        }
      }

      getArticleFromRepository.flatMap{persistedArticle =>
        principal.doAuthorizedOrFail(Update, persistedArticle){() =>
          for {
            _ <- validateArticleAndTranslation(persistedArticle)
            _ <- tagsService.updateTagsForArticle(persistedArticle.id.get, article.tags)
          } yield {
            articlesRepository.update(persistedArticle.id.get, articleToUpdate(article, DateTime.now))
            ()
          }
        } match {
          case Authorized(result) => result.fold(
            succ = success => Authorized(()).successNel,
            fail = errors => errors.head.failNel
          )
          case NotAuthorized() => NotAuthorized().successNel
        }
      }
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
      for {
        (article, user, tags) <- articlesRepository.get(id)
        translations = articlesRepository.getTranslations(id).map{case (uid,lang) => Translation(uid, lang)}
      } yield recordToDetailsModel(article, user, tags, translations)

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
      ArticleRecord(None, article.title, article.content, creationTime, creationTime,
        article.description, authorId, article.language, article.sourceId)
    }

    private def articleToUpdate(article: Article, updatedAt: Timestamp) = {
      ArticleToUpdate(article.title, article.content, updatedAt, article.description, article.language)
    }

    private def recordToDetailsModel(articleRecord: ArticleRecord, authorRecord: UserRecord,
                                     tags: Seq[String], translations: List[Translation]) = {
      ArticleDetailsModel(articleRecord.id.get, articleRecord.title, articleRecord.content, articleRecord.createdAt,
        UserModel(authorRecord.id.get, authorRecord.username), tags, articleRecord.language,
        articleRecord.sourceId.get, translations)
    }

    private def recordToListModel(articleRecord: ArticleRecord, authorRecord: UserRecord, tags: Seq[String],
                                  commentsCount: Int) = {
      ArticleListModel(articleRecord.id.get, articleRecord.title,
        articleRecord.description, articleRecord.createdAt,
        UserModel(authorRecord.id.get, authorRecord.username), tags, commentsCount)
    }
  }
}
