package models

import models.UserModels.UserModel
import scala.language.implicitConversions

object ArticleModels {

  case class ArticleListModel(id: Int, title: String, description: String,
                              createdAt: java.util.Date, author: UserModel, tags: Seq[String])

  case class ArticleDetailsModel(id: Int, title: String, content: String,
                                 createdAt: java.util.Date, author: UserModel, tags: Seq[String])

  // article form
  case class Article(id: Option[Int] = None, title: String, content: String, tags: Seq[String]) {
    //TODO: strip tags
    lazy val description = content
  }

  implicit def detailsAsArticle(details: ArticleDetailsModel) =
    Article(Some(details.id), details.title, details.content, details.tags)
}
