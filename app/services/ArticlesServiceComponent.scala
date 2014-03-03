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
import validators.{TagValidator, Validator}
import scala.slick.jdbc.JdbcBackend
import security.Principal
import security.Entities
import security.Permissions._
import security.AuthenticatedUser
import security.Result._

trait ArticlesServiceComponent {
  val articlesService: ArticlesService

  trait ArticlesService {
    def get(id: Int): Option[ArticleDetailsModel]
    def getPage(page: Int, tag : Option[String] = None): Page[ArticleListModel]
    def getPageForUser(page: Int, userName : String, tag : Option[String] = None): Page[ArticleListModel]
    def insert(article: Article)(implicit principal: Principal): AuthorizationResult[ValidationNel[String, ArticleDetailsModel]]
    def updateArticle(article: Article)(implicit principal: Principal): ValidationNel[String, AuthorizationResult[ValidationNel[String, Unit]]]
    def removeArticle(id: Int)(implicit principal: Principal): ValidationNel[String, Boolean]
    def searchByTag(tag: String): ValidationNel[String, List[Article]]
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
            //TODO:
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

    def updateArticle(article: Article)(implicit principal: Principal) = withTransaction { implicit session =>
        //TODO: create NotFound Result to prevent nested ValidataionNel?
        article.id.flatMap(articlesRepository.get(_)).map { case (article, _, _) => article }.cata(
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

    def removeArticle(id: Int)(implicit principal: Principal) = withTransaction {
      implicit session =>
      // todo: handle non-existent id properly
        val article = articlesRepository.get(id).get._1
        principal match {
          case currentUser: AuthenticatedUser if currentUser.can(Delete, article) =>
            articlesRepository.remove(id).successNel
          case _ => "Authorization failure".failureNel
      }
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

    def searchByTag(tag: String): ValidationNel[String, List[Article]] = {
      tagValidator.validate(tag).fold(
        fail = nel => Failure(nel),
        succ = created => {
          //TODO provide a real implementation
          List().successNel
        }
      )
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
