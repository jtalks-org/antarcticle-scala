package services

import org.joda.time.DateTime
import java.sql.Timestamp
import repositories.ArticlesRepositoryComponent
import models.database.{ArticleToInsert, ArticleToUpdate, UserRecord, ArticleRecord}
import models.ArticleModels.{Article, ArticleDetailsModel, ArticleListModel}
import models.UserModels.UserModel
import utils.DateImplicits._
import scala.slick.session.Session
import scala.math.ceil
import conf.Constants
import models.Page
import scalaz._
import Scalaz._
import validators.Validator

trait ArticlesServiceComponent {
  val articlesService: ArticlesService

  trait ArticlesService {
    def getPage(page: Int): Page[ArticleListModel]
    def getPageForUser(page: Int, userName : String): Page[ArticleListModel]
    def createArticle(article: Article): ValidationNel[String, ArticleDetailsModel]
    def get(id: Int): Option[ArticleDetailsModel]
    def updateArticle(article: Article)
    def removeArticle(id: Int)
  }
}

trait ArticlesServiceComponentImpl extends ArticlesServiceComponent {
  this: ArticlesRepositoryComponent with TagsServiceComponent
    with SessionProvider =>

  val articlesService = new ArticlesServiceImpl
  val articleValidator: Validator[Article]

  class ArticlesServiceImpl extends ArticlesService {
    def createArticle(article: Article) = withTransaction { implicit session: Session =>
      def createRecord = {
        val creationTime = DateTime.now
        val currentUserId = 1 //TODO
        articleToInsert(article, creationTime, currentUserId)
      }

      val result = for {
        _ <- articleValidator.validate(article)
        newRecord = createRecord
        id = articlesRepository.insert(newRecord)
        _ <- tagsService.createTagsForArticle(id, article.tags)
      } yield insertToDetailsModel(id, newRecord, UserRecord(Some(1), "")/*TODO: real user*/)

      if (result.isFailure) {
        // article should not be persisted, when tags creation failed
        // reason: tags creation requires article id which is auto-increment column
        session.rollback()
      }

      result
    }


    def updateArticle(article: Article) = withTransaction { implicit s: Session =>
      val modificationTime = DateTime.now
      //TODO: handle id
      articlesRepository.update(article.id.get, articleToUpdate(article, modificationTime))
    }

    def removeArticle(id: Int) = withTransaction { implicit s: Session =>
      articlesRepository.remove(id)
    }

    def get(id: Int) = withSession { implicit s: Session =>
      articlesRepository.get(id).map((recordToDetailsModel _).tupled)
    }

    def getPage(page: Int) = withSession { implicit s: Session =>
      fetchPageFromDb(page)
    }

    //TODO: Fetch articles for given user only
    def getPageForUser(page: Int, userName: String): Page[ArticleListModel] = withSession { implicit s: Session =>
      val userId = 1 // TODO
      fetchPageFromDb(page, Some(userId))
    }

    private def fetchPageFromDb(page: Int, userId: Option[Int] = None)(implicit s: Session) = {
      val pageSize = Constants.PAGE_SIZE
      val offset = pageSize * page
      val list = userId.cata(
        some = articlesRepository.getListForUser(_, offset, pageSize),
        none = articlesRepository.getList(offset, pageSize)
      )
      val modelsList = list.map((recordToListModel _).tupled)
      val total = userId.cata(
        some = articlesRepository.countForUser(_),
        none = articlesRepository.count()
      )
      val totalPages = ceil(total / pageSize.toDouble).toInt
      Page(page, totalPages, total, modelsList)
    }

    //TODO: Extract conversions and write tests for them

    private def articleToInsert(article: Article, creationTime: Timestamp, authorId: Int) = {
      ArticleToInsert(article.title, article.content, creationTime, creationTime, article.description, authorId)
    }

    private def articleToUpdate(article: Article, updatedAt: Timestamp) = {
      ArticleToUpdate(article.title, article.content, updatedAt, article.description)
    }

    private def insertToDetailsModel(id: Int, article: ArticleToInsert, authorRecord: UserRecord) = {
      ArticleDetailsModel(id, article.title,
        article.content, article.createdAt,
        UserModel(authorRecord.id.get, authorRecord.username), List[String]())
    }

    private def recordToDetailsModel(articleRecord: ArticleRecord, authorRecord: UserRecord, tags: List[String]) = {
      ArticleDetailsModel(articleRecord.id.get, articleRecord.title,
        articleRecord.content, articleRecord.createdAt,
        UserModel(authorRecord.id.get, authorRecord.username), tags)
    }

    private def recordToListModel(articleRecord: ArticleRecord, authorRecord: UserRecord, tags: List[String]) = {
      ArticleListModel(articleRecord.id.get, articleRecord.title,
        articleRecord.description, articleRecord.createdAt,
        UserModel(authorRecord.id.get, authorRecord.username), tags)
    }
  }
}
