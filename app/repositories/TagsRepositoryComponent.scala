package repositories

import models.database.{Profile, Tag, TagsSchemaComponent}
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation
import scala.slick.jdbc.JdbcBackend

trait TagsRepositoryComponent {

  val tagsRepository: TagsRepository

  trait TagsRepository {
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

  class TagsRepositoryImpl extends TagsRepository {
    implicit val getTagResult = GetResult(r => Tag(r.<<, r.<<))


    override def getByName(name: Option[String])(implicit session: JdbcBackend#Session) = {
      tags.filter(_.name === name).firstOption.map(r => Tag(r._1.get, r._2))
    }

    override def getByNames(names: Seq[String])(implicit session: JdbcBackend#Session) = {
      import models.database.Tag
      if (names.isEmpty) {
        Vector[Tag]()
      } else {
        val inClause = names.map(name => s"'$name'").mkString(",")
        sql"select id, name from tags where name in (#$inClause)".as[Tag].list
      }
    }

    override def insertTags(names: Seq[String])(implicit session: JdbcBackend#Session) = {
      tags.map(t => t.name).returning(tags.map(_.id)).insertAll(names.distinct: _*)
    }

    override def insertArticleTags(articleTags: Seq[(Int, Int)])(implicit session: JdbcBackend#Session) = {
      articlesTags.insertAll(articleTags.distinct: _*)
    }

    override def removeArticleTags(articleId: Int)(implicit session: JdbcBackend#Session): Unit = {
      articlesTags.filter(_.articleId === articleId).delete
    }
  }
}
