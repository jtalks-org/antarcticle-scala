package repositories

import play.api.db.slick.Config.driver.simple._
import models.database._

trait ArticlesRepositoryComponent {
  val articlesRepository: ArticlesRepository

  trait ArticlesRepository {
    def getList(offset: Int, portionSize: Int)(implicit s: Session): List[(ArticleRecord, UserRecord)]
    def get(id: Int)(implicit s: Session): Option[(ArticleRecord, UserRecord)]
    def insert(article: ArticleToInsert)(implicit s: Session): Int
    def update(id: Int, article: ArticleToUpdate)(implicit s: Session): Boolean
    def remove(id: Int)(implicit s: Session): Boolean
  }
}

trait SlickArticlesRepositoryComponent extends ArticlesRepositoryComponent {
  this: Profile with UsersComponent with ArticlesComponent =>

  val articlesRepository = new SlickArticlesRepository

  class SlickArticlesRepository extends ArticlesRepository {
    import profile.simple._

    def getList(offset: Int, portionSize: Int)(implicit s: Session) = {
      articlesWithAuthor
        .drop(offset)
        .take(portionSize)
        .sortBy { case (article, _) => article.createdAt }
        .list
    }

    def get(id: Int)(implicit s: Session) = {
      (for {
        (article, author) <- articlesWithAuthor if article.id === id
      } yield (article, author)).firstOption
    }

    def insert(article: ArticleToInsert)(implicit s: Session) = {
      Articles.forInsert.insert(article)
    }

    def update(id: Int, articleToUpdate: ArticleToUpdate)(implicit s: Session) = {
      Articles
        .filter(_.id === id)
        .map(_.forUpdate)
        .update(articleToUpdate) > 0
    }

    def remove(id: Int)(implicit s: Session) = {
      Articles.where(_.id === id).delete > 0
    }

    private def articlesWithAuthor = for {
        article <- Articles
        author <- article.author
      } yield (article, author)

  }
}
