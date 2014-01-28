package services

import org.joda.time.DateTime
import java.sql.Timestamp
import repositories.{TagsRepositoryComponent, UsersRepositoryComponent, ArticlesRepositoryComponent}
import models.database.{ArticleToUpdate, UserRecord, ArticleRecord}
import models.ArticleModels.{Article, ArticleDetailsModel, ArticleListModel}
import models.UserModels.UserModel
import utils.Implicits._
import conf.Constants
import models.Page
import scalaz._
import Scalaz._
import validators.Validator
import scala.slick.jdbc.JdbcBackend

trait ArticlesServiceComponent {
  val articlesService: ArticlesService

  trait ArticlesService {
    def getPage(page: Int, tag : Option[String] = None): Page[ArticleListModel]
    def getPageForUser(page: Int, userName : String, tag : Option[String] = None): Page[ArticleListModel]
    def createArticle(article: Article): ValidationNel[String, ArticleDetailsModel]
    def get(id: Int): Option[ArticleDetailsModel]
    def updateArticle(article: Article): ValidationNel[String, Article]
    def removeArticle(id: Int)
  }
}

trait ArticlesServiceComponentImpl extends ArticlesServiceComponent {
  this: ArticlesRepositoryComponent with TagsServiceComponent with UsersRepositoryComponent with TagsRepositoryComponent
    with SessionProvider =>

  val articlesService = new ArticlesServiceImpl
  val articleValidator: Validator[Article]

  class ArticlesServiceImpl extends ArticlesService {

    def createArticle(article: Article) = withTransaction { implicit session =>
      def createRecord = {
        val creationTime = DateTime.now
        val currentUserId = 1 //TODO
        articleToInsert(article, creationTime, currentUserId)
      }

      val result = for {
        _ <- articleValidator.validate(article)
        newRecord = createRecord
        id = articlesRepository.insert(newRecord)
        tags <- tagsService.createTagsForArticle(id, article.tags)
      } yield recordToDetailsModel(newRecord.copy(id = Some(id)), UserRecord(Some(1), "")/*TODO: real user*/, tags)

      if (result.isFailure) {
        // article should not be persisted, when tags creation failed
        // reason: tags creation requires article id which is auto-increment column
        session.rollback()
      }

      result
    }


    def updateArticle(article: Article) = withTransaction { implicit session =>
      articleValidator.validate(article).map { _ =>
        val modificationTime = DateTime.now
        //TODO: handle id, tags validation
        articlesRepository.update(article.id.get, articleToUpdate(article, modificationTime))
        article
      }
    }

    def removeArticle(id: Int) = withTransaction { implicit session =>
      articlesRepository.remove(id)
    }

    def get(id: Int) = withSession { implicit session =>
      articlesRepository.get(id).map((recordToDetailsModel _).tupled)
    }

    def getPage(page: Int, tag : Option[String] = None) = withSession { implicit session =>
      fetchPageFromDb(page, None, tag)
    }

    def getPageForUser(page: Int, userName: String, tag : Option[String] = None): Page[ArticleListModel] = withSession { implicit session =>
      val userId = usersRepository.getByUsername(userName).get.id
      fetchPageFromDb(page, userId, tag)
    }

    private def fetchPageFromDb(page: Int, userId: Option[Int] = None, tag : Option[String] = None)(implicit s: JdbcBackend#Session) = {
      val pageSize = Constants.PAGE_SIZE
      val offset = pageSize * (page - 1)
      val tagId = tagsRepository.getByName(tag).map(_.id)
      val list = userId.cata(
        some = articlesRepository.getListForUser(_, offset, pageSize, tagId),
        none = articlesRepository.getList(offset, pageSize, tagId)
      )
      val modelsList = list.map((recordToListModel _).tupled)
      val total = userId.cata(
        some = articlesRepository.countForUser(_, tagId),
        none = articlesRepository.count(tagId)
      )
      Page(page, total, modelsList)
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

    private def recordToListModel(articleRecord: ArticleRecord, authorRecord: UserRecord, tags: Seq[String]) = {
      ArticleListModel(articleRecord.id.get, articleRecord.title,
        articleRecord.description, articleRecord.createdAt,
        UserModel(authorRecord.id.get, authorRecord.username), tags)
    }
  }
}
