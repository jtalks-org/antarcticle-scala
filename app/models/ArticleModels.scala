package models

import models.UserModels.UserModel

object ArticleModels {
  case class ArticleListModel(id: Int, title: String, description: String, createdAt: java.util.Date, author: UserModel)

  case class ArticleDetailsModel(id: Int, title: String, content: String, createdAt: java.util.Date, author: UserModel)

  // article form
  case class Article(id: Option[Int] = None, title: String, content: String, tags: Seq[String]) {
    //TODO: strip tags
    lazy val description = content.take(300)
  }
}
