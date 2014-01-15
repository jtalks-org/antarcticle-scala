package repositories

import models.database.{Profile, Tag, TagsSchemaComponent}
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation
import scala.slick.jdbc.JdbcBackend

trait TagsRepositoryComponent {

  val tagsRepository: TagsRepository

  trait TagsRepository {
    def getByNames(names: Seq[String])(implicit session: JdbcBackend#Session): Seq[Tag]
    def insertAll(names: Seq[String])(implicit session: JdbcBackend#Session): Seq[Int]
    def insertArticleTags(articleTags: Seq[(Int, Int)])(implicit session: JdbcBackend#Session)
  }
}

trait TagsRepositoryComponentImpl extends TagsRepositoryComponent {
  this: TagsSchemaComponent with Profile =>

  val tagsRepository = new TagsRepositoryImpl

  import profile.simple._

  class TagsRepositoryImpl extends TagsRepository {
    implicit val getTagResult = GetResult(r => Tag(r.<<, r.<<))

    def getByNames(names: Seq[String])(implicit session: JdbcBackend#Session) = {
      import models.database.Tag
      if (names.isEmpty) {
        Vector[Tag]()
      } else {
        val inClause = names.map(name => s"'$name'").mkString(",")
        sql"select id, name from tags where name in (#$inClause)".as[Tag].list
      }
    }

    def insertAll(names: Seq[String])(implicit session: JdbcBackend#Session) = {
      tags.map(t => t.name).returning(tags.map(_.id)).insertAll(names : _*)
    }

    def insertArticleTags(articleTags: Seq[(Int, Int)])(implicit session: JdbcBackend#Session) = {
      articlesTags.insertAll(articleTags : _*)
    }
  }
}
