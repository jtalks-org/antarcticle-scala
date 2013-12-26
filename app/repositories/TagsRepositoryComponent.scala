package repositories

import scala.slick.session.Session
import models.database.{Profile, Tag, TagsSchemaComponent}
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation

trait TagsRepositoryComponent {
  val tagsRepository: TagsRepository

  trait TagsRepository {
    def getByNames(names: Seq[String])(implicit session: Session): Seq[Tag]
    def insertAll(names: Seq[String])(implicit session: Session): Seq[Int]
    def insertArticleTags(articleTags: Seq[(Int, Int)])(implicit session: Session)
  }
}

trait TagsRepositoryComponentImpl extends TagsRepositoryComponent {
  this: TagsSchemaComponent with Profile =>

  val tagsRepository = new TagsRepositoryImpl

  class TagsRepositoryImpl extends TagsRepository {
    import profile.simple._

    implicit val getTagResult = GetResult(r => Tag(r.<<, r.<<))

    def getByNames(names: Seq[String])(implicit session: Session) = {
      if (names.isEmpty) {
        Vector[Tag]()
      } else {
        val inClause = names.map(name => s"'$name'").mkString(",")
        sql"select id, name from tags where name in (#$inClause)".as[Tag].list
      }
    }

    def insertAll(names: Seq[String])(implicit session: Session) = {
      Tags.forInsert.insertAll(names : _*)
    }

    def insertArticleTags(articleTags: Seq[(Int, Int)])(implicit session: Session) = {
      ArticlesTags.insertAll(articleTags : _*)
    }
  }
}
