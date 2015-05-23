package models

import models.UserModels.UserModel
import scala.language.implicitConversions

object ArticleModels {

  object Language extends Enumeration {
    protected case class Val(title: String) extends super.Val
    type Language = Val
    val Arabic = Val("العربية")
    val Chinese = Val("中文")
    val Dutch = Val("Nederlands")
    val English = Val("English")
    val French = Val("Français")
    val German = Val("Deutsch")
    val Hindi = Val("हिंदी")
    val Italian = Val("Italiano")
    val Japanese = Val("日本語")
    val Korean = Val("한국어")
    val Polish = Val("Polski")
    val Portuguese = Val("Português")
    val Russian = Val("Русский")
    val Spanish = Val("Español")
    val Turkish = Val("Türkçe")
    val Ukrainian = Val("Українська")

    def languageIterator: Iterator[Language] = super.values.iterator.map(l => l.asInstanceOf[Language])

    implicit def languageToString(lang: Language): String = lang.toString()
    implicit def parseLanguage(lang: String): Val = Language.withName(lang).asInstanceOf[Val]
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

  implicit def detailsAsArticle(details: ArticleDetailsModel): Article =
    Article(Some(details.id), details.title, details.content, details.tags, details.language, Some(details.sourceId))
}
