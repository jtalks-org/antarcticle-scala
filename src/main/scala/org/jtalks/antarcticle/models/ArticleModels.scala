package org.jtalks.antarcticle.models

object ArticleModels {
  case class ArticleListModel(id: Int, title: String, description: String, createdAt: java.util.Date, author: UserModel)

  case class ArticleDetailsModel(id: Int, title: String, content: String, createdAt: java.util.Date, author: UserModel)
}
