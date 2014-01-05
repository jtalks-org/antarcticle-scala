package repositories

import models.database._
import scala.slick.session.Session

trait ArticlesRepositoryComponent {
  val articlesRepository: ArticlesRepository

  trait ArticlesRepository {
    def getList(offset: Int, portionSize: Int)(implicit s: Session): List[(ArticleRecord, UserRecord, List[String])]
    def getListForUser(userId: Int, offset: Int, portionSize: Int)(implicit s: Session): List[(ArticleRecord, UserRecord, List[String])]
    def get(id: Int)(implicit s: Session): Option[(ArticleRecord, UserRecord, List[String])]
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

  implicit class ArticlesExtension[E](val q: Query[Articles.type, E]) {
    //TODO: simplify with slick 2.0
    def withAuthor(u: Query[Users.type, UserRecord] = Query(Users)): Query[(Articles.type, Users.type), (E, UserRecord)] = {
      q.leftJoin(u).on(_.authorId === _.id)
    }

    def portion(offset: Int, portionSize: Int) = {
      q.withAuthor()
        .drop(offset)
        .take(portionSize)
        .sortBy { case (article, _) => article.createdAt }
    }

    def byId(id: Column[Int]): Query[Articles.type, E] = {
      q.filter(_.id === id)
    }
  }

  class SlickArticlesRepository extends ArticlesRepository {

    def getList(offset: Int, portionSize: Int)(implicit s: Session) = {
      Query(Articles).portion(offset, portionSize).list.map(fetchTags)
    }

    def getListForUser(userId: Int, offset: Int, portionSize: Int)(implicit s: Session) = {
      Query(Articles).filter(_.authorId === userId).portion(offset, portionSize).list.map(fetchTags)
    }

    def get(id: Int)(implicit s: Session) = {
      Query(Articles).byId(id).withAuthor().firstOption.map(fetchTags)
    }

    def insert(article: ArticleToInsert)(implicit s: Session) = {
      Articles.forInsert.insert(article)
    }

    def update(id: Int, articleToUpdate: ArticleToUpdate)(implicit s: Session) = {
      Query(Articles).byId(id).map(_.forUpdate).update(articleToUpdate) > 0
    }

    def remove(id: Int)(implicit s: Session) = {
      Query(Articles).byId(id).delete > 0
    }

    def count()(implicit s: Session) = {
      Query(Articles.length).first
    }

    def countForUser(userId: Int)(implicit s: Session) = {
      //TODO: replace count with something else in slick 2.0
      (for {
        article <- Articles if article.authorId === userId
      } yield article.id.count).first
    }

    private def fetchTags(t: (ArticleRecord, UserRecord))(implicit s: Session) = t match {
      case (article, author) => (article, author, articleTags(article.id.get).list)
    }

    //TODO: convert to compiled query?
    private def articleTags(articleId: Int) = for {
        articleTag <- ArticlesTags if articleTag.articleId === articleId
        tag <- Tags if articleTag.tagId === tag.id
      } yield tag.name
  }
}
