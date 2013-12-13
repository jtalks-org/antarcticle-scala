package org.jtalks.antarcticle.persistence.repositories

import org.jtalks.antarcticle.persistence._
import org.jtalks.antarcticle.persistence.schema._
import org.jtalks.antarcticle.persistence.schema.ArticleRecord

trait ArticlesRepositoryComponent {
  val articlesRepository: ArticlesRepository

  trait ArticlesRepository {
    def getList(offset: Int, portionSize: Int): List[(ArticleRecord, UserRecord)]
    def get(id: Int): Option[(ArticleRecord, UserRecord)]
    def insert(article: ArticleRecord): ArticleRecord
    def update(id: Int, article: ArticleToUpdate): Boolean
    def remove(id: Int): Boolean
  }
}

trait SlickArticlesRepositoryComponent extends ArticlesRepositoryComponent {
  this: DatabaseProfile with UsersComponent with ArticlesComponent =>
  import profile.simple._

  val articlesRepository = new SlickArticlesRepository

  class SlickArticlesRepository extends ArticlesRepository {

    def getList(offset: Int, portionSize: Int) = {
      db withSession { implicit session: Session =>
        articlesWithAuthor
          .drop(offset)
          .take(portionSize)
          .sortBy { case (article, _) => article.createdAt }
          .list
      }
    }

    def get(id: Int) = {
      db withSession { implicit session: Session =>
        (for {
          (article, author) <- articlesWithAuthor if article.id === id
        } yield (article, author)).firstOption
      }
    }

    def insert(article: ArticleRecord) = {
      db withSession { implicit session: Session =>
        val id = Articles.autoInc.insert(article)
        article.copy(id = Option(id))
      }
    }

    def update(id: Int, articleToUpdate: ArticleToUpdate) = {
      db withSession { implicit session: Session =>
        Query(Articles)
          .filter(_.id === id)
          .map(_.updateProjection)
          .update(articleToUpdate) > 0
      }
    }

    def remove(id: Int) = {
      db withSession { implicit session: Session =>
        Articles.where(_.id === id).delete > 0
      }
    }

    private def articlesWithAuthor = for {
        article <- Articles
        author <- article.author
      } yield (article, author)

  }
}
