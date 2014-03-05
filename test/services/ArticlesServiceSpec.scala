package services

import org.specs2.mutable.Specification
import models.database._
import utils.Implicits._
import org.specs2.time.NoTimeConversions
import com.github.nscala_time.time.Imports._
import org.specs2.mock.Mockito
import repositories.{UsersRepositoryComponent, TagsRepositoryComponent, ArticlesRepositoryComponent}
import org.mockito.Matchers
import models.ArticleModels.{ArticleDetailsModel, Article, ArticleListModel}
import util.{TimeFridge, MockSession}
import models.Page
import org.specs2.specification.BeforeExample
import scalaz._
import Scalaz._
import validators.{TagValidator, Validator}
import util.ScalazValidationTestUtils._
import org.specs2.scalaz.ValidationMatchers
import conf.Constants._
import security._
import security.Result._


class ArticlesServiceSpec extends Specification
  with NoTimeConversions with Mockito
  with BeforeExample with ValidationMatchers with MockSession {

  object service extends ArticlesServiceComponentImpl
  with ArticlesRepositoryComponent
  with TagsServiceComponent with UsersRepositoryComponent with TagsRepositoryComponent with MockSessionProvider {
    override val articlesRepository = mock[ArticlesRepository]
    override val tagsService = mock[TagsService]
    override val articleValidator = mock[Validator[Article]]
    override val usersRepository = mock[UsersRepository]
    override val tagsRepository = mock[TagsRepository]
    val tagValidator: TagValidator = mock[TagValidator]
  }

  import service._

  def before = {
    org.mockito.Mockito.reset(tagsService)
    org.mockito.Mockito.reset(articlesRepository)
    org.mockito.Mockito.reset(articleValidator)
    org.mockito.Mockito.reset(session)
    org.mockito.Mockito.reset(usersRepository)
    org.mockito.Mockito.reset(tagsRepository)
  }

  val dbRecord = {
    val article = ArticleRecord(1.some, "", "", DateTime.now, DateTime.now, "", 1)
    val user = UserRecord(1.some, "")
    val tags = List("tag1", "tag2")
    (article, user, tags)
  }

  "get article" should {
    val article = dbRecord.some

    "return model" in {
      tagsRepository.getByName(any)(Matchers.eq(session)) returns None
      articlesRepository.get(anyInt)(Matchers.eq(session)) returns article

      val model = articlesService.get(1)

      model.map(_.id) must beSome(1)
    }

    "have correct author" in {
      articlesRepository.get(anyInt)(Matchers.eq(session)) returns article

      val model = articlesService.get(1)

      model.map(_.author.id) must beSome(1)
    }

    "have correct tags" in {
      articlesRepository.get(anyInt)(Matchers.eq(session)) returns article

      val model = articlesService.get(1)

      model.map(_.tags).get must containTheSameElementsAs(dbRecord._3)
    }
  }

  "paginated articles list" should {

    "request second page with correct parameters" in {
      tagsRepository.getByName(any)(Matchers.eq(session)) returns None
      articlesRepository.getList(anyInt, anyInt, any)(Matchers.eq(session)) returns List()
      articlesRepository.count(any)(Matchers.eq(session)) returns PAGE_SIZE * 2

      articlesService.getPage(2)

      there was one(articlesRepository).getList(PAGE_SIZE, PAGE_SIZE, None)(session)
    }

    "contain list models" in {
      tagsRepository.getByName(any)(Matchers.eq(session)) returns None
      articlesRepository.getList(anyInt, anyInt, any)(Matchers.eq(session)) returns List(dbRecord)
      articlesRepository.count(any)(Matchers.eq(session)) returns PAGE_SIZE

      articlesService.getPage(1).fold(
        fail = nel => ko,
        succ = model => {
          model.list(0).id must_== 1
          model.list(0).author.id must_== 1
        }
      )
    }

    "contain list models" in {
      tagsRepository.getByName(any)(Matchers.eq(session)) returns None
      articlesRepository.getList(anyInt, anyInt, any)(Matchers.eq(session)) returns List(dbRecord)
      articlesRepository.count(any)(Matchers.eq(session)) returns PAGE_SIZE

      articlesService.getPage(1).fold(
        fail = nel => ko,
        succ = model => {
          model.list(0).id must_== 1
          model.list(0).author.id must_== 1
        }
      )
    }

    "contain current page" in {
      tagsRepository.getByName(any)(Matchers.eq(session)) returns None
      articlesRepository.getList(anyInt, anyInt, any)(Matchers.eq(session)) returns List(dbRecord)
      articlesRepository.count(any)(Matchers.eq(session)) returns PAGE_SIZE

      articlesService.getPage(1).fold(
        fail = nel => ko,
        succ = model => model.currentPage must_== 1
      )
    }

    "contain total pages count" in {
      val count = 3 * PAGE_SIZE/2
      tagsRepository.getByName(any)(Matchers.eq(session)) returns None
      articlesRepository.getList(anyInt, anyInt, any)(Matchers.eq(session)) returns List(dbRecord)
      articlesRepository.count(any)(Matchers.eq(session)) returns count

      articlesService.getPage(1).fold(
        fail = nel => ko,
        succ = model => model.totalPages must_== 2
      )
    }

    "correctly return empty article list" in {
      tagsRepository.getByName(any)(Matchers.eq(session)) returns None
      articlesRepository.getList(anyInt, anyInt, any)(Matchers.eq(session)) returns List(dbRecord)
      articlesRepository.count(any)(Matchers.eq(session)) returns 0

      articlesService.getPage(1).fold(
        fail = nel => ko,
        succ = model => {
          model.list(0).id must_== 1
          model.list(0).author.id must_== 1
        }
      )
    }

    "return failure for non-exising page" in {
      tagsRepository.getByName(any)(Matchers.eq(session)) returns None
      articlesRepository.getList(anyInt, anyInt, any)(Matchers.eq(session)) returns List(dbRecord)
      articlesRepository.count(any)(Matchers.eq(session)) returns 1

      articlesService.getPage(-15).fold(
        fail = nel => ok,
        succ = model => ko
      )
    }

    "filter articles by tag" in {
      val count = 3 * PAGE_SIZE/2
      tagsRepository.getByName(Some("tag"))(session) returns Some(new Tag(1, "tag"))
      articlesRepository.getList(0, PAGE_SIZE, Some(1))(session) returns List(dbRecord)
      articlesRepository.count(any)(Matchers.eq(session)) returns count

     articlesService.getPage(1, Some("tag")).fold(
       fail = nel => ko,
       succ = model => {
         model.list.nonEmpty must beTrue
         model.currentPage must_== 1
         model.totalItems must_== count
       }
     )
    }

    "handle nonexistent tags" in {
      tagsRepository.getByName(Some("tag"))(session) returns None
      articlesRepository.getList(0, PAGE_SIZE, None)(session) returns List(dbRecord)
      articlesRepository.count(any)(Matchers.eq(session)) returns PAGE_SIZE

      articlesService.getPage(1, Some("tag")).fold(
        fail = nel => ko,
        succ = model => model.list.nonEmpty must beTrue
      )
    }
  }

  "creating new article" should {
    implicit def getCurrentUser = {
      val usr = spy(AuthenticatedUser(1, "username", Authorities.User))
      org.mockito.Mockito.doReturn(true)
        .when(usr)
        .can(Matchers.eq(Permissions.Create), Matchers.eq(Entities.Article))
      usr
    }
    val article = Article(None, "", "", List())
    val userRecord = UserRecord(1.some, "user").some

    "insert new article" in {
      TimeFridge.withFrozenTime() { dt =>
          val record = ArticleRecord(None, "", "", dt, dt, "", getCurrentUser.userId)
          articlesRepository.insert(any[ArticleRecord])(Matchers.eq(session)) returns 1
          articleValidator.validate(any[Article]) returns article.successNel
          tagsService.createTagsForArticle(anyInt, any[Seq[String]])(Matchers.eq(session)) returns Seq.empty.success
          usersRepository.getByUsername(getCurrentUser.username)(session) returns userRecord

          articlesService.insert(article)

          there was one(articlesRepository).insert(record)(session)
      }
    }

    "return model with assigned id" in {
      articlesRepository.insert(any[ArticleRecord])(Matchers.eq(session)) returns 1
      articleValidator.validate(any[Article]) returns article.successNel
      tagsService.createTagsForArticle(anyInt, any[Seq[String]])(Matchers.eq(session)) returns Seq.empty.success
      usersRepository.getByUsername(getCurrentUser.username)(session) returns userRecord

      articlesService.insert(article) match {
        case Authorized(Success(model)) => model.id must_== 1
        case _ => ko
      }
    }

    "create tags" in {
      val tags = List("tag1", "tag2")
      val articleId = 1
      articlesRepository.insert(any[ArticleRecord])(Matchers.eq(session)) returns articleId
      articleValidator.validate(any[Article]) returns article.successNel
      usersRepository.getByUsername(getCurrentUser.username)(session) returns userRecord
      tagsService.createTagsForArticle(anyInt, any[Seq[String]])(Matchers.eq(session)) returns tags.success

      articlesService.insert(article.copy(tags = tags))

      there was one(tagsService).createTagsForArticle(articleId, tags)(session)
    }

    "not create article when validation failed" in {
      articleValidator.validate(any[Article]) returns "".failNel
      tagsService.createTagsForArticle(anyInt, any[Seq[String]])(Matchers.eq(session)) returns Seq.empty.success

      articlesService.insert(article)

      there was noMoreCallsTo(articlesRepository, tagsService)
    }

    "rollback transaction when tags creation failed" in {
      articleValidator.validate(any[Article]) returns article.successNel
      tagsService.createTagsForArticle(anyInt, any[Seq[String]])(Matchers.eq(session)) returns "".failNel

      articlesService.insert(article)

      there was one(session).rollback()
    }

    "set author as current user" in {
      articlesRepository.insert(any[ArticleRecord])(Matchers.eq(session)) returns 1
      articleValidator.validate(any[Article]) returns article.successNel
      tagsService.createTagsForArticle(anyInt, any[Seq[String]])(Matchers.eq(session)) returns Seq.empty.success
      usersRepository.getByUsername(getCurrentUser.username)(session) returns userRecord

      articlesService.insert(article) match {
        case Authorized(Success(model)) => model.author.id must_== getCurrentUser.userId
        case _ => ko
      }
    }

    "fail when user is not authorized to do it" in {
      val currentUser = mock[AuthenticatedUser]
      currentUser.can(Permissions.Create, Entities.Article) returns false

      articlesService.insert(article)(AnonymousPrincipal) must beLike {
        case NotAuthorized() => ok
        case _ => ko
      }
    }
  }

  "article update" should {
    val articleId = 1
    val tags = List("tag")
    val article = Article(articleId.some, "", "", tags)
    val record = ArticleRecord(articleId.some, "", "", null, null, "", 1)
    implicit def getCurrentUser = {
      val usr = spy(AuthenticatedUser(1, "username", Authorities.User))
      org.mockito.Mockito.doReturn(true)
        .when(usr)
        .can(Matchers.eq(Permissions.Update), Matchers.eq(record))
      usr
    }

    "update existing article" in {
      articlesRepository.get(articleId)(session) returns Option(record, null, null)
      articleValidator.validate(any[Article]) returns article.successNel
      tagsService.updateTagsForArticle(Matchers.eq(articleId), any[Seq[String]])(Matchers.eq(session)) returns Seq.empty.successNel

      articlesService.updateArticle(article)

      there was one(articlesRepository).update(Matchers.eq(articleId), any[ArticleToUpdate])(Matchers.eq(session))
    }

    "update modification time" in {
      TimeFridge.withFrozenTime() { now =>
        articlesRepository.get(articleId)(session)  returns Option(record, null, null)
        articleValidator.validate(any[Article]) returns article.successNel
        tagsService.updateTagsForArticle(Matchers.eq(articleId), any[Seq[String]])(Matchers.eq(session)) returns Seq.empty.successNel

        articlesService.updateArticle(article)

        there was one(articlesRepository).update(articleId, ArticleToUpdate("", "", now, ""))(session) //TODO: match only modification time
      }
    }

    "update tags" in {
      articlesRepository.get(articleId)(session)  returns Option(record, null, null)
      articleValidator.validate(any[Article]) returns article.successNel
      tagsService.updateTagsForArticle(articleId, tags)(session) returns Seq.empty.successNel

      articlesService.updateArticle(article)

      there was one(tagsService).updateTagsForArticle(articleId, tags)(session)
    }

    "return failure when tags validation failed" in pending
    "return failure when article validation failed" in pending
    "return failure when article not found" in pending

    "not update article when tags validation failed" in {
      articlesRepository.get(articleId)(session)  returns Option(record, null, null)
      articleValidator.validate(any[Article]) returns article.successNel
      tagsService.updateTagsForArticle(Matchers.eq(articleId), any[Seq[String]])(Matchers.eq(session)) returns "".failNel

      articlesService.updateArticle(article)

      there was no(articlesRepository).update(anyInt, any[ArticleToUpdate])(Matchers.eq(session))
    }

    "not update article when article validation failed" in {
      articlesRepository.get(articleId)(session) returns Option(record, null, null)
      articleValidator.validate(any[Article]) returns "".failNel

      articlesService.updateArticle(article)

      there was no(articlesRepository).update(anyInt, any[ArticleToUpdate])(Matchers.eq(session))
    }

    "fail when user is not authorized to do it" in {
      val currentUser = mock[AuthenticatedUser]
      articlesRepository.get(articleId)(session) returns Option(record, null, null)
      currentUser.can(Permissions.Update, article) returns false

      articlesService.updateArticle(article)(AnonymousPrincipal) must beSuccessful.like {
        case NotAuthorized() => ok
        case _ => ko
      }
    }
  }

  "article removal" should {
    val record = ArticleRecord(None, "", "", null, null, "", 1)
    implicit def getCurrentUser = {
      val usr = mock[AuthenticatedUser]
      usr.userId returns 1
      usr.username returns "user"
      usr.can(Permissions.Delete, record) returns true
      usr
    }

    "remove article" in {
      articlesRepository.get(1)(session)  returns Option(record, null, null)

      articlesService.removeArticle(1)

      there was one(articlesRepository).remove(1)(session)
    }
  }
}
