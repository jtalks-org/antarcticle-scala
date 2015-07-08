package repositories

import models.database.{Profile, Tag, TagsSchemaComponent}
import scala.slick.jdbc.GetResult
import scala.slick.jdbc.JdbcBackend

trait TagsRepositoryComponent {

  val tagsRepository: TagsRepository

  /**
   * Provides basic tag-related operations over a database.
   * Database session should be provided by a caller via implicit parameter.
   */
  trait TagsRepository {
    def getAllTags()(implicit session: JdbcBackend#Session): Seq[Tag]

    def getByName(name: Option[String])(implicit session: JdbcBackend#Session): Option[Tag]

    def getByNames(names: Seq[String])(implicit session: JdbcBackend#Session): Seq[Tag]

    def insertTags(names: Seq[String])(implicit session: JdbcBackend#Session): Seq[Int]

    def insertArticleTags(articleTags: Seq[(Int, Int)])(implicit session: JdbcBackend#Session)

    def removeArticleTags(articleId: Int)(implicit session: JdbcBackend#Session)
  }
}

trait TagsRepositoryComponentImpl extends TagsRepositoryComponent {
  this: TagsSchemaComponent with Profile =>

  val tagsRepository = new TagsRepositoryImpl

  import profile.simple._

  /**
   * Slick tag dao implementation based on precompiled queries.
   * For information about precompiled queries refer to
   * <p> http://slick.typesafe.com/doc/2.0.0/queries.html#compiled-queries
   * <p> http://stackoverflow.com/questions/21422394/why-cannot-use-compiled-insert-statement-in-slick
   */
  class TagsRepositoryImpl extends TagsRepository {
    implicit val getTagResult = GetResult(r => Tag(r.<<, r.<<))

    val compiledByName = Compiled((name: Column[Option[String]]) => tags.filter(_.name === name))
    val compiledByArticleId = Compiled((id: Column[Int]) => articlesTags.filter(_.articleId === id))
    val compiledForInsert = tags.map(t => t.name).returning(tags.map(_.id)).insertInvoker
    val compiledArticleTagsForInsert = articlesTags.insertInvoker

    override def getAllTags()(implicit session: JdbcBackend#Session) = tags.list.map(r => Tag(r._1.get, r._2))

    override def getByName(name: Option[String])(implicit session: JdbcBackend#Session) =
      compiledByName(name).firstOption.map(r => Tag(r._1.get, r._2))

    override def insertTags(names: Seq[String])(implicit session: JdbcBackend#Session) =
      compiledForInsert.insertAll(names.map(_.toLowerCase).distinct: _*)

    override def insertArticleTags(articleTags: Seq[(Int, Int)])(implicit session: JdbcBackend#Session) =
      compiledArticleTagsForInsert.insertAll(articleTags.distinct: _*)

    override def removeArticleTags(articleId: Int)(implicit session: JdbcBackend#Session) =
      compiledByArticleId(articleId).delete

    override def getByNames(names: Seq[String])(implicit session: JdbcBackend#Session) = {
      (for {
        tag <- tags if tag.name inSet names.map(_.toLowerCase)
      } yield tag).list.map(tag => Tag(tag._1.get, tag._2))
    }
  }
}
