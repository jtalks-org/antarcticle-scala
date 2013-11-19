package org.jtalks.antarcticle.persistence.repositories

import org.jtalks.antarcticle.persistence._
import scala.slick.session.Session
import org.jtalks.antarcticle.persistence.DatabaseProvider

trait ArticlesRepositoryComponent {
  val articlesRepository: ArticlesRepository

  trait ArticlesRepository {
    def findAll: List[ArticleListModel]
    def get(id: Int): Option[ArticleListModel]
  }
}

trait SlickArticlesRepositoryComponent extends ArticlesRepositoryComponent {
  this: DatabaseProvider with Profile with UsersComponent with ArticlesComponent =>
  import profile.simple._

  val articlesRepository = new SlickArticlesRepository

  class SlickArticlesRepository extends ArticlesRepository {

    def findAll = {
      db withSession { implicit session: Session =>
        articleWithAuthor.list.map(toModel(_))
      }
    }

    def get(id: Int) = {
      db withSession { implicit session: Session =>
        (for {
          (article, author) <- articleWithAuthor if article.id === id
        } yield (article, author)).firstOption.map(toModel(_))
      }
    }

    private val toModel = (tuple: (Article, User)) => tuple match {
      case (article, author) =>
        ArticleListModel(article.id.get, article.title,
          article.content, article.createdAt,
            UserModel(author.id.get, author.username))
    }

    private def articleWithAuthor = for {
        article <- Articles
        author <- article.author
      } yield (article, author)

  }
}
