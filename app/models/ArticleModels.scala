package models

import models.UserModels.UserModel
import scala.language.implicitConversions

object ArticleModels {

  case class ArticleListModel(id: Int, title: String, description: String,
                              createdAt: java.util.Date, author: UserModel, tags: Seq[String], commentsCount: Int)

  case class ArticleDetailsModel(id: Int, title: String, content: String,
                                 createdAt: java.util.Date, author: UserModel, tags: Seq[String])

  // article form
  case class Article(id: Option[Int] = None, title: String, content: String, tags: Seq[String]) {
    //TODO: strip tags
    lazy val description = content
  }


  object Language extends Enumeration {
    type Language = Value
    val Arabic, Chinese, Dutch, English, French, German, Hindi, Italian, Japanese, Korean, Polish,
      Portuguese, Russian, Spanish, Turkish, Ukrainian = Value
  }


  implicit def detailsAsArticle(details: ArticleDetailsModel) =
    Article(Some(details.id), details.title, details.content, details.tags)
}
