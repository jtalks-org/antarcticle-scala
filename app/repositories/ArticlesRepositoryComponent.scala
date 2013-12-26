package repositories

import models.database._
import scala.slick.session.Session

trait ArticlesRepositoryComponent {
  val articlesRepository: ArticlesRepository

  trait ArticlesRepository {
    def getList(offset: Int, portionSize: Int)(implicit s: Session): List[(ArticleRecord, UserRecord, List[String])]
    def get(id: Int)(implicit s: Session): Option[(ArticleRecord, UserRecord, List[String])]
    def insert(article: ArticleToInsert)(implicit s: Session): Int
    def update(id: Int, article: ArticleToUpdate)(implicit s: Session): Boolean
    def remove(id: Int)(implicit s: Session): Boolean
    def count()(implicit s: Session): Int
  }
}

trait SlickArticlesRepositoryComponent extends ArticlesRepositoryComponent {
  this: Profile with UsersSchemaComponent with ArticlesSchemaComponent with TagsSchemaComponent =>

  val articlesRepository = new SlickArticlesRepository

  class SlickArticlesRepository extends ArticlesRepository {
    import profile.simple._

    def getList(offset: Int, portionSize: Int)(implicit s: Session) = {
      articlesWithAuthor
        .drop(offset)
        .take(portionSize)
        .sortBy { case (article, _) => article.createdAt }
        .list.map(withTags)
    }

    def get(id: Int)(implicit s: Session) = {
      (for {
        (article, author) <- articlesWithAuthor if article.id === id
      } yield (article, author)).firstOption.map(withTags)
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

    def count()(implicit s: Session) = {
      Query(Articles.length).first
    }

    //TODO: join tags in the same slick query as article
    private def withTags(t: (ArticleRecord, UserRecord))(implicit s: Session) = t match {
      case (article, author) => (article, author, getArticleTagsNames(article.id.get))
    }

    private def getArticleTagsNames(articleId: Int)(implicit s: Session) = {
      articleTags(articleId).list.map {
        case (_, name) => name
      }
    }

    private def articlesWithAuthor = for {
        article <- Articles
        author <- article.author
      } yield (article, author)

    private def articleTags(articleId: Int) = for {
        articleTag <- ArticlesTags if articleTag.articleId === articleId
        tag <- Tags if articleTag.tagId === tag.id
      } yield tag
  }
}
