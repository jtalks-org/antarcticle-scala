package repositories

import models.database.{ArticleRecord, ArticleToUpdate, UserRecord, _}

import scala.language.higherKinds
import scala.slick.jdbc.JdbcBackend

trait ArticlesRepositoryComponent {
  val articlesRepository: ArticlesRepository

  /**
   * Provides basic articles-related operations over a database.
   * Database session should be provided by a caller via implicit parameter.
   */
  trait ArticlesRepository {
    def getList(offset: Int, portionSize: Int, tagsIds: Option[Seq[Int]] = None)
               (implicit s: JdbcBackend#Session): List[(ArticleRecord, UserRecord, Seq[String], Int)]

    def getListForUser(userId: Int, offset: Int, portionSize: Int, tagsIds: Option[Seq[Int]] = None)
                      (implicit s: JdbcBackend#Session): List[(ArticleRecord, UserRecord, Seq[String], Int)]

    def get(id: Int)(implicit s: JdbcBackend#Session): Option[(ArticleRecord, UserRecord, Seq[String])]

    def insert(article: ArticleRecord)(implicit s: JdbcBackend#Session): Int

    def update(id: Int, article: ArticleToUpdate)(implicit s: JdbcBackend#Session): Boolean

    def remove(id: Int)(implicit s: JdbcBackend#Session): Boolean

    def count(tagsIds: Option[Seq[Int]] = None)(implicit s: JdbcBackend#Session): Int

    def countForUser(userId: Int, tagsIds: Option[Seq[Int]] = None)(implicit s: JdbcBackend#Session): Int

    def getTranslations(id: Int)(implicit s: JdbcBackend#Session): List[(Int,String)]
  }

}

trait SlickArticlesRepositoryComponent extends ArticlesRepositoryComponent {
  this: Profile with UsersSchemaComponent with ArticlesSchemaComponent
    with TagsSchemaComponent with CommentsSchemaComponent =>

  val articlesRepository = new SlickArticlesRepository

  import profile.simple._

  /**
   * Query extensions to avoid criteria duplication
   */
  implicit class ArticlesExtension[E, C[_]](val q: Query[Articles, E, C]) {
    def withAuthor = {
      q.leftJoin(users).on(_.authorId === _.id)
    }

    def portion(offset: Int, portionSize: Int) = {
      q.sortBy(_.createdAt.desc)
        .drop(offset)
        .take(portionSize)
        .withAuthor
    }

    def byId(id: Column[Int]): Query[Articles, E, C] = {
      q.filter(_.id === id)
    }

    def byAuthor(id: Column[Int]): Query[Articles, E, C] = {
      q.filter(_.authorId === id)
    }

    def byTag(tagId: Column[Int]) = {
      q.leftJoin(articlesTags)
        .on(_.id === _.articleId)
        .filter(_._2.tagId === tagId)
        .map(_._1)
    }
  }

  /**
   * Slick article dao implementation based on precompiled queries.
   * For information about precompiled queries refer to
   * <p> http://slick.typesafe.com/doc/2.0.0/queries.html#compiled-queries
   * <p> http://stackoverflow.com/questions/21422394/why-cannot-use-compiled-insert-statement-in-slick
   */
  class SlickArticlesRepository extends ArticlesRepository {

    val forInsertCompiled = articles.returning(articles.map(_.id)).insertInvoker
    val forRemoveCompiled = Compiled((id: Column[Int]) => articles.byId(id))
    val forUpdateCompiled = Compiled((id: Column[Int]) =>
      articles.byId(id).map(a => (a.title, a.content, a.updatedAt, a.description, a.language, a.published)))
    val forUdateSourceId = Compiled((id: Column[Int]) => articles.byId(id).map(a => a.sourceId))
    val articleTagsCompiled = Compiled((id: Column[Int]) => for {
      articleTag <- articlesTags if articleTag.articleId === id
      tag <- tags if articleTag.tagId === tag.id
    } yield tag.name)
    val articleCommentsCompiled = Compiled((id: Column[Int]) => for {
      comment <- comments if comment.articleId === id
    } yield comment.id)

    val translationCompiled = Compiled((sourceId: Column[Int]) => for {
      article <- articles if article.sourceId === sourceId
    } yield (article.id, article.language))

    def getList(offset: Int, portionSize: Int, tagsIds: Option[Seq[Int]] = None)(implicit s: JdbcBackend#Session) = {
      (tagsIds match {
        case Some(x) => articlesByTags(x)
        case None => articles
      }).portion(offset, portionSize).list.map(fetchTagsAndCommentsCount)
    }

    def getListForUser(userId: Int, offset: Int, portionSize: Int, tagsIds: Option[Seq[Int]] = None)(implicit s: JdbcBackend#Session) = {
      (tagsIds match {
        case Some(x) => articlesByTags(x).byAuthor(userId)
        case None => articles.byAuthor(userId)
      }).portion(offset, portionSize).list.map(fetchTagsAndCommentsCount)
    }

    def get(id: Int)(implicit s: JdbcBackend#Session) = {
      articles.byId(id).withAuthor.firstOption.map(fetchTags)
    }

    def insert(article: ArticleRecord)(implicit s: Session) = {
      val id = forInsertCompiled.insert(article)
      if (!article.sourceId.isDefined) {
        forUdateSourceId(id).update(id)
      }
      id
    }

    def update(id: Int, articleToUpdate: ArticleToUpdate)(implicit s: JdbcBackend#Session) =
      forUpdateCompiled(id).update(ArticleToUpdate.unapply(articleToUpdate).get) > 0

    def remove(id: Int)(implicit s: JdbcBackend#Session) = forRemoveCompiled(id).delete > 0

    def count(tagsIds: Option[Seq[Int]] = None)(implicit s: JdbcBackend#Session) = {
      (tagsIds match {
        case Some(ids) => articlesByTags(ids)
        case None => articles
      }).length.run
    }

    def countForUser(userId: Int, tagsIds: Option[Seq[Int]] = None)(implicit s: JdbcBackend#Session): Int = {
      (tagsIds match {
        case Some(ids) => articlesByTags(ids).byAuthor(userId)
        case None => articles.filter(_.authorId === userId)
      }).length.run
    }

    def getTranslations(id: Int)(implicit s: JdbcBackend#Session) = {
      forRemoveCompiled(id).firstOption match {
        case Some(article) => translationCompiled(article.sourceId.get).list
        case None => List()
      }
    }

    private def fetchTags(t: (ArticleRecord, UserRecord))(implicit s: JdbcBackend#Session) = t match {
      case (article, author) => (article, author, articleTagsCompiled(article.id.get).list)
    }

    private def fetchTagsAndCommentsCount(t: (ArticleRecord, UserRecord))(implicit s: JdbcBackend#Session) = t match {
        case (article, author) => 
          (article, author, articleTagsCompiled(article.id.get).list, articleCommentsCompiled(article.id.get).list.length)
    }

    /**
     * Returns only articles containing all the tags given.
     * For empty set will return an article empty set.
     *
     * select * from article_tags
     * where tag_id in (tagIds)
     * join articles
     * on article.id = article_tags.articleId
     * group by article_tags.articleId
     * having count(*) = tagIds.size
     */
    private def articlesByTags(tagsIds: Seq[Int])(implicit s: JdbcBackend#Session) = {
      articlesTags.filter(_.tagId.inSet(tagsIds))
        .groupBy {_.articleId}
        .map {case (articleId, tagIdGroup) => (articleId, tagIdGroup.length)}
        .filter(_._2 === tagsIds.size)
        .join(articles)
        .on(_._1 === _.id)
        .map(_._2)
    }
  }

}
