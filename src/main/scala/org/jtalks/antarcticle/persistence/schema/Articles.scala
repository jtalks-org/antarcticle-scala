package org.jtalks.antarcticle.persistence.schema

import java.sql.Timestamp

import org.jtalks.antarcticle.persistence.Profile
import org.jtalks.antarcticle.models.UserModel

case class Article(id: Option[Int], title: String, content: String, createdAt: Timestamp, authorId: Int)

trait ArticlesComponent  {
  this: Profile with UsersComponent =>

  import profile.simple._

  object Articles extends Table[Article]("articles") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def content = column[String]("content")
    def createdAt = column[Timestamp]("created_at")
    def authorId = column[Int]("author_id")
    def author = foreignKey("author_fk", authorId, Users)(_.id)
    def * = id.? ~ title ~ content ~ createdAt ~ authorId <> (Article.apply _, Article.unapply _)

    def autoInc = * returning id
  }

}