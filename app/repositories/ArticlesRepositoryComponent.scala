package repositories

import models.database._
import models.database.ArticleToUpdate
import models.database.UserRecord
import models.database.ArticleRecord
import models.database.ArticleToInsert

trait ArticlesRepositoryComponent {
  import scala.slick.jdbc.JdbcBackend.Session

  val articlesRepository: ArticlesRepository

  trait ArticlesRepository {
    def getList(offset: Int, portionSize: Int)(implicit s: Session): List[(ArticleRecord, UserRecord, Seq[String])]
    def getListForUser(userId: Int, offset: Int, portionSize: Int)(implicit s: Session): Seq[(ArticleRecord, UserRecord, Seq[String])]
    def get(id: Int)(implicit s: Session): Option[(ArticleRecord, UserRecord, Seq[String])]
    def insert(article: ArticleToInsert)(implicit s: Session): Int
    def update(id: Int, article: ArticleToUpdate)(implicit s: Session): Boolean
    def remove(id: Int)(implicit s: Session): Boolean
    def count()(implicit s: Session): Int
    def countForUser(userId: Int)(implicit s: Session): Int
  }
}

trait SlickArticlesRepositoryComponent extends ArticlesRepositoryComponent {
  this: Profile with UsersSchemaComponent with ArticlesSchemaComponent with TagsSchemaComponent =>

  val articlesRepository = new SlickArticlesRepository

  import profile.simple._
  import scala.slick.jdbc.JdbcBackend.Session

  implicit class ArticlesExtension[E](val q: Query[Articles, E]) {
    def withAuthor = {
      q.leftJoin(users).on(_.authorId === _.id)
    }

    def portion(offset: Int, portionSize: Int) = {
      q.withAuthor
        // .sortBy { case (article, _) => article.createdAt.desc }
        .drop(offset)
        .take(portionSize)
    }

    def byId(id: Column[Int]): Query[Articles, E] = {
      q.filter(_.id === id)
    }
  }

  class SlickArticlesRepository extends ArticlesRepository {

    def getList(offset: Int, portionSize: Int)(implicit s: Session) = {
      val q = (for {
        (article, author) <- articles.sortBy(_.createdAt).drop(offset).take(portionSize) innerJoin users on(_.authorId === _.id)
      } yield (article, author))
      println(q.selectStatement)
      q.list.map(fetchTags)
    }

    def getListForUser(userId: Int, offset: Int, portionSize: Int)(implicit s: Session) = {
      articles.filter(_.authorId === userId).portion(offset, portionSize).list.map(fetchTags)
    }

    def get(id: Int)(implicit s: Session) = {
      articles.byId(id).withAuthor().firstOption.map(fetchTags)
    }

    def insert(article: ArticleToInsert)(implicit s: Session) = {
      articles.map(a => (a.title, a.content, a.createdAt, a.updatedAt, a.description, a.authorId))
      .returning(articles.map(_.id)) += ArticleToInsert.unapply(article).get
    }

    def update(id: Int, articleToUpdate: ArticleToUpdate)(implicit s: Session) = {
      articles.byId(id).map(a => (a.title, a.content, a.updatedAt, a.description))
        .update(ArticleToUpdate.unapply(articleToUpdate).get) > 0
    }

    def remove(id: Int)(implicit s: Session) = {
      articles.byId(id).delete > 0
    }

    def count()(implicit s: Session) = {
      articles.length.run
    }

    def countForUser(userId: Int)(implicit s: Session) = {
      //TODO: replace count with something else in slick 2.0
      // (for {
      //   article <- articles if article.authorId === userId
      // } yield article.id.count).first
      articles.filter(_.authorId === userId).length.run
    }

    private def fetchTags(t: (ArticleRecord, UserRecord))(implicit s: Session) = t match {
      case (article, author) => (article, author, articleTags(article.id.get).list)
    }

    //TODO: convert to compiled query?
    private def articleTags(articleId: Int) = for {
        articleTag <- articlesTags if articleTag.articleId === articleId
        tag <- tags if articleTag.tagId === tag.id
      } yield tag.name
  }
}
