package repositories

import models.database._
import models.database.ArticleToUpdate
import models.database.UserRecord
import models.database.ArticleRecord
import scala.slick.jdbc.JdbcBackend
import scalaz._
import Scalaz._

trait ArticlesRepositoryComponent {
  val articlesRepository: ArticlesRepository

  trait ArticlesRepository {
    def getList(offset: Int, portionSize: Int, tagsIds: Option[Seq[Int]] = None)(implicit s: JdbcBackend#Session): List[(ArticleRecord, UserRecord, Seq[String])]

    def getListForUser(userId: Int, offset: Int, portionSize: Int, tagsIds: Option[Seq[Int]] = None)(implicit s: JdbcBackend#Session): List[(ArticleRecord, UserRecord, Seq[String])]

    def get(id: Int)(implicit s: JdbcBackend#Session): Option[(ArticleRecord, UserRecord, Seq[String])]

    def insert(article: ArticleRecord)(implicit s: JdbcBackend#Session): Int

    def update(id: Int, article: ArticleToUpdate)(implicit s: JdbcBackend#Session): Boolean

    def remove(id: Int)(implicit s: JdbcBackend#Session): Boolean

    def count(tagsIds: Option[Seq[Int]] = None)(implicit s: JdbcBackend#Session): Int

    def countForUser(userId: Int, tagsIds: Option[Seq[Int]] = None)(implicit s: JdbcBackend#Session): Int
  }

}

trait SlickArticlesRepositoryComponent extends ArticlesRepositoryComponent {
  this: Profile with UsersSchemaComponent with ArticlesSchemaComponent with TagsSchemaComponent =>

  val articlesRepository = new SlickArticlesRepository

  import profile.simple._


  implicit class ArticlesExtension[E](val q: Query[Articles, E]) {
    def withAuthor = {
      q.leftJoin(users).on(_.authorId === _.id)
    }

    def portion(offset: Int, portionSize: Int) = {
      q.sortBy(_.createdAt.desc)
        .drop(offset)
        .take(portionSize)
        .withAuthor
    }

    def byId(id: Column[Int]) = {
      q.filter(_.id === id)
    }

    def byAuthor(id: Column[Int]) = {
      q.filter(_.authorId === id)
    }

    def byTag(tagId: Column[Int]) = {
      q.leftJoin(articlesTags).on(_.id === _.articleId).filter(_._2.tagId === tagId).map(_._1)
    }

    def idsByTags(tagsIds: Seq[Int]) = {
      byTags(tagsIds).map(_.id)
    }

    def byTags(tagsIds: Seq[Int]) = {
      q.leftJoin(articlesTags).on(_.id === _.articleId).filter(_._2.tagId.inSet(tagsIds)).map(_._1)
    }
  }

  class SlickArticlesRepository extends ArticlesRepository {

    def getList(offset: Int, portionSize: Int, tagsIds: Option[Seq[Int]] = None)(implicit s: JdbcBackend#Session) = {
      tagsIds match {
        case Some(x) =>  articles.byTags(x).portion(offset, portionSize).list.distinct.map(fetchTags)
        case None =>  articles.portion(offset, portionSize).list.distinct.map(fetchTags)
      }

    }

    def getListForUser(userId: Int, offset: Int, portionSize: Int, tagsIds: Option[Seq[Int]] = None)(implicit s: JdbcBackend#Session) = {
      tagsIds match {
        case Some(ids) => articles.byAuthor(userId).byTags(ids).portion(offset, portionSize).list.distinct.map(fetchTags)
        case None => articles.byAuthor(userId).portion(offset, portionSize).list.distinct.map(fetchTags)
      }
    }

    def get(id: Int)(implicit s: JdbcBackend#Session) = {
      articles.byId(id).withAuthor().firstOption.map(fetchTags)
    }

    def insert(article: ArticleRecord)(implicit s: Session) = {
      articles.returning(articles.map(_.id)) += article
    }

    def update(id: Int, articleToUpdate: ArticleToUpdate)(implicit s: JdbcBackend#Session) = {
      articles.byId(id).map(a => (a.title, a.content, a.updatedAt, a.description))
        .update(ArticleToUpdate.unapply(articleToUpdate).get) > 0
    }

    def remove(id: Int)(implicit s: JdbcBackend#Session) = {
      articles.byId(id).delete > 0
    }

    def count(tagsIds: Option[Seq[Int]] = None)(implicit s: JdbcBackend#Session) = {
      tagsIds match {
        case Some(ids) => articles.idsByTags(tagsIds.get).countDistinct.run
        case None => articles.length.run
      }
    }

    def countForUser(userId: Int, tagsIds: Option[Seq[Int]] = None)(implicit s: JdbcBackend#Session): Int = {
      tagsIds match {
        case Some(ids) => articles.filter(_.authorId === userId).idsByTags(tagsIds.get).countDistinct.run
        case None => articles.filter(_.authorId === userId).length.run
      }
    }

    private def fetchTags(t: (ArticleRecord, UserRecord))(implicit s: JdbcBackend#Session) = t match {
      case (article, author) => (article, author, articleTags(article.id.get).list)
    }

    //TODO: convert to compiled query?
    private def articleTags(articleId: Int) = for {
      articleTag <- articlesTags if articleTag.articleId === articleId
      tag <- tags if articleTag.tagId === tag.id
    } yield tag.name
  }

}
