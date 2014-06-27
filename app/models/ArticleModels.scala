package models

import models.UserModels.UserModel
import scala.language.implicitConversions

object ArticleModels {

  object Language extends Enumeration {
    type Language = Value
    val Arabic, Chinese, Dutch, English, French, German, Hindi, Italian, Japanese, Korean, Polish,
    Portuguese, Russian, Spanish, Turkish, Ukrainian = Value
  }

  import Language._

  case class ArticleListModel(id: Int, title: String, description: String,
                              createdAt: java.util.Date, author: UserModel, tags: Seq[String], commentsCount: Int)

  case class ArticleDetailsModel(id: Int, title: String, content: String,
                                 createdAt: java.util.Date, author: UserModel, tags: Seq[String],
                                 language: Language, sourceId: Int, translations: List[Translation])

  // article form
  case class Article(id: Option[Int] = None, title: String, content: String, tags: Seq[String], language: Language, sourceId: Option[Int]) {
    //TODO: strip tags
    lazy val description = content
  }

  case class Translation(id: Int, language: Language)

  implicit def languageToString(lang: Language) = lang.toString
  implicit def stringToLanguage(lang: String) = Language.withName(lang)

  implicit def detailsAsArticle(details: ArticleDetailsModel) =
    Article(Some(details.id), details.title, details.content, details.tags, details.language, Some(details.sourceId))
}
